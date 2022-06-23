/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.engine.pms.execution.strategy.identity;

import static io.harness.steps.StepUtils.createStepResponseFromChildResponse;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.UUIDGenerator;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.executions.plan.PlanService;
import io.harness.engine.pms.steps.identity.IdentityStepParameters;
import io.harness.execution.NodeExecution;
import io.harness.plan.IdentityPlanNode;
import io.harness.plan.Node;
import io.harness.plan.PlanNode;
import io.harness.plancreator.NGCommonUtilPlanCreationConstants;
import io.harness.plancreator.strategy.MatrixConfig;
import io.harness.plancreator.strategy.StrategyConfig;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.ChildExecutableResponse;
import io.harness.pms.contracts.execution.ChildrenExecutableResponse;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.steps.executables.ChildExecutable;
import io.harness.pms.sdk.core.steps.executables.ChildrenExecutable;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;
import io.harness.pms.yaml.ParameterField;
import io.harness.steps.matrix.StrategyStepParameters;
import io.harness.tasks.ResponseData;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@OwnedBy(HarnessTeam.PIPELINE)
public class IdentityStrategyInternalStep
    implements ChildExecutable<IdentityStepParameters>, ChildrenExecutable<IdentityStepParameters> {
  @Inject PlanService planService;
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(NGCommonUtilPlanCreationConstants.IDENTITY_STRATEGY_INTERNAL)
                                               .setStepCategory(StepCategory.STRATEGY)
                                               .build();

  @Inject private NodeExecutionService nodeExecutionService;
  @Override
  public ChildExecutableResponse obtainChild(
      Ambiance ambiance, IdentityStepParameters identityParams, StepInputPackage inputPackage) {
    NodeExecution childNodeExecution =
        nodeExecutionService
            .fetchNodeExecutionsByParentIdWithAmbianceAndNode(identityParams.getOriginalNodeExecutionId(), true)
            .get(0);

    Node node = childNodeExecution.getNode();
    if (node instanceof PlanNode) {
      IdentityPlanNode identityPlanNode = IdentityPlanNode.mapPlanNodeToIdentityNode(UUIDGenerator.generateUuid(), node,
          childNodeExecution.getIdentifier(), childNodeExecution.getName(), node.getStepType(),
          childNodeExecution.getUuid());
      planService.saveIdentityNodesForMatrix(Collections.singletonList(identityPlanNode), ambiance.getPlanId());
      return ChildExecutableResponse.newBuilder().setChildNodeId(identityPlanNode.getUuid()).build();
    }
    NodeExecution originalNodeExecution = nodeExecutionService.get(identityParams.getOriginalNodeExecutionId());
    return originalNodeExecution.getExecutableResponses().get(0).getChild();
  }

  @Override
  public StepResponse handleChildResponse(
      Ambiance ambiance, IdentityStepParameters identityParams, Map<String, ResponseData> responseDataMap) {
    return createStepResponseFromChildResponse(responseDataMap);
  }

  @Override
  public ChildrenExecutableResponse obtainChildren(
      Ambiance ambiance, IdentityStepParameters identityParams, StepInputPackage inputPackage) {
    List<ChildrenExecutableResponse.Child> children = new ArrayList<>();
    List<Node> identityNodesToBeCreated = new ArrayList<>();
    List<NodeExecution> childrenNodeExecutions = nodeExecutionService.fetchNodeExecutionsByParentIdWithAmbianceAndNode(
        identityParams.getOriginalNodeExecutionId(), true);

    NodeExecution strategyNodeExecution = nodeExecutionService.get(identityParams.getOriginalNodeExecutionId());

    for (NodeExecution nodeExecution : childrenNodeExecutions) {
      if (nodeExecution.getNode() instanceof PlanNode) {
        Node node = IdentityPlanNode.mapPlanNodeToIdentityNode(UUIDGenerator.generateUuid(), nodeExecution.getNode(),
            nodeExecution.getIdentifier(), nodeExecution.getName(), nodeExecution.getNode().getStepType(),
            nodeExecution.getUuid());
        children.add(ChildrenExecutableResponse.Child.newBuilder()
                         .setChildNodeId(node.getUuid())
                         .setStrategyMetadata(
                             AmbianceUtils.obtainCurrentLevel(nodeExecution.getAmbiance()).getStrategyMetadata())
                         .build());
        identityNodesToBeCreated.add(node);
      } else {
        children.add(ChildrenExecutableResponse.Child.newBuilder()
                         .setChildNodeId(nodeExecution.getNode().getUuid())
                         .setStrategyMetadata(
                             AmbianceUtils.obtainCurrentLevel(nodeExecution.getAmbiance()).getStrategyMetadata())
                         .build());
      }
    }
    planService.saveIdentityNodesForMatrix(identityNodesToBeCreated, ambiance.getPlanId());
    int maxConcurrency = 0;
    StrategyConfig strategyConfig =
        RecastOrchestrationUtils
            .fromMap(strategyNodeExecution.getResolvedStepParameters(), StrategyStepParameters.class)
            .getStrategyConfig();
    if (strategyConfig.getMatrixConfig() != null
        && !ParameterField.isBlank(((MatrixConfig) strategyConfig.getMatrixConfig()).getMaxConcurrency())) {
      maxConcurrency = ((MatrixConfig) strategyConfig.getMatrixConfig()).getMaxConcurrency().getValue();
    }
    if (strategyConfig.getForConfig() != null
        && !ParameterField.isBlank(strategyConfig.getForConfig().getMaxConcurrency())) {
      maxConcurrency = strategyConfig.getForConfig().getMaxConcurrency().getValue();
    }

    return ChildrenExecutableResponse.newBuilder().addAllChildren(children).setMaxConcurrency(maxConcurrency).build();
  }

  @Override
  public StepResponse handleChildrenResponse(
      Ambiance ambiance, IdentityStepParameters identityParams, Map<String, ResponseData> responseDataMap) {
    return createStepResponseFromChildResponse(responseDataMap);
  }

  @Override
  public Class<IdentityStepParameters> getStepParametersClass() {
    return IdentityStepParameters.class;
  }
}
