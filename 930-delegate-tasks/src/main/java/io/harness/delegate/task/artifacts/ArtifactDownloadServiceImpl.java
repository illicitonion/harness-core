/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.artifacts;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import static java.lang.String.format;

import io.harness.annotations.dev.OwnedBy;
import io.harness.artifact.ArtifactMetadataKeys;
import io.harness.artifactory.ArtifactoryConfigRequest;
import io.harness.artifactory.ArtifactoryNgService;
import io.harness.delegate.beans.connector.artifactoryconnector.ArtifactoryConnectorDTO;
import io.harness.delegate.task.artifactory.ArtifactoryRequestMapper;
import io.harness.delegate.task.artifacts.artifactory.ArtifactoryArtifactRequestDetails;
import io.harness.exception.InvalidArgumentsException;

import com.google.inject.Inject;
import java.io.InputStream;

@OwnedBy(CDP)
public class ArtifactDownloadServiceImpl implements ArtifactDownloadService {
  @Inject private ArtifactoryNgService artifactoryNgService;
  @Inject private ArtifactoryRequestMapper artifactoryRequestMapper;

  @Override
  public InputStream download(ArtifactDownloadRequest request) {
    switch (request.getArtifactSourceType()) {
      case ARTIFACTORY_REGISTRY:
        return downloadFromArtifactory(request);
      default:
        throw new UnsupportedOperationException(format(
            "Artifact source type [%s] is not downloadable or not supported yet", request.getArtifactSourceType()));
    }
  }

  private InputStream downloadFromArtifactory(ArtifactDownloadRequest request) {
    if (!(request.getConnector() instanceof ArtifactoryConnectorDTO)) {
      throw new InvalidArgumentsException(format("Invalid connector type [%s], expected: [%s]",
          request.getConnector().getClass().getSimpleName(), ArtifactoryConnectorDTO.class.getSimpleName()));
    }

    if (!(request.getArtifactDetails() instanceof ArtifactoryArtifactRequestDetails)) {
      throw new InvalidArgumentsException(format("Invalid artifact details type [%s], expected: [%s]",
          request.getArtifactDetails().getClass().getSimpleName(),
          ArtifactoryArtifactRequestDetails.class.getSimpleName()));
    }

    ArtifactoryConnectorDTO artifactoryConnector = (ArtifactoryConnectorDTO) request.getConnector();
    ArtifactoryArtifactRequestDetails artifactDetails =
        (ArtifactoryArtifactRequestDetails) request.getArtifactDetails();
    ArtifactoryConfigRequest artifactoryConfigRequest =
        artifactoryRequestMapper.toArtifactoryRequest(artifactoryConnector);
    return artifactoryNgService.downloadArtifacts(artifactoryConfigRequest, artifactDetails.getRepository(),
        artifactDetails.toMetadata(), ArtifactMetadataKeys.artifactPath, ArtifactMetadataKeys.artifactName);
  }
}
