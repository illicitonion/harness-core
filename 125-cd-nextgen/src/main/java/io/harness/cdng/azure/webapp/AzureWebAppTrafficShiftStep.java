/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.azure.webapp;

import static io.harness.azure.model.AzureConstants.SLOT_TRAFFIC_PERCENTAGE;

import io.harness.EntityType;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.IdentifierRef;
import io.harness.cdng.CDStepHelper;
import io.harness.cdng.azure.AzureHelperService;
import io.harness.cdng.infra.beans.AzureWebAppInfrastructureOutcome;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.connector.azureconnector.AzureConnectorDTO;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.request.AzureWebAppTrafficShiftRequest;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppTaskResponse;
import io.harness.exception.ExceptionUtils;
import io.harness.exception.InvalidArgumentsException;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.logging.UnitProgress;
import io.harness.ng.core.BaseNGAccess;
import io.harness.ng.core.EntityDetail;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.plancreator.steps.common.rollback.TaskExecutableWithRollbackAndRbac;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.rbac.PipelineRbacHelper;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.StepHelper;
import io.harness.steps.StepUtils;
import io.harness.supplier.ThrowingSupplier;
import io.harness.utils.IdentifierRefHelper;

import software.wings.beans.TaskType;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CDP)
@Slf4j
public class AzureWebAppTrafficShiftStep extends TaskExecutableWithRollbackAndRbac<AzureWebAppTaskResponse> {
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(ExecutionNodeType.AZURE_TRAFFIC_SHIFT.getYamlType())
                                               .setStepCategory(StepCategory.STEP)
                                               .build();

  @Inject private CDStepHelper cdStepHelper;
  @Inject private KryoSerializer kryoSerializer;
  @Inject private StepHelper stepHelper;
  @Inject private AzureHelperService azureHelperService;
  @Inject private PipelineRbacHelper pipelineRbacHelper;

  @Override
  public void validateResources(Ambiance ambiance, StepElementParameters stepParameters) {
    List<EntityDetail> entityDetailList = new ArrayList<>();
    String accountId = AmbianceUtils.getAccountId(ambiance);
    String orgIdentifier = AmbianceUtils.getOrgIdentifier(ambiance);
    String projectIdentifier = AmbianceUtils.getProjectIdentifier(ambiance);

    AzureWebAppInfrastructureOutcome azureWebAppInfrastructureOutcome =
        (AzureWebAppInfrastructureOutcome) cdStepHelper.getInfrastructureOutcome(ambiance);

    IdentifierRef identifierRef = IdentifierRefHelper.getIdentifierRef(
        azureWebAppInfrastructureOutcome.getConnectorRef(), accountId, orgIdentifier, projectIdentifier);

    EntityDetail entityDetail = EntityDetail.builder().type(EntityType.CONNECTORS).entityRef(identifierRef).build();
    entityDetailList.add(entityDetail);
    pipelineRbacHelper.checkRuntimePermissions(ambiance, entityDetailList, true);
  }

  @Override
  public TaskRequest obtainTaskAfterRbac(
      Ambiance ambiance, StepElementParameters stepParameters, StepInputPackage inputPackage) {
    String accountId = AmbianceUtils.getAccountId(ambiance);
    String orgIdentifier = AmbianceUtils.getOrgIdentifier(ambiance);
    String projectIdentifier = AmbianceUtils.getProjectIdentifier(ambiance);

    AzureWebAppInfrastructureOutcome azureWebAppInfraOutcome =
        (AzureWebAppInfrastructureOutcome) cdStepHelper.getInfrastructureOutcome(ambiance);

    IdentifierRef identifierRef = IdentifierRefHelper.getIdentifierRef(
        azureWebAppInfraOutcome.getConnectorRef(), accountId, orgIdentifier, projectIdentifier);

    AzureConnectorDTO connectorDTO = azureHelperService.getConnector(identifierRef);
    BaseNGAccess baseNGAccess =
        azureHelperService.getBaseNGAccess(identifierRef.getAccountIdentifier(), orgIdentifier, projectIdentifier);

    AzureWebAppTrafficShiftStepParameters azureWebAppTrafficShiftStepParameters =
        (AzureWebAppTrafficShiftStepParameters) stepParameters.getSpec();

    AzureWebAppInfraDelegateConfig infrastructure =
        AzureWebAppInfraDelegateConfig.builder()
            .appName(azureWebAppInfraOutcome.getWebApp())
            .deploymentSlot(azureWebAppInfraOutcome.getDeploymentSlot())
            .subscription(azureWebAppInfraOutcome.getSubscription())
            .resourceGroup(azureWebAppInfraOutcome.getResourceGroup())
            .encryptionDataDetails(azureHelperService.getEncryptionDetails(connectorDTO, baseNGAccess))
            .azureConnectorDTO(connectorDTO)
            .build();

    AzureWebAppTrafficShiftRequest azureWebAppTrafficShiftRequest =
        AzureWebAppTrafficShiftRequest.builder()
            .trafficPercentage(parseTrafficPercentage(azureWebAppTrafficShiftStepParameters.getTraffic().getValue()))
            .infrastructure(infrastructure)
            .build();

    TaskData taskData = TaskData.builder()
                            .async(true)
                            .taskType(TaskType.AZURE_WEB_APP_TASK_NG.name())
                            .timeout(CDStepHelper.getTimeoutInMillis(stepParameters))
                            .parameters(new Object[] {azureWebAppTrafficShiftRequest})
                            .build();

    return StepUtils.prepareCDTaskRequest(ambiance, taskData, kryoSerializer,
        Collections.singletonList(SLOT_TRAFFIC_PERCENTAGE), TaskType.AZURE_WEB_APP_TASK_NG.getDisplayName(),
        TaskSelectorYaml.toTaskSelector(azureWebAppTrafficShiftStepParameters.getDelegateSelectors()),
        stepHelper.getEnvironmentType(ambiance));
  }

  @Override
  public StepResponse handleTaskResultWithSecurityContext(Ambiance ambiance, StepElementParameters stepParameters,
      ThrowingSupplier<AzureWebAppTaskResponse> responseDataSupplier) throws Exception {
    StepResponse.StepResponseBuilder builder = StepResponse.builder();

    AzureWebAppTaskResponse response;
    try {
      response = responseDataSupplier.get();
    } catch (Exception ex) {
      log.error("Error while processing Azure WebApp Traffic Shift response: {}", ExceptionUtils.getMessage(ex), ex);
      throw ex;
    }
    List<UnitProgress> unitProgresses = response.getCommandUnitsProgress().getUnitProgresses() == null
        ? Collections.emptyList()
        : response.getCommandUnitsProgress().getUnitProgresses();
    builder.unitProgressList(unitProgresses);
    builder.status(Status.SUCCEEDED);
    return builder.build();
  }

  @Override
  public Class<StepElementParameters> getStepParametersClass() {
    return StepElementParameters.class;
  }

  private double parseTrafficPercentage(String trafficPercentage) {
    try {
      return Double.parseDouble(trafficPercentage);
    } catch (NumberFormatException ex) {
      throw new InvalidArgumentsException("Failed to parse traffic percentage");
    }
  }
}
