/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.k8s.client;

import static io.harness.logging.CommandExecutionStatus.FAILURE;
import static io.harness.logging.LogLevel.INFO;

import static java.lang.String.format;

import io.harness.data.structure.EmptyPredicate;
import io.harness.k8s.model.K8sSteadyStateDTO;
import io.harness.k8s.model.KubernetesResourceId;
import io.harness.k8s.steadystate.model.K8sEventWatchDTO;
import io.harness.k8s.steadystate.model.K8sRolloutStatusDTO;
import io.harness.k8s.steadystate.watcher.event.KubeApiEventWatcher;
import io.harness.k8s.steadystate.watcher.workload.K8sWorkloadWatcherFactory;
import io.harness.k8s.steadystate.watcher.workload.WorkloadWatcher;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.LogCallback;

import com.google.inject.Inject;
import io.kubernetes.client.openapi.ApiClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesApiClient implements KubernetesClient {
  @Inject KubernetesClientHelper kubernetesClientHelper;
  @Inject KubeApiEventWatcher kubeApiEventWatcher;
  @Inject private K8sWorkloadWatcherFactory workloadWatcherFactory;

  @Override
  public boolean performSteadyStateCheck(K8sSteadyStateDTO steadyStateDTO) throws Exception {
    List<KubernetesResourceId> resourceIds = steadyStateDTO.getResourceIds();
    if (EmptyPredicate.isEmpty(resourceIds)) {
      return true;
    }

    ApiClient apiClient =
        kubernetesClientHelper.createKubernetesApiClient(steadyStateDTO.getRequest().getK8sInfraDelegateConfig());
    Set<String> namespaces = kubernetesClientHelper.getNamespacesToMonitor(resourceIds, steadyStateDTO.getNamespace());
    LogCallback executionLogCallback = steadyStateDTO.getExecutionLogCallback();

    executionLogCallback.saveExecutionLog("Executing steady state check using Kubernetes java client.");

    K8sEventWatchDTO eventWatchDTO =
        kubernetesClientHelper.createNamespaceEventWatchDTO(steadyStateDTO, apiClient, null);
    K8sRolloutStatusDTO rolloutStatusDTO =
        kubernetesClientHelper.createRolloutStatusDTO(steadyStateDTO, apiClient, null);

    List<Future<?>> futureList = new ArrayList<>();
    boolean success = false;

    try {
      for (String ns : namespaces) {
        Future<?> threadRef = kubeApiEventWatcher.watchForEvents(ns, eventWatchDTO, executionLogCallback);
        futureList.add(threadRef);
      }

      for (KubernetesResourceId workload : resourceIds) {
        WorkloadWatcher workloadWatcher = workloadWatcherFactory.getWorkloadWatcher(workload.getKind(), true);
        success = workloadWatcher.watchRolloutStatus(rolloutStatusDTO, workload, executionLogCallback);
        if (!success) {
          break;
        }
      }

      return success;
    } catch (Exception e) {
      log.error("Exception while doing statusCheck", e);
      if (steadyStateDTO.isErrorFrameworkEnabled()) {
        throw e;
      }

      executionLogCallback.saveExecutionLog("\nFailed.", INFO, FAILURE);
      return false;
    } finally {
      kubeApiEventWatcher.destroyRunning(futureList);
      if (success) {
        if (steadyStateDTO.isDenoteOverallSuccess()) {
          executionLogCallback.saveExecutionLog("\nDone.", INFO, CommandExecutionStatus.SUCCESS);
        }
      } else {
        executionLogCallback.saveExecutionLog(
            format("%nStatus check for resources in namespace [%s] failed.", steadyStateDTO.getNamespace()), INFO,
            FAILURE);
      }
    }
  }
}
