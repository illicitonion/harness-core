/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.common;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.azure.model.AzureConstants.FETCH_ARTIFACT_FILE;
import static io.harness.rule.OwnerRule.ABOSII;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.task.artifacts.ArtifactDownloadRequest;
import io.harness.delegate.task.artifacts.ArtifactDownloadService;
import io.harness.delegate.task.artifacts.ArtifactRequestDetails;
import io.harness.delegate.task.artifacts.ArtifactSourceType;
import io.harness.delegate.task.azure.artifact.AzurePackageArtifactConfig;
import io.harness.exception.ExplanationException;
import io.harness.exception.FileCopyException;
import io.harness.exception.HintException;
import io.harness.logging.LogCallback;
import io.harness.rule.Owner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@OwnedBy(CDP)
public class AzureNgArtifactDownloaderServiceTest extends CategoryTest {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private ConnectorConfigDTO connector;
  @Mock private ArtifactRequestDetails artifactDetails;
  @Mock private LogCallback logCallback;
  @Mock private AzureLogCallbackProvider logCallbackProvider;

  @Mock private ArtifactDownloadService artifactDownloadService;

  @InjectMocks private AzureNgArtifactDownloaderService azureArtifactDownloaderService;

  private final InputStream artifactStream = new ByteArrayInputStream("artifact".getBytes(StandardCharsets.UTF_8));

  @Before
  public void setup() {
    doReturn(logCallback).when(logCallbackProvider).obtainLogCallback(FETCH_ARTIFACT_FILE);
    doReturn(artifactStream).when(artifactDownloadService).download(any(ArtifactDownloadRequest.class));
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testDownloadArtifact() throws IOException {
    try (AutoCloseableWorkingDirectory autoCloseableWorkingDirectory =
             new AutoCloseableWorkingDirectory("./azure-artifacts/test/", "./azure-artifacts/")) {
      final File workingDirectory = autoCloseableWorkingDirectory.workingDir();
      final ArtifactNgDownloadContext downloadContext =
          ArtifactNgDownloadContext.builder()
              .artifactConfig(AzurePackageArtifactConfig.builder()
                                  .connectorConfig(connector)
                                  .artifactDetails(artifactDetails)
                                  .sourceType(ArtifactSourceType.ARTIFACTORY_REGISTRY)
                                  .build())
              .logCallbackProvider(logCallbackProvider)
              .commandUnitName(FETCH_ARTIFACT_FILE)
              .workingDirectory(workingDirectory)
              .build();

      File artifactFile = azureArtifactDownloaderService.downloadArtifact(downloadContext);

      validateArtifactFileContent(artifactFile, "artifact");
      assertThat(artifactFile.getAbsolutePath()).contains(workingDirectory.getAbsolutePath() + "/artifact-");
    }
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testDownloadArtifactInvalidArtifactStream() {
    try (AutoCloseableWorkingDirectory autoCloseableWorkingDirectory =
             new AutoCloseableWorkingDirectory("./azure-artifacts/test/", "./azure-artifacts/")) {
      final File workingDirectory = autoCloseableWorkingDirectory.workingDir();
      final ArtifactNgDownloadContext downloadContext =
          ArtifactNgDownloadContext.builder()
              .artifactConfig(AzurePackageArtifactConfig.builder()
                                  .connectorConfig(connector)
                                  .artifactDetails(artifactDetails)
                                  .sourceType(ArtifactSourceType.ARTIFACTORY_REGISTRY)
                                  .build())
              .logCallbackProvider(logCallbackProvider)
              .commandUnitName(FETCH_ARTIFACT_FILE)
              .workingDirectory(workingDirectory)
              .build();
      final InvalidArtifactStream invalidArtifactStream = new InvalidArtifactStream();

      doReturn(invalidArtifactStream).when(artifactDownloadService).download(any(ArtifactDownloadRequest.class));

      assertThatThrownBy(() -> azureArtifactDownloaderService.downloadArtifact(downloadContext)).matches(exception -> {
        assertThat(exception).isInstanceOf(HintException.class);
        HintException hintException = (HintException) exception;
        assertThat(hintException.getCause()).isInstanceOf(ExplanationException.class);
        ExplanationException explanationException = (ExplanationException) hintException.getCause();
        assertThat(explanationException.getCause()).isInstanceOf(FileCopyException.class);
        return true;
      });
    }
  }

  private void validateArtifactFileContent(File artifactFile, String expectedContent) throws IOException {
    try (FileInputStream fio = new FileInputStream(artifactFile)) {
      String artifactFileContent = IOUtils.toString(fio, StandardCharsets.UTF_8);
      assertThat(artifactFileContent).isEqualTo(expectedContent);
    }
  }

  private static class InvalidArtifactStream extends InputStream {
    @Override
    public int read() throws IOException {
      throw new IOException("I/O Exception");
    }
  }
}