/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.k8s.steadystate.statusviewer;

import io.harness.k8s.steadystate.model.K8ApiResponseDTO;

import com.google.inject.Singleton;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobCondition;
import io.kubernetes.client.openapi.models.V1JobStatus;
import java.util.List;

@Singleton
public class JobStatusViewer {
  public K8ApiResponseDTO extractRolloutStatus(V1Job job) {
    V1JobStatus jobStatus = job.getStatus();
    String jobStatusString = "";

    if (jobStatus != null) {
      jobStatusString = jobStatus.toString();

      List<V1JobCondition> jobConditions = jobStatus.getConditions();
      if (jobConditions != null) {
        if (jobConditions.stream().anyMatch(
                condition -> condition.getType().equals("Failed") && condition.getStatus().equals("True"))) {
          return K8ApiResponseDTO.builder()
              .isFailed(true)
              .message(String.format(" Job failed. Status: %s", jobStatusString))
              .build();
        }

        if (jobConditions.stream().anyMatch(
                condition -> condition.getType().equals("Complete") && condition.getStatus().equals("True"))
            && jobStatus.getCompletionTime() != null) {
          return K8ApiResponseDTO.builder()
              .isDone(true)
              .message(String.format("Successfully completed Job with status: %s %n", jobStatusString))
              .build();
        }
      }
    }

    return K8ApiResponseDTO.builder()
        .isDone(false)
        .message(String.format("Waiting for job to complete. %nCurrent Job Status: %s %n", jobStatusString))
        .build();
  }
}
