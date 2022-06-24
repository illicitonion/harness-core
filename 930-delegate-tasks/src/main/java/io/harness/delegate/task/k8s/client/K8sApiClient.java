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
import io.harness.k8s.steadystate.watcher.event.K8sApiEventWatcher;
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
public class K8sApiClient implements K8sClient {
  @Inject private K8sClientHelper k8sClientHelper;
  @Inject private K8sApiEventWatcher k8sApiEventWatcher;
  @Inject private K8sWorkloadWatcherFactory workloadWatcherFactory;

  @Override
  public boolean performSteadyStateCheck(K8sSteadyStateDTO steadyStateDTO) throws Exception {
    List<KubernetesResourceId> workloads = steadyStateDTO.getResourceIds();
    if (EmptyPredicate.isEmpty(workloads)) {
      return true;
    }

    ApiClient apiClient =
        k8sClientHelper.createKubernetesApiClient(steadyStateDTO.getRequest().getK8sInfraDelegateConfig());
    Set<String> namespaces = k8sClientHelper.getNamespacesToMonitor(workloads, steadyStateDTO.getNamespace());
    LogCallback executionLogCallback = steadyStateDTO.getExecutionLogCallback();

    executionLogCallback.saveExecutionLog("Executing steady state check using Kubernetes java client.");

    K8sEventWatchDTO eventWatchDTO = k8sClientHelper.createNamespaceEventWatchDTO(steadyStateDTO, apiClient, null);
    K8sRolloutStatusDTO rolloutStatusDTO = k8sClientHelper.createRolloutStatusDTO(steadyStateDTO, apiClient, null);

    List<Future<?>> futureList = new ArrayList<>();
    boolean success = false;

    try {
      for (String ns : namespaces) {
        Future<?> threadRef = k8sApiEventWatcher.watchForEvents(ns, eventWatchDTO, executionLogCallback);
        futureList.add(threadRef);
      }

      for (KubernetesResourceId workload : workloads) {
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
      k8sApiEventWatcher.destroyRunning(futureList);
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
