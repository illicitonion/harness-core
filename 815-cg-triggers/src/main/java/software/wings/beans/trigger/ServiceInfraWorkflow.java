/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans.trigger;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.WorkflowType;

import lombok.Builder;
import lombok.Data;

@OwnedBy(CDC)
@Data
@Builder
public class ServiceInfraWorkflow {
  private String infraMappingId;
  private String infraMappingName;
  private String workflowId;
  private String workflowName;
  private WorkflowType workflowType;
}
