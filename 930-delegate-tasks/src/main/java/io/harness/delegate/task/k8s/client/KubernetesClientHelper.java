/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.k8s.client;

import static java.util.stream.Collectors.toSet;

import io.harness.delegate.task.k8s.ContainerDeploymentDelegateBaseHelper;
import io.harness.delegate.task.k8s.K8sInfraDelegateConfig;
import io.harness.k8s.KubernetesHelperService;
import io.harness.k8s.kubectl.Kubectl;
import io.harness.k8s.model.K8sDelegateTaskParams;
import io.harness.k8s.model.K8sSteadyStateDTO;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.k8s.model.KubernetesNamespaceEventWatchDTO;
import io.harness.k8s.model.KubernetesResourceId;
import io.harness.k8s.model.KubernetesRolloutStatusDTO;
import io.harness.k8s.steadystate.KubernetesApiWatcherFactory;
import io.harness.k8s.steadystate.KubernetesCliWatcherFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.kubernetes.client.openapi.ApiClient;
import java.util.List;
import java.util.Set;

@Singleton
public class KubernetesClientHelper {
  @Inject private KubernetesHelperService kubernetesHelperService;
  @Inject private ContainerDeploymentDelegateBaseHelper containerDeploymentDelegateBaseHelper;
  @Inject private KubernetesApiWatcherFactory kubernetesApiWatcherFactory;
  @Inject private KubernetesCliWatcherFactory kubernetesCliWatcherFactory;

  public KubernetesNamespaceEventWatchDTO createNamespaceEventWatchDTO(
      K8sSteadyStateDTO steadyStateDTO, ApiClient apiClient, Kubectl client) {
    int maxResourceNameLength = getMaxResourceNameLength(steadyStateDTO.getResourceIds());
    final String eventErrorFormat = "%-7s: %s";
    final String eventInfoFormat = "%-7s: %-" + maxResourceNameLength + "s   %s";
    return KubernetesNamespaceEventWatchDTO.builder()
        .apiClient(apiClient)
        .client(client)
        .executionLogCallback(steadyStateDTO.getExecutionLogCallback())
        .eventInfoFormat(eventInfoFormat)
        .eventErrorFormat(eventErrorFormat)
        .resourceIds(steadyStateDTO.getResourceIds())
        .workingDirectory(steadyStateDTO.getK8sDelegateTaskParams().getWorkingDirectory())
        .build();
  }

  public KubernetesRolloutStatusDTO createRolloutStatusDTO(
      K8sSteadyStateDTO steadyStateDTO, ApiClient apiClient, Kubectl client) {
    int maxResourceNameLength = getMaxResourceNameLength(steadyStateDTO.getResourceIds());
    final String statusFormat = "%n%-7s: %-" + maxResourceNameLength + "s   %s";

    return KubernetesRolloutStatusDTO.builder()
        .apiClient(apiClient)
        .client(client)
        .logCallback(steadyStateDTO.getExecutionLogCallback())
        .k8sDelegateTaskParams(steadyStateDTO.getK8sDelegateTaskParams())
        .isErrorFrameworkEnabled(steadyStateDTO.isErrorFrameworkEnabled())
        .statusFormat(statusFormat)
        .build();
  }

  public ApiClient createKubernetesApiClient(K8sInfraDelegateConfig k8sInfraDelegateConfig) {
    KubernetesConfig kubernetesConfig =
        containerDeploymentDelegateBaseHelper.createKubernetesConfig(k8sInfraDelegateConfig);
    return kubernetesHelperService.getApiClient(kubernetesConfig);
  }

  public Kubectl createKubernetesCliClient(K8sDelegateTaskParams k8sDelegateTaskParams) {
    return Kubectl.client(k8sDelegateTaskParams.getKubectlPath(), k8sDelegateTaskParams.getKubeconfigPath());
  }

  public Set<String> getNamespacesToMonitor(List<KubernetesResourceId> resourceIds, String namespace) {
    Set<String> namespacesToMonitor = resourceIds.stream().map(KubernetesResourceId::getNamespace).collect(toSet());
    namespacesToMonitor.add(namespace);
    return namespacesToMonitor;
  }

  private int getMaxResourceNameLength(List<KubernetesResourceId> resourceIds) {
    int maxResourceNameLength = 0;
    for (KubernetesResourceId kubernetesResourceId : resourceIds) {
      maxResourceNameLength = Math.max(maxResourceNameLength, kubernetesResourceId.getName().length());
    }
    return maxResourceNameLength;
  }
}
