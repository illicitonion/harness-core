/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.jenkins.jenkinsstep;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.task.artifacts.jenkins.JenkinsArtifactDelegateRequest.JenkinsArtifactDelegateRequestBuilder;
import io.harness.delegate.task.artifacts.response.ArtifactTaskResponse;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.supplier.ThrowingSupplier;

@OwnedBy(CDC)
public interface JenkinsBuildStepHelperService {
  TaskRequest prepareTaskRequest(JenkinsArtifactDelegateRequestBuilder paramsBuilder, Ambiance ambiance,
      String connectorRef, String timeStr, String taskName);
  StepResponse prepareStepResponse(ThrowingSupplier<ArtifactTaskResponse> responseSupplier) throws Exception;
}
