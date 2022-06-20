/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.appservice.webapp.handler;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.azure.context.AzureWebClientContext;
import io.harness.azure.model.AzureAppServiceApplicationSetting;
import io.harness.azure.model.AzureAppServiceConnectionString;
import io.harness.azure.model.AzureConfig;
import io.harness.azure.utility.AzureResourceUtility;
import io.harness.delegate.task.azure.appservice.AzureAppServiceResourceUtilities;
import io.harness.delegate.task.azure.appservice.deployment.context.AzureAppServiceDockerDeploymentContext;
import io.harness.delegate.task.azure.appservice.deployment.context.AzureAppServicePackageDeploymentContext;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.request.AbstractSlotDataRequest;
import io.harness.delegate.task.azure.artifact.AzureContainerArtifactConfig;
import io.harness.delegate.task.azure.common.AzureLogCallbackProvider;

import software.wings.utils.ArtifactType;

import com.google.inject.Inject;
import java.io.File;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(CDP)
public abstract class AbstractSlotDataRequestHandler<T extends AbstractSlotDataRequest>
    extends AzureWebAppRequestHandler<T> {
  @Inject protected AzureAppServiceResourceUtilities azureResourceUtilities;

  protected AzureAppServiceDockerDeploymentContext toAzureAppServiceDockerDeploymentContext(T taskRequest,
      AzureConfig azureConfig, AzureWebClientContext clientContext, AzureLogCallbackProvider logCallbackProvider) {
    AzureContainerArtifactConfig artifactConfig = (AzureContainerArtifactConfig) taskRequest.getArtifact();
    AzureWebAppInfraDelegateConfig infrastructure = taskRequest.getInfrastructure();
    Map<String, AzureAppServiceApplicationSetting> appSettingsToAdd =
        azureResourceUtilities.getAppSettingsToAdd(taskRequest.getApplicationSettings());
    Map<String, AzureAppServiceConnectionString> connSettingsToAdd =
        azureResourceUtilities.getConnectionSettingsToAdd(taskRequest.getConnectionStrings());
    Map<String, AzureAppServiceApplicationSetting> dockerSettings = azureResourceUtilities.getDockerSettings(
        artifactConfig.getConnectorConfig(), artifactConfig.getRegistryType(), azureConfig);

    String imagePathAndTag =
        AzureResourceUtility.getDockerImageFullNameAndTag(artifactConfig.getImage(), artifactConfig.getTag());

    return AzureAppServiceDockerDeploymentContext.builder()
        .logCallbackProvider(logCallbackProvider)
        .startupCommand(taskRequest.getStartupCommand())
        .slotName(infrastructure.getDeploymentSlot())
        .azureWebClientContext(clientContext)
        .appSettingsToAdd(appSettingsToAdd)
        .connSettingsToAdd(connSettingsToAdd)
        .dockerSettings(dockerSettings)
        .imagePathAndTag(imagePathAndTag)
        .steadyStateTimeoutInMin(azureResourceUtilities.getTimeoutIntervalInMin(taskRequest.getTimeoutIntervalInMin()))
        .skipTargetSlotValidation(true)
        .build();
  }

  protected AzureAppServicePackageDeploymentContext toAzureAppServicePackageDeploymentContext(T taskRequest,
      AzureWebClientContext clientContext, File artifactFile, AzureLogCallbackProvider logCallbackProvider) {
    Map<String, AzureAppServiceApplicationSetting> appSettingsToAdd =
        azureResourceUtilities.getAppSettingsToAdd(taskRequest.getApplicationSettings());
    Map<String, AzureAppServiceConnectionString> connSettingsToAdd =
        azureResourceUtilities.getConnectionSettingsToAdd(taskRequest.getConnectionStrings());

    return AzureAppServicePackageDeploymentContext.builder()
        .logCallbackProvider(logCallbackProvider)
        .appSettingsToAdd(appSettingsToAdd)
        .connSettingsToAdd(connSettingsToAdd)
        .slotName(taskRequest.getInfrastructure().getDeploymentSlot())
        .azureWebClientContext(clientContext)
        .startupCommand(taskRequest.getStartupCommand())
        .artifactFile(artifactFile)
        .artifactType(ArtifactType.ZIP) // TODO: check how we can get artifact type
        .steadyStateTimeoutInMin(azureResourceUtilities.getTimeoutIntervalInMin(taskRequest.getTimeoutIntervalInMin()))
        .build();
  }
}
