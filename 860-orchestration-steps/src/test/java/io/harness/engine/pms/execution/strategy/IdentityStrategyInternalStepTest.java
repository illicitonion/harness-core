/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.engine.pms.execution.strategy;

import static io.harness.rule.OwnerRule.BRIJESH;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.harness.CategoryTest;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.executions.plan.PlanService;
import io.harness.engine.pms.execution.strategy.identity.IdentityStrategyInternalStep;
import io.harness.engine.pms.steps.identity.IdentityStepParameters;
import io.harness.execution.NodeExecution;
import io.harness.persistence.UuidAccess;
import io.harness.plan.IdentityPlanNode;
import io.harness.plan.Node;
import io.harness.plan.PlanNode;
import io.harness.plancreator.strategy.HarnessForConfig;
import io.harness.plancreator.strategy.MatrixConfig;
import io.harness.plancreator.strategy.StrategyConfig;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.ChildrenExecutableResponse;
import io.harness.pms.contracts.execution.ExecutableResponse;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.StrategyMetadata;
import io.harness.pms.data.stepparameters.PmsStepParameters;
import io.harness.pms.plan.execution.SetupAbstractionKeys;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;
import io.harness.pms.yaml.ParameterField;
import io.harness.rule.Owner;
import io.harness.steps.StepUtils;
import io.harness.steps.matrix.StrategyStepParameters;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;

@OwnedBy(HarnessTeam.PIPELINE)
@PrepareForTest(StepUtils.class)
public class IdentityStrategyInternalStepTest extends CategoryTest {
  @Mock private NodeExecutionService nodeExecutionService;
  @Mock private PlanService planService;
  @Inject @InjectMocks private IdentityStrategyInternalStep identityStrategyInternalStep;

  private Ambiance buildAmbiance() {
    return Ambiance.newBuilder()
        .putSetupAbstractions(SetupAbstractionKeys.accountId, "accId")
        .putSetupAbstractions(SetupAbstractionKeys.orgIdentifier, "orgId")
        .putSetupAbstractions(SetupAbstractionKeys.projectIdentifier, "projId")
        .addLevels(Level.newBuilder().setStrategyMetadata(StrategyMetadata.newBuilder().build()).build())
        .build();
  }

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testObtainChildren() {
    String originalNodeExecutionId = "originalNodeExecutionId";
    Ambiance oldAmbiance = buildAmbiance();
    Ambiance ambiance = Ambiance.newBuilder().setPlanId("planId").build();

    IdentityStepParameters stepParameters =
        IdentityStepParameters.builder().originalNodeExecutionId(originalNodeExecutionId).build();
    List<NodeExecution> childrenNodeExecutions = new ArrayList<>();
    childrenNodeExecutions.add(NodeExecution.builder()
                                   .ambiance(oldAmbiance)
                                   .status(Status.SUCCEEDED)
                                   .planNode(PlanNode.builder().uuid("planUuid1").build())
                                   .build());
    childrenNodeExecutions.add(NodeExecution.builder()
                                   .ambiance(oldAmbiance)
                                   .status(Status.SUCCEEDED)
                                   .planNode(PlanNode.builder().uuid("planUuid2").build())
                                   .build());
    childrenNodeExecutions.add(NodeExecution.builder()
                                   .ambiance(oldAmbiance)
                                   .planNode(IdentityPlanNode.builder().uuid("identityUuid1").build())
                                   .status(Status.SUCCEEDED)
                                   .build());
    childrenNodeExecutions.add(NodeExecution.builder()
                                   .ambiance(oldAmbiance)
                                   .planNode(IdentityPlanNode.builder().uuid("identityUuid2").build())
                                   .status(Status.SUCCEEDED)
                                   .build());

    doReturn(childrenNodeExecutions)
        .when(nodeExecutionService)
        .fetchNodeExecutionsByParentIdWithAmbianceAndNode(originalNodeExecutionId, true);

    NodeExecution strategyNodeExecution =
        NodeExecution.builder()
            .resolvedParams(PmsStepParameters.parse(RecastOrchestrationUtils.toMap(
                StrategyStepParameters.builder().strategyConfig(StrategyConfig.builder().build()).build())))
            .executableResponse(
                ExecutableResponse.newBuilder()
                    .setChildren(
                        ChildrenExecutableResponse.newBuilder()
                            .addChildren(
                                ChildrenExecutableResponse.Child.newBuilder().setChildNodeId("childId").build())
                            .build())
                    .build())
            .build();
    doReturn(strategyNodeExecution).when(nodeExecutionService).get(originalNodeExecutionId);

    ArgumentCaptor<List> identityNodesCaptor = ArgumentCaptor.forClass(List.class);

    ChildrenExecutableResponse response = identityStrategyInternalStep.obtainChildren(ambiance, stepParameters, null);

    assertEquals(response.getChildrenCount(), childrenNodeExecutions.size());
    assertEquals(response.getMaxConcurrency(), 0);
    verify(planService, times(1)).saveIdentityNodesForMatrix(identityNodesCaptor.capture(), any());
    assertChildrenResponse(response, identityNodesCaptor.getValue(), childrenNodeExecutions);

    strategyNodeExecution =
        NodeExecution.builder()
            .resolvedParams(PmsStepParameters.parse(RecastOrchestrationUtils.toMap(
                StrategyStepParameters.builder()
                    .strategyConfig(
                        StrategyConfig.builder()
                            .matrixConfig(MatrixConfig.builder()
                                              .maxConcurrency(ParameterField.<Integer>builder().value(2).build())
                                              .build())
                            .build())
                    .build())))
            .executableResponse(
                ExecutableResponse.newBuilder()
                    .setChildren(
                        ChildrenExecutableResponse.newBuilder()
                            .addChildren(
                                ChildrenExecutableResponse.Child.newBuilder().setChildNodeId("childId").build())
                            .build())
                    .build())
            .build();
    doReturn(strategyNodeExecution).when(nodeExecutionService).get(originalNodeExecutionId);
    response = identityStrategyInternalStep.obtainChildren(ambiance, stepParameters, null);
    assertEquals(response.getChildrenCount(), childrenNodeExecutions.size());
    assertEquals(response.getMaxConcurrency(), 2);
    verify(planService, times(2)).saveIdentityNodesForMatrix(identityNodesCaptor.capture(), any());
    assertChildrenResponse(response, identityNodesCaptor.getValue(), childrenNodeExecutions);

    strategyNodeExecution =
        NodeExecution.builder()
            .resolvedParams(PmsStepParameters.parse(RecastOrchestrationUtils.toMap(
                StrategyStepParameters.builder()
                    .strategyConfig(
                        StrategyConfig.builder()
                            .forConfig(HarnessForConfig.builder()
                                           .maxConcurrency(ParameterField.<Integer>builder().value(4).build())
                                           .build())
                            .build())
                    .build())))
            .executableResponse(
                ExecutableResponse.newBuilder()
                    .setChildren(
                        ChildrenExecutableResponse.newBuilder()
                            .addChildren(
                                ChildrenExecutableResponse.Child.newBuilder().setChildNodeId("childId").build())
                            .build())
                    .build())
            .build();
    doReturn(strategyNodeExecution).when(nodeExecutionService).get(originalNodeExecutionId);
    response = identityStrategyInternalStep.obtainChildren(ambiance, stepParameters, null);
    assertEquals(response.getChildrenCount(), childrenNodeExecutions.size());
    assertEquals(response.getMaxConcurrency(), 4);
    verify(planService, times(3)).saveIdentityNodesForMatrix(identityNodesCaptor.capture(), any());
    assertChildrenResponse(response, identityNodesCaptor.getValue(), childrenNodeExecutions);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testHandleChildrenResponse() {
    StepResponse stepResponse = identityStrategyInternalStep.handleChildrenResponse(null, null, new HashMap<>());
    assertNotNull(stepResponse);
  }

  private void assertChildrenResponse(ChildrenExecutableResponse childrenExecutableResponse, List<Node> identityNodes,
      List<NodeExecution> childrenNodeExecutions) {
    List<String> nodeIds = identityNodes.stream().map(UuidAccess::getUuid).collect(Collectors.toList());
    long planNodesCount = childrenNodeExecutions.stream().filter(o -> o.getNode() instanceof PlanNode).count();
    int identityNodesCount = 0;
    for (ChildrenExecutableResponse.Child child : childrenExecutableResponse.getChildrenList()) {
      if (!child.getChildNodeId().equals("childId")) {
        identityNodesCount++;
        assertTrue(nodeIds.contains(child.getChildNodeId()));
      }
    }
    assertEquals(identityNodesCount, nodeIds.size());
    assertEquals(planNodesCount, identityNodesCount);
  }
}
