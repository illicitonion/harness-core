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
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.openapi.models.V1StatefulSetStatus;

@Singleton
public class StatefulSetStatusViewer {
  public K8ApiResponseDTO extractRolloutStatus(V1StatefulSet statefulSet) {
    V1ObjectMeta meta = statefulSet.getMetadata();

    if (statefulSet.getSpec() != null && statefulSet.getSpec().getUpdateStrategy() != null
        && !"RollingUpdate".equals(statefulSet.getSpec().getUpdateStrategy().getType())) {
      throw new InvalidRequestException("rollout status is only available for RollingUpdate strategy type");
    }

    V1StatefulSetStatus statefulSetStatus = statefulSet.getStatus();
    initializeNullFieldsInStatefulSetStatus(statefulSetStatus);

    if (statefulSetStatus != null && statefulSetStatus.getObservedGeneration() != null) {
      if (statefulSetStatus.getObservedGeneration() == 0
          || (meta != null && meta.getGeneration() != null
              && meta.getGeneration() > statefulSetStatus.getObservedGeneration())) {
        return K8ApiResponseDTO.builder()
            .message("Waiting for statefulset spec update to be observed...%n")
            .isDone(false)
            .build();
      }
    }
    V1StatefulSetSpec statefulSetSpec = statefulSet.getSpec();
    if (statefulSetSpec != null && statefulSetSpec.getReplicas() != null && statefulSetStatus != null
        && statefulSetStatus.getReadyReplicas() != null
        && statefulSetStatus.getReadyReplicas() < statefulSetSpec.getReplicas()) {
      return K8ApiResponseDTO.builder()
          .isDone(false)
          .message(String.format("Waiting for %d pods to be ready...%n",
              statefulSetSpec.getReplicas() - statefulSetStatus.getReadyReplicas()))
          .build();
    }

    if (statefulSetSpec != null && statefulSetSpec.getUpdateStrategy() != null
        && "RollingUpdate".equals(statefulSetSpec.getUpdateStrategy().getType())
        && statefulSetSpec.getUpdateStrategy().getRollingUpdate() != null
        && statefulSetSpec.getUpdateStrategy().getRollingUpdate().getPartition() != null
        && statefulSetSpec.getUpdateStrategy().getRollingUpdate().getPartition() > 0) {
      if (statefulSetSpec.getReplicas() != null
          && statefulSetStatus.getUpdatedReplicas()
              < statefulSetSpec.getReplicas() - statefulSetSpec.getUpdateStrategy().getRollingUpdate().getPartition()) {
        return K8ApiResponseDTO.builder()
            .message(String.format(
                "Waiting for partitioned roll out to finish: %d out of %d new pods have been updated...%n",
                statefulSetStatus.getUpdatedReplicas(),
                statefulSetSpec.getReplicas() - statefulSetSpec.getUpdateStrategy().getRollingUpdate().getPartition()))
            .isDone(false)
            .build();
      }

      return K8ApiResponseDTO.builder()
          .message(String.format("partitioned roll out complete: %d new pods have been updated...%n",
              statefulSetStatus.getUpdatedReplicas()))
          .isDone(true)
          .build();
    }

    if (statefulSetStatus != null && statefulSetStatus.getCurrentRevision() != null
        && statefulSetStatus.getUpdateRevision() != null
        && !statefulSetStatus.getCurrentRevision().equals(statefulSetStatus.getUpdateRevision())) {
      return K8ApiResponseDTO.builder()
          .isDone(false)
          .message(String.format("waiting for statefulset rolling update to complete %d pods at revision %s...%n",
              statefulSetStatus.getUpdatedReplicas(), statefulSetStatus.getUpdateRevision()))
          .build();
    }

    return K8ApiResponseDTO.builder()
        .isDone(true)
        .message(String.format("statefulset rolling update complete %d pods at revision %s...%n",
            statefulSetStatus.getCurrentReplicas(), statefulSetStatus.getCurrentRevision()))
        .build();
  }

  private void initializeNullFieldsInStatefulSetStatus(V1StatefulSetStatus statefulSetStatus) {
    if (statefulSetStatus != null) {
      if (statefulSetStatus.getReadyReplicas() == null) {
        statefulSetStatus.setReadyReplicas(0);
      }

      if (statefulSetStatus.getUpdatedReplicas() == null) {
        statefulSetStatus.setUpdatedReplicas(0);
      }

      if (statefulSetStatus.getCurrentReplicas() == null) {
        statefulSetStatus.setCurrentReplicas(0);
      }
    }
  }
}
