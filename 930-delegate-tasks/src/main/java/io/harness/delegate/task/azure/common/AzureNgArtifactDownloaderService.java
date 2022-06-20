/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.common;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.exception.WingsException.USER;

import static java.lang.String.format;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.task.artifacts.ArtifactDownloadRequest;
import io.harness.delegate.task.artifacts.ArtifactDownloadService;
import io.harness.delegate.task.artifacts.ArtifactRequestDetails;
import io.harness.delegate.task.azure.artifact.AzurePackageArtifactConfig;
import io.harness.eraro.ErrorCode;
import io.harness.eraro.Level;
import io.harness.exception.ExceptionUtils;
import io.harness.exception.ExplanationException;
import io.harness.exception.FileCopyException;
import io.harness.exception.FileCreationException;
import io.harness.exception.HintException;
import io.harness.exception.NestedExceptionUtils;
import io.harness.exception.sanitizer.ExceptionMessageSanitizer;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.LogCallback;
import io.harness.logging.LogLevel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

@Slf4j
@Singleton
@OwnedBy(CDP)
public class AzureNgArtifactDownloaderService {
  private static final String ARTIFACT_NAME_PREFIX = "artifact-";

  @Inject private ArtifactDownloadService artifactDownloadService;

  public File downloadArtifact(ArtifactNgDownloadContext downloadContext) {
    final AzurePackageArtifactConfig artifactConfig = downloadContext.getArtifactConfig();
    final ArtifactRequestDetails artifactDetails = artifactConfig.getArtifactDetails();
    final LogCallback logCallback =
        downloadContext.getLogCallbackProvider().obtainLogCallback(downloadContext.getCommandUnitName());

    try {
      logCallback.saveExecutionLog(format("Downloading artifact '%s' from '%s'", artifactDetails.getArtifactName(),
          artifactConfig.getSourceType().getDisplayName()));
      InputStream artifactStream =
          artifactDownloadService.download(ArtifactDownloadRequest.builder()
                                               .artifactSourceType(artifactConfig.getSourceType())
                                               .artifactDetails(artifactConfig.getArtifactDetails())
                                               .connector(artifactConfig.getConnectorConfig())
                                               .build());
      File artifactFile = copyArtifactStreamToWorkingDirectory(downloadContext, artifactStream, logCallback);
      logCallback.saveExecutionLog("Artifact successfully downloaded", LogLevel.INFO, CommandExecutionStatus.SUCCESS);
      return artifactFile;
    } catch (Exception e) {
      logCallback.saveExecutionLog(format("Failed to download artifact '%s' due to: %s",
                                       artifactDetails.getArtifactName(), ExceptionUtils.getMessage(e)),
          LogLevel.ERROR, CommandExecutionStatus.FAILURE);
      throw e;
    }
  }

  private File copyArtifactStreamToWorkingDirectory(
      ArtifactNgDownloadContext downloadContext, InputStream artifactStream, LogCallback logCallback) {
    final ArtifactRequestDetails artifactDetails = downloadContext.getArtifactConfig().getArtifactDetails();
    final File artifactFile = createArtifactFileInWorkingDirectory(downloadContext);
    try (FileOutputStream output = new FileOutputStream(artifactFile)) {
      logCallback.saveExecutionLog(
          format("Copy artifact '%s' to '%s'", artifactDetails.getArtifactName(), artifactFile.getPath()));
      IOUtils.copy(artifactStream, output);
      logCallback.saveExecutionLog(format(
          "Artifact '%s' successfully copied to '%s'", artifactDetails.getArtifactName(), artifactFile.getPath()));
      return artifactFile;
    } catch (IOException exception) {
      throw NestedExceptionUtils.hintWithExplanationException(HintException.HINT_FILE_CREATION_ERROR,
          ExplanationException.EXPLANATION_FILE_CREATION_ERROR,
          new FileCopyException(format("Failed to copy artifact file '%s' from input stream to path '%s' due to: %s",
              artifactDetails.getArtifactName(), artifactFile.getPath(), exception.getMessage())));
    }
  }

  private File createArtifactFileInWorkingDirectory(ArtifactNgDownloadContext downloadContext) {
    final ArtifactRequestDetails artifactDetails = downloadContext.getArtifactConfig().getArtifactDetails();
    final String fileName = ARTIFACT_NAME_PREFIX + System.currentTimeMillis();
    final String filePath =
        Paths.get(downloadContext.getWorkingDirectory().getPath(), fileName).toAbsolutePath().toString();
    final File artifactFile = new File(filePath);

    try {
      if (!artifactFile.createNewFile()) {
        throw NestedExceptionUtils.hintWithExplanationException(HintException.HINT_FILE_CREATION_ERROR,
            ExplanationException.EXPLANATION_FILE_CREATION_ERROR,
            new FileCreationException(format("Failed to create a new file for artifact '%s' using artifact file '%s'",
                                          artifactDetails.getArtifactName(), filePath),
                null, ErrorCode.FILE_CREATE_ERROR, Level.ERROR, USER, null));
      }

      return artifactFile;
    } catch (IOException exception) {
      IOException sanitizedException = ExceptionMessageSanitizer.sanitizeException(exception);
      log.error("Failed to create file {}", filePath, sanitizedException);
      throw NestedExceptionUtils.hintWithExplanationException(HintException.HINT_FILE_CREATION_ERROR,
          ExplanationException.EXPLANATION_FILE_CREATION_ERROR,
          new FileCreationException(format("Failed to create a new file for artifact '%s' using artifact file '%s'",
                                        artifactDetails.getArtifactName(), filePath),
              sanitizedException, ErrorCode.FILE_CREATE_ERROR, Level.ERROR, USER, null));
    }
  }
}
