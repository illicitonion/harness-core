/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.appservice.webapp.handler;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.azure.model.AzureConstants.AZURE_APP_SVC_ARTIFACT_DOWNLOAD_DIR_PATH;
import static io.harness.azure.model.AzureConstants.FETCH_ARTIFACT_FILE;
import static io.harness.azure.model.AzureConstants.REPOSITORY_DIR_PATH;

import static java.lang.String.format;

import io.harness.annotations.dev.OwnedBy;
import io.harness.azure.context.AzureWebClientContext;
import io.harness.azure.model.AzureConfig;
import io.harness.delegate.task.azure.appservice.AzureAppServicePreDeploymentData;
import io.harness.delegate.task.azure.appservice.deployment.context.AzureAppServiceDockerDeploymentContext;
import io.harness.delegate.task.azure.appservice.deployment.context.AzureAppServicePackageDeploymentContext;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.exception.AzureWebAppSlotDeploymentExceptionData;
import io.harness.delegate.task.azure.appservice.webapp.ng.request.AzureWebAppSlotDeploymentRequest;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppRequestResponse;
import io.harness.delegate.task.azure.appservice.webapp.ng.response.AzureWebAppSlotDeploymentResponse;
import io.harness.delegate.task.azure.appservice.webapp.response.AzureAppDeploymentData;
import io.harness.delegate.task.azure.artifact.AzureArtifactConfig;
import io.harness.delegate.task.azure.artifact.AzurePackageArtifactConfig;
import io.harness.delegate.task.azure.common.ArtifactNgDownloadContext;
import io.harness.delegate.task.azure.common.AutoCloseableWorkingDirectory;
import io.harness.delegate.task.azure.common.AzureLogCallbackProvider;
import io.harness.delegate.task.azure.common.AzureNgArtifactDownloaderService;

import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(CDP)
@Slf4j
public class AzureWebAppSlotDeploymentRequestHandler
    extends AbstractSlotDataRequestHandler<AzureWebAppSlotDeploymentRequest> {
  private AzureNgArtifactDownloaderService artifactDownloaderService;

  @Override
  protected AzureWebAppRequestResponse execute(AzureWebAppSlotDeploymentRequest taskRequest, AzureConfig azureConfig,
      AzureLogCallbackProvider logCallbackProvider) {
    AzureArtifactConfig artifactConfig = taskRequest.getArtifact();
    switch (artifactConfig.getArtifactType()) {
      case CONTAINER:
        return executeContainer(taskRequest, azureConfig, logCallbackProvider);
      case PACKAGE:
        return executePackage(taskRequest, azureConfig, logCallbackProvider);
      default:
        throw new UnsupportedOperationException(
            format("Artifact type [%s] is not supported yet", artifactConfig.getArtifactType()));
    }
  }

  @Override
  protected Class<AzureWebAppSlotDeploymentRequest> getRequestType() {
    return AzureWebAppSlotDeploymentRequest.class;
  }

  private AzureWebAppRequestResponse executeContainer(AzureWebAppSlotDeploymentRequest taskRequest,
      AzureConfig azureConfig, AzureLogCallbackProvider logCallbackProvider) {
    AzureWebClientContext azureWebClientContext =
        buildAzureWebClientContext(taskRequest.getInfrastructure(), azureConfig);
    AzureWebAppInfraDelegateConfig infrastructure = taskRequest.getInfrastructure();
    AzureAppServiceDockerDeploymentContext dockerDeploymentContext =
        toAzureAppServiceDockerDeploymentContext(taskRequest, azureConfig, azureWebClientContext, logCallbackProvider);
    AzureAppServicePreDeploymentData preDeploymentData = taskRequest.getPreDeploymentData();

    try {
      azureAppServiceDeploymentService.deployDockerImage(dockerDeploymentContext, preDeploymentData);
      List<AzureAppDeploymentData> azureAppDeploymentData =
          azureAppServiceService.fetchDeploymentData(azureWebClientContext, infrastructure.getDeploymentSlot());

      return AzureWebAppSlotDeploymentResponse.builder()
          .azureAppDeploymentData(azureAppDeploymentData)
          .deploymentProgressMarker(preDeploymentData.getDeploymentProgressMarker())
          .build();
    } catch (Exception e) {
      throw new AzureWebAppSlotDeploymentExceptionData(preDeploymentData.getDeploymentProgressMarker(), e);
    }
  }

  private AzureWebAppRequestResponse executePackage(AzureWebAppSlotDeploymentRequest taskRequest,
      AzureConfig azureConfig, AzureLogCallbackProvider logCallbackProvider) {
    AzureAppServicePreDeploymentData preDeploymentData = taskRequest.getPreDeploymentData();

    try (AutoCloseableWorkingDirectory autoCloseableWorkingDirectory =
             new AutoCloseableWorkingDirectory(REPOSITORY_DIR_PATH, AZURE_APP_SVC_ARTIFACT_DOWNLOAD_DIR_PATH)) {
      AzurePackageArtifactConfig artifactConfig = (AzurePackageArtifactConfig) taskRequest.getArtifact();
      ArtifactNgDownloadContext downloadContext =
          toArtifactNgDownloadContext(artifactConfig, autoCloseableWorkingDirectory, logCallbackProvider);
      File artifactFile = artifactDownloaderService.downloadArtifact(downloadContext);
      AzureWebClientContext azureWebClientContext =
          buildAzureWebClientContext(taskRequest.getInfrastructure(), azureConfig);

      AzureAppServicePackageDeploymentContext packageDeploymentContext = toAzureAppServicePackageDeploymentContext(
          taskRequest, azureWebClientContext, artifactFile, logCallbackProvider);
      azureAppServiceDeploymentService.deployPackage(packageDeploymentContext, preDeploymentData);
      List<AzureAppDeploymentData> azureAppDeploymentData = azureAppServiceService.fetchDeploymentData(
          azureWebClientContext, taskRequest.getInfrastructure().getDeploymentSlot());

      return AzureWebAppSlotDeploymentResponse.builder()
          .azureAppDeploymentData(azureAppDeploymentData)
          .deploymentProgressMarker(preDeploymentData.getDeploymentProgressMarker())
          .build();
    } catch (Exception e) {
      throw new AzureWebAppSlotDeploymentExceptionData(preDeploymentData.getDeploymentProgressMarker(), e);
    }
  }

  private ArtifactNgDownloadContext toArtifactNgDownloadContext(AzurePackageArtifactConfig artifactConfig,
      AutoCloseableWorkingDirectory workingDirectory, AzureLogCallbackProvider logCallbackProvider) {
    return ArtifactNgDownloadContext.builder()
        .artifactConfig(artifactConfig)
        .commandUnitName(FETCH_ARTIFACT_FILE)
        .workingDirectory(workingDirectory.workingDir())
        .logCallbackProvider(logCallbackProvider)
        .build();
  }
}
