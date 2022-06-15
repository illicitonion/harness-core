/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.k8s;

import static io.harness.logging.CommandExecutionStatus.FAILURE;
import static io.harness.logging.LogLevel.INFO;

import static java.lang.String.format;

import io.harness.exception.InvalidRequestException;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.KubernetesNamespaceEventWatchDTO;
import io.harness.k8s.model.KubernetesResourceId;
import io.harness.k8s.model.KubernetesRolloutStatusDTO;
import io.harness.k8s.steadystate.K8sRolloutStatusFactory;
import io.harness.k8s.steadystate.statuschecker.AbstractSteadyStateChecker;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.LogCallback;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.CoreV1EventList;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Singleton
@Slf4j
public class KubernetesSteadyStateService {
  @Inject private KubernetesHelperService kubernetesHelperService;
  @Inject private K8sRolloutStatusFactory k8sRolloutStatusFactory;
  @Inject @Named("k8sSteadyStateExecutor") private ExecutorService k8sSteadyStateExecutor;
  private static final String EVENT_ERROR_MESSAGE_FORMAT = "%-7s: %s";

  public boolean performSteadyStateCheck(KubernetesConfig kubernetesConfig, List<KubernetesResourceId> workloads,
      String namespace, LogCallback executionLogCallback, long steadyStateTimeoutInMillis) throws Exception {
    Set<String> namespaces = workloads.stream().map(KubernetesResourceId::getNamespace).collect(Collectors.toSet());
    namespaces.add(namespace);

    ApiClient apiClient = kubernetesHelperService.getApiClient(kubernetesConfig);

    KubernetesNamespaceEventWatchDTO k8sNamespaceEventWatchDTO =
        KubernetesNamespaceEventWatchDTO.builder()
            .executionLogCallback(executionLogCallback)
            .eventInfoFormat(getEventInfoMessageFormat(workloads))
            .eventErrorFormat(EVENT_ERROR_MESSAGE_FORMAT)
            .workloadNames(workloads.stream().map(KubernetesResourceId::getName).collect(Collectors.toSet()))
            .timeoutInMillis(steadyStateTimeoutInMillis)
            .build();

    boolean success = false;
    List<Future<?>> steadyStateFutures = new ArrayList<>();
    try {
      for (String namespaceToCheck : namespaces) {
        Future<?> eventWatchFuture = k8sSteadyStateExecutor.submit(
            () -> runEventWatchInNamespace(namespaceToCheck, k8sNamespaceEventWatchDTO, apiClient));
        steadyStateFutures.add(eventWatchFuture);
      }

      for (KubernetesResourceId workload : workloads) {
        KubernetesRolloutStatusDTO k8sRolloutStatusDTO = KubernetesRolloutStatusDTO.builder()
                                                             .kubernetesConfig(kubernetesConfig)
                                                             .workload(workload)
                                                             .logCallback(executionLogCallback)
                                                             .timeoutInMillis(steadyStateTimeoutInMillis)
                                                             .build();
        AbstractSteadyStateChecker statusViewer = k8sRolloutStatusFactory.getStatusViewer(workload.getKind());
        success = statusViewer.rolloutStatus(k8sRolloutStatusDTO, apiClient);
        if (!success) {
          break;
        }
      }
    } catch (Exception e) {
      log.error("Failed to do steady state check. ", e);
      throw e;
    } finally {
      if (success) {
        executionLogCallback.saveExecutionLog("\nDone.", INFO, CommandExecutionStatus.SUCCESS);
      } else {
        executionLogCallback.saveExecutionLog(
            format("%nStatus check for resources in namespace [%s] failed.", namespace), INFO, FAILURE);
      }
      for (Future<?> future : steadyStateFutures) {
        boolean cancelled = future.cancel(true);
        if (!cancelled) {
          future.cancel(true);
        }
      }

      OkHttpClient httpClient = apiClient.getHttpClient();
      shutdownExecutorService(httpClient.dispatcher().executorService());
      httpClient.connectionPool().evictAll();
    }

    return false;
  }

  private String getEventInfoMessageFormat(List<KubernetesResourceId> workloads) {
    int maxResourceNameLength = 0;
    for (KubernetesResourceId kubernetesResourceId : workloads) {
      maxResourceNameLength = Math.max(maxResourceNameLength, kubernetesResourceId.getName().length());
    }
    return "%-7s: %-" + maxResourceNameLength + "s   %s";
  }

  public void runEventWatchInNamespace(
      String namespace, KubernetesNamespaceEventWatchDTO k8sNamespaceEventWatchDTO, ApiClient apiClient) {
    CoreV1Api coreV1Api = new CoreV1Api(apiClient);
    String eventInfoFormat = k8sNamespaceEventWatchDTO.getEventInfoFormat();
    String eventErrorFormat = k8sNamespaceEventWatchDTO.getEventErrorFormat();
    Set<String> workloads = k8sNamespaceEventWatchDTO.getWorkloadNames();
    LogCallback executionLogCallback = k8sNamespaceEventWatchDTO.getExecutionLogCallback();

    try {
      String resourceVersion = null;
      while (true) {
        if (resourceVersion == null) {
          CoreV1EventList coreV1EventList =
              coreV1Api.listNamespacedEvent(namespace, null, null, null, null, null, null, null, null, null, false);
          resourceVersion = coreV1EventList.getMetadata() != null ? coreV1EventList.getMetadata().getResourceVersion()
                                                                  : resourceVersion;
        }
        try (Watch<CoreV1Event> watch = Watch.createWatch(apiClient,
                 coreV1Api.listNamespacedEventCall(
                     namespace, null, false, null, null, null, null, resourceVersion, null, 300, true, null),
                 new TypeToken<Response<CoreV1Event>>() {}.getType())) {
          for (Response<CoreV1Event> eventListResponse : watch) {
            CoreV1Event event = eventListResponse.object;
            V1ObjectReference ref = event.getInvolvedObject();
            if (workloads.parallelStream().noneMatch(
                    workloadName -> ref.getName() != null && ref.getName().contains(workloadName))) {
              continue;
            }
            if ("WARNING".equalsIgnoreCase(event.getType())) {
              executionLogCallback.saveExecutionLog(format(eventErrorFormat, "Event", event.getMessage()));
            } else {
              executionLogCallback.saveExecutionLog(
                  format(eventInfoFormat, "Event", ref.getName(), event.getMessage()));
            }
          }
        } catch (ApiException ex) {
          if (ex.getCode() == 504 || ex.getCode() == 410) {
            resourceVersion = extractResourceVersionFromException(ex);
          } else {
            resourceVersion = null;
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      throw new InvalidRequestException("Failed to fetch events from namespace");
    }
  }

  private static String extractResourceVersionFromException(ApiException ex) {
    String body = ex.getResponseBody();
    if (body == null) {
      return null;
    }

    Gson gson = new Gson();
    Map<?, ?> st = gson.fromJson(body, Map.class);
    Pattern p = Pattern.compile("Timeout: Too large resource version: (\\d+), current: (\\d+)");
    String msg = (String) st.get("message");
    Matcher m = p.matcher(msg);
    if (!m.matches()) {
      return null;
    }

    return m.group(2);
  }

  private void shutdownExecutorService(ExecutorService executorService) {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}
