/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.k8s.steadystate.model;

import io.harness.k8s.kubectl.Kubectl;
import io.harness.k8s.model.K8sDelegateTaskParams;
import io.harness.logging.LogCallback;

import io.kubernetes.client.openapi.ApiClient;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class K8sRolloutStatusDTO {
  ApiClient apiClient;
  Kubectl client;
  K8sDelegateTaskParams k8sDelegateTaskParams;
  boolean isErrorFrameworkEnabled;
  String statusFormat;
}
