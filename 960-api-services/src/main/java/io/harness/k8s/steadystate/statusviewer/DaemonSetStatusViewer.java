/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.k8s.steadystate.statusviewer;

import io.harness.exception.InvalidRequestException;
import io.harness.k8s.steadystate.model.K8ApiResponseDTO;

import com.google.inject.Singleton;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

@Singleton
public class DaemonSetStatusViewer {
  public K8ApiResponseDTO extractRolloutStatus(V1DaemonSet daemonSet) {
    if (daemonSet.getSpec() != null && daemonSet.getSpec().getUpdateStrategy() != null
        && !"RollingUpdate".equals(daemonSet.getSpec().getUpdateStrategy().getType())) {
      throw new InvalidRequestException("rollout status is only available for RollingUpdate strategy type");
    }
    V1ObjectMeta meta = daemonSet.getMetadata();

    if (meta != null && meta.getGeneration() != null && daemonSet.getStatus() != null
        && daemonSet.getStatus().getObservedGeneration() != null
        && meta.getGeneration() <= daemonSet.getStatus().getObservedGeneration()) {
      V1DaemonSetStatus daemonSetStatus = daemonSet.getStatus();

      initializeNullFieldsInStatefulSetStatus(daemonSetStatus);

      if (daemonSetStatus != null
          && daemonSetStatus.getUpdatedNumberScheduled() < daemonSetStatus.getDesiredNumberScheduled()) {
        return K8ApiResponseDTO.builder()
            .message(String.format(
                "Waiting for daemon set %s rollout to finish: %d out of %d new pods have been updated...%n",
                meta.getName(), daemonSetStatus.getUpdatedNumberScheduled(),
                daemonSetStatus.getDesiredNumberScheduled()))
            .isDone(false)
            .build();
      }

      if (daemonSetStatus != null
          && daemonSetStatus.getNumberAvailable() < daemonSetStatus.getDesiredNumberScheduled()) {
        return K8ApiResponseDTO.builder()
            .message(
                String.format("Waiting for daemon set %s rollout to finish: %d of %d updated pods are available...%n",
                    meta.getName(), daemonSetStatus.getNumberAvailable(), daemonSetStatus.getDesiredNumberScheduled()))
            .isDone(false)
            .build();
      }

      return K8ApiResponseDTO.builder()
          .message(String.format("daemon set %s successfully rolled out%n", meta.getName()))
          .isDone(true)
          .build();
    }

    return K8ApiResponseDTO.builder()
        .isDone(false)
        .message("Waiting for daemon set spec update to be observed...%n")
        .build();
  }

  private void initializeNullFieldsInStatefulSetStatus(V1DaemonSetStatus daemonSetStatus) {
    if (daemonSetStatus != null) {
      if (daemonSetStatus.getUpdatedNumberScheduled() == null) {
        daemonSetStatus.setUpdatedNumberScheduled(0);
      }

      if (daemonSetStatus.getDesiredNumberScheduled() == null) {
        daemonSetStatus.setDesiredNumberScheduled(0);
      }

      if (daemonSetStatus.getNumberAvailable() == null) {
        daemonSetStatus.setNumberAvailable(0);
      }
    }
  }
}
