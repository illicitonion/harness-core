/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans;

import static software.wings.beans.CollectionEntityType.MANIFEST;

import software.wings.service.intfc.WorkflowService;
import software.wings.sm.State;
import software.wings.sm.StateType;
import software.wings.sm.StateTypeDescriptor;
import software.wings.sm.states.ArtifactCollectionState;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ManifestCollectLoopParams implements LoopParams {
  String appManifestId;
  String buildNo;
  String stepName;

  @Override
  public State getEnvStateInstanceFromParams(WorkflowService workflowService, String appId) {
    Map<String, StateTypeDescriptor> stencilMap = workflowService.stencilMap(appId);
    StateTypeDescriptor stateTypeDesc = stencilMap.get(StateType.ARTIFACT_COLLECTION.getType());

    State state = stateTypeDesc.newInstance(stepName);
    Map<String, Object> properties = new HashMap<>();
    properties.put(ArtifactCollectionState.ArtifactCollectionStateKeys.appManifestId, appManifestId);
    properties.put(ArtifactCollectionState.ArtifactCollectionStateKeys.buildNo, buildNo);
    properties.put(ArtifactCollectionState.ArtifactCollectionStateKeys.regex, false);
    properties.put(ArtifactCollectionState.ArtifactCollectionStateKeys.sourceType, MANIFEST);
    state.parseProperties(properties);
    state.resolveProperties();
    return state;
  }
}
