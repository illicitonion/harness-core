/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.artifacts;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.rule.OwnerRule.ABOSII;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.artifact.ArtifactMetadataKeys;
import io.harness.artifactory.ArtifactoryConfigRequest;
import io.harness.artifactory.ArtifactoryNgService;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryConnectorDTO;
import io.harness.delegate.task.artifactory.ArtifactoryRequestMapper;
import io.harness.delegate.task.artifacts.artifactory.ArtifactoryArtifactRequestDetails;
import io.harness.rule.Owner;

import java.io.InputStream;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@OwnedBy(CDP)
public class ArtifactDownloadServiceImplTest extends CategoryTest {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private ArtifactoryConnectorDTO artifactoryConnector;
  @Mock private InputStream artifactStream;

  @Mock private ArtifactoryNgService artifactoryNgService;
  @Mock private ArtifactoryRequestMapper artifactoryRequestMapper;

  @InjectMocks private ArtifactDownloadServiceImpl artifactDownloadService;

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testDownloadUnsupportedArtifactType() {
    final ArtifactDownloadRequest request =
        ArtifactDownloadRequest.builder().artifactSourceType(ArtifactSourceType.GCR).build();

    assertThatThrownBy(() -> artifactDownloadService.download(request))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testDownloadArtifactory() {
    final ArtifactoryConfigRequest artifactoryConfigRequest = ArtifactoryConfigRequest.builder().build();
    final ArtifactoryArtifactRequestDetails artifactRequestDetails =
        ArtifactoryArtifactRequestDetails.builder()
            .repository("repository")
            .artifactPaths(Collections.singletonList("artifact"))
            .build();
    final ArtifactDownloadRequest request =
        ArtifactDownloadRequest.builder()
            .connector(artifactoryConnector)
            .artifactSourceType(ArtifactSourceType.ARTIFACTORY_REGISTRY)
            .artifactDetails(ArtifactoryArtifactRequestDetails.builder()
                                 .artifactPaths(Collections.singletonList("artifact"))
                                 .repository("repository")
                                 .build())
            .build();

    doReturn(artifactoryConfigRequest).when(artifactoryRequestMapper).toArtifactoryRequest(artifactoryConnector);
    doReturn(artifactStream)
        .when(artifactoryNgService)
        .downloadArtifacts(artifactoryConfigRequest, "repository", artifactRequestDetails.toMetadata(),
            ArtifactMetadataKeys.artifactPath, ArtifactMetadataKeys.artifactName);

    InputStream result = artifactDownloadService.download(request);

    assertThat(result).isSameAs(artifactStream);
  }
}