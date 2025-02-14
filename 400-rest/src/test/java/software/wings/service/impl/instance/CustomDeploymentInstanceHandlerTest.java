/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.service.impl.instance;

import static io.harness.beans.EnvironmentType.NON_PROD;
import static io.harness.rule.OwnerRule.RISHABH;
import static io.harness.rule.OwnerRule.YOGESH;

import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.ARTIFACT_ID;
import static software.wings.utils.WingsTestConstants.ARTIFACT_NAME;
import static software.wings.utils.WingsTestConstants.ENV_ID;
import static software.wings.utils.WingsTestConstants.INFRA_MAPPING_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;
import static software.wings.utils.WingsTestConstants.TEMPLATE_ID;
import static software.wings.utils.WingsTestConstants.WORKFLOW_EXECUTION_ID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.harness.beans.ExecutionStatus;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import io.harness.serializer.JsonUtils;

import software.wings.WingsBaseTest;
import software.wings.api.CustomDeploymentTypeInfo;
import software.wings.api.DeploymentSummary;
import software.wings.api.shellscript.provision.ShellScriptProvisionExecutionData;
import software.wings.beans.Application;
import software.wings.beans.CustomInfrastructureMapping;
import software.wings.beans.Environment;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.InfrastructureMappingType;
import software.wings.beans.Service;
import software.wings.beans.artifact.Artifact;
import software.wings.beans.infrastructure.instance.Instance;
import software.wings.beans.infrastructure.instance.InstanceType;
import software.wings.beans.infrastructure.instance.info.PhysicalHostInstanceInfo;
import software.wings.beans.infrastructure.instance.key.HostInstanceKey;
import software.wings.beans.infrastructure.instance.key.deployment.CustomDeploymentKey;
import software.wings.beans.template.deploymenttype.CustomDeploymentTypeTemplate;
import software.wings.service.CustomDeploymentInstanceSyncPTCreator;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.InfrastructureMappingService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.customdeployment.CustomDeploymentTypeService;
import software.wings.service.intfc.instance.InstanceService;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class CustomDeploymentInstanceHandlerTest extends WingsBaseTest {
  @Mock private CustomDeploymentInstanceSyncPTCreator perpetualTaskCreator;
  @Mock private CustomDeploymentTypeService customDeploymentTypeService;
  @Mock private InfrastructureMappingService infraMappingService;
  @Mock private InstanceService instanceService;
  @Mock private AppService appService;
  @Mock private EnvironmentService environmentService;
  @Mock private ServiceResourceService serviceResourceService;
  @Spy private InstanceUtils instanceUtil;

  @InjectMocks private CustomDeploymentInstanceHandler handler;

  ArgumentCaptor<Instance> captor;
  ArgumentCaptor<Set> stringArgumentCaptor;

  @Before
  public void setUp() {
    doReturn(buildInfraMapping()).when(infraMappingService).get(APP_ID, INFRA_MAPPING_ID);
    captor = ArgumentCaptor.forClass(Instance.class);
    stringArgumentCaptor = ArgumentCaptor.forClass(Set.class);
    doReturn(CustomDeploymentTypeTemplate.builder()
                 .hostObjectArrayPath("Instances")
                 .hostAttributes(ImmutableMap.of("hostname", "ip"))
                 .build())
        .when(customDeploymentTypeService)
        .fetchDeploymentTemplate(ACCOUNT_ID, TEMPLATE_ID, "1");
    doReturn(Application.Builder.anApplication().uuid(APP_ID).accountId(ACCOUNT_ID).build())
        .when(appService)
        .get(APP_ID);
    doReturn(Environment.Builder.anEnvironment().accountId(ACCOUNT_ID).uuid(ENV_ID).build())
        .when(environmentService)
        .get(APP_ID, ENV_ID, false);
    doReturn(Service.builder().uuid(SERVICE_ID).appId(APP_ID).build())
        .when(serviceResourceService)
        .getWithDetails(APP_ID, SERVICE_ID);
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldAddInstancesOnFirstDeployment() {
    doReturn(emptyList()).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildSampleInstancesJson(1, 2, 3), ARTIFACT_ID,
                                    ARTIFACT_NAME, ARTIFACT_ID, ARTIFACT_NAME)),
        false, null);

    verify(instanceService, times(3)).save(captor.capture());
    verify(instanceService, never()).delete(anySet());

    final List<Instance> savedInstances = captor.getAllValues();

    List<Instance> expectedInstances = buildSampleInstances(ARTIFACT_ID, ARTIFACT_NAME, 1, 2, 3);
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
  }

  @Test
  @Owner(developers = RISHABH)
  @Category(UnitTests.class)
  public void shouldNotAddInstancesOnRollbackInFirstDeployment() {
    List<Instance> instancesInDb = buildSampleInstances(ARTIFACT_ID, ARTIFACT_NAME, 1, 2, 3);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(
                                    buildSampleInstancesJson(1, 2, 3), ARTIFACT_ID, ARTIFACT_NAME, null, null)),
        true, null);

    verify(instanceService, times(0)).save(captor.capture());
    verify(instanceService, times(1)).delete(anySet());
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldAddInstancesOnFirstDeploymentWithArtifact() {
    doReturn(emptyList()).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    final String artifactName = "hello-world";
    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildSampleInstancesJson(1, 2, 3), ARTIFACT_ID,
                                    artifactName, ARTIFACT_ID, artifactName)),
        false, null);

    verify(instanceService, times(3)).save(captor.capture());
    verify(instanceService, never()).delete(anySet());

    final List<Instance> savedInstances = captor.getAllValues();

    List<Instance> expectedInstances = buildSampleInstances(ARTIFACT_ID, artifactName, 1, 2, 3);
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldDeleteInstancesWhenDeletedFromInfraNewDeployment() {
    List<Instance> instancesInDb = buildSampleInstances(null, null, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildSampleInstancesJson(1, 5), ARTIFACT_ID,
                                    ARTIFACT_NAME, ARTIFACT_ID, ARTIFACT_NAME)),
        false, null);

    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());
    verify(instanceService, times(2)).save(captor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();
    final List<Instance> savedInstances = captor.getAllValues();

    assertThat(instanceIdsDeleted).contains("1", "2", "3", "4", "4");
    assertThat(getHostNames(savedInstances)).containsExactly("1", "5");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldDeleteAllWhenNoInstancesLeftInInfraNewDeployment() {
    List<Instance> instancesInDb = buildSampleInstances(null, null, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildEmptyInstancesJson())), false, null);

    verify(instanceService, never()).save(any(Instance.class));
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    assertThat(instanceIdsDeleted).contains("1", "2", "3", "4", "5");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldPerformAdditionDeletionNewDeployment() {
    List<Instance> instancesInDb = buildSampleInstances(null, null, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildSampleInstancesJson(1, 5, 6, 7), ARTIFACT_ID,
                                    ARTIFACT_NAME, ARTIFACT_ID, ARTIFACT_NAME)),
        false, null);

    verify(instanceService, times(4)).save(captor.capture());
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    List<Instance> expectedInstances = buildSampleInstances(ARTIFACT_ID, ARTIFACT_NAME, 1, 5, 6, 7);
    List<Instance> savedInstances = captor.getAllValues();
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
    assertThat(instanceIdsDeleted).contains("2", "3", "4");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldPerformAdditionDeletionNewDeploymentNewArtifact() {
    final String oldArtifactId = "old-artifact-id";
    final String oldArtifactName = "hello-old";
    final String newArtifactId = "new-artifact-id";
    final String newArtifactName = "hello-new";
    List<Instance> instancesInDb = buildSampleInstances(oldArtifactId, oldArtifactName, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.handleNewDeployment(singletonList(buildDeploymentSummary(buildSampleInstancesJson(1, 5, 6, 7),
                                    newArtifactId, newArtifactName, newArtifactId, newArtifactName)),
        false, null);

    verify(instanceService, times(4)).save(captor.capture());
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    List<Instance> expectedInstances = buildSampleInstances(newArtifactId, newArtifactName, 1, 5, 6, 7);
    List<Instance> savedInstances = captor.getAllValues();
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
    assertThat(instanceIdsDeleted).contains("2", "3", "4");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldDeleteInstancesWhenDeletedFromPTask() {
    List<Instance> instancesInDb = buildSampleInstances("old-artifact-id", "hello-world", 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.processInstanceSyncResponseFromPerpetualTask(
        buildInfraMapping(), buildPerpetualTaskResponse(buildSampleInstancesJson(1, 5)));

    verify(instanceService, never()).save(any(Instance.class));
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    assertThat(instanceIdsDeleted).contains("2", "3", "4");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldDeleteAllWhenNoInstancesLeftInInfraPTask() {
    List<Instance> instancesInDb = buildSampleInstances(null, null, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.processInstanceSyncResponseFromPerpetualTask(
        buildInfraMapping(), buildPerpetualTaskResponse(buildEmptyInstancesJson()));

    verify(instanceService, never()).save(any(Instance.class));
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    assertThat(instanceIdsDeleted).contains("1", "2", "3", "4", "5");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldPerformAdditionDeletionPTask() {
    final String artifactId = "artifact-id";
    final String artifactName = "hello-world";
    List<Instance> instancesInDb = buildSampleInstances(artifactId, artifactName, 1, 2, 3, 4, 5);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    handler.processInstanceSyncResponseFromPerpetualTask(
        buildInfraMapping(), buildPerpetualTaskResponse(buildSampleInstancesJson(1, 5, 6, 7)));

    verify(instanceService, times(2)).save(captor.capture());
    verify(instanceService, times(1)).delete(stringArgumentCaptor.capture());

    @SuppressWarnings("unchecked") final Set<String> instanceIdsDeleted = stringArgumentCaptor.getValue();

    List<Instance> savedInstances = captor.getAllValues();
    assertThat(savedInstances.stream()
                   .map(Instance::getInstanceInfo)
                   .map(PhysicalHostInstanceInfo.class ::cast)
                   .map(PhysicalHostInstanceInfo::getHostName)
                   .collect(Collectors.toList()))
        .containsExactlyInAnyOrder("6", "7");
    assertThat(savedInstances.stream().map(Instance::getLastDeployedByName).collect(Collectors.toSet()))
        .containsExactly(InstanceHandler.AUTO_SCALE);
    assertThat(savedInstances.stream().map(Instance::getLastArtifactId).collect(Collectors.toSet()))
        .containsExactly(artifactId);
    assertThat(savedInstances.stream().map(Instance::getLastArtifactName).collect(Collectors.toSet()))
        .containsExactly(artifactName);
    assertThat(instanceIdsDeleted).contains("2", "3", "4");
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void shouldBeNoopWhenInstancesRemainSamePTask() {
    doReturn(buildSampleInstances(null, null, 1, 2, 3, 4, 5))
        .when(instanceService)
        .getInstancesForAppAndInframapping(anyString(), anyString());

    handler.processInstanceSyncResponseFromPerpetualTask(
        buildInfraMapping(), buildPerpetualTaskResponse(buildSampleInstancesJson(1, 2, 3, 4, 5)));
    verify(instanceService, never()).save(any(Instance.class));
    verify(instanceService, never()).delete(anySet());
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void generateDeploymentKey() {
    final CustomDeploymentKey deploymentKey = (CustomDeploymentKey) handler.generateDeploymentKey(
        CustomDeploymentTypeInfo.builder().instanceFetchScript("echo hello").tags(asList("tag1", "tag2")).build());
    assertThat(deploymentKey.getInstanceFetchScriptHash()).isEqualTo("echo hello".hashCode());
    assertThat(deploymentKey.getTags()).containsExactly("tag1", "tag2");
  }

  @Test
  @Owner(developers = RISHABH)
  @Category(UnitTests.class)
  public void shouldNotUseRollbackArtifactInDeployPhase() {
    final String lastArtifactId = "last-success-artifact-id";
    final String lastArtifactName = "hello-last-success-artifact";
    final String newArtifactId = "new-artifact-id";
    final String newArtifactName = "hello-new-artifact";

    List<Instance> instancesInDb = buildSampleInstances(newArtifactId, newArtifactName, 1, 2, 3);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    List<DeploymentSummary> deploymentSummary = singletonList(buildDeploymentSummary(
        buildSampleInstancesJson(1, 2, 3), newArtifactId, newArtifactName, newArtifactId, newArtifactName));
    Artifact artifact = Artifact.Builder.anArtifact()
                            .withAppId(APP_ID)
                            .withUuid(lastArtifactId)
                            .withDisplayName(lastArtifactName)
                            .build();
    List<Artifact> artifacts = new ArrayList<>();
    artifacts.add(artifact);

    handler.handleNewDeployment(deploymentSummary, false, null);

    verify(instanceService, times(3)).save(captor.capture());
    verify(instanceService, times(1)).delete(anySet());

    final List<Instance> savedInstances = captor.getAllValues();

    List<Instance> expectedInstances = buildSampleInstances(newArtifactId, newArtifactName, 1, 2, 3);
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
  }

  @Test
  @Owner(developers = RISHABH)
  @Category(UnitTests.class)
  public void shouldUseRollbackArtifactInRollbackPhase() {
    final String lastArtifactId = "last-success-artifact-id";
    final String lastArtifactName = "hello-last-success-artifact";
    final String newArtifactId = "new-artifact-id";
    final String newArtifactName = "hello-new-artifact";

    List<Instance> instancesInDb = buildSampleInstances(newArtifactId, newArtifactName, 1, 2, 3);
    instancesInDb.forEach(setExecutionId());
    doReturn(instancesInDb).when(instanceService).getInstancesForAppAndInframapping(anyString(), anyString());

    List<DeploymentSummary> deploymentSummary = singletonList(buildDeploymentSummary(
        buildSampleInstancesJson(1, 2, 3), newArtifactId, newArtifactName, lastArtifactId, lastArtifactName));
    Artifact artifact = Artifact.Builder.anArtifact()
                            .withAppId(APP_ID)
                            .withUuid(lastArtifactId)
                            .withDisplayName(lastArtifactName)
                            .build();
    List<Artifact> artifacts = new ArrayList<>();
    artifacts.add(artifact);

    handler.handleNewDeployment(deploymentSummary, true, null);

    verify(instanceService, times(3)).save(captor.capture());
    verify(instanceService, times(1)).delete(anySet());

    final List<Instance> savedInstances = captor.getAllValues();

    List<Instance> expectedInstances = buildSampleInstances(lastArtifactId, lastArtifactName, 1, 2, 3);
    savedInstances.forEach(this::nullUuid);
    expectedInstances.forEach(this::nullUuid);
    assertThat(savedInstances).containsExactlyElementsOf(expectedInstances);
  }

  private List<Instance> buildSampleInstances(String artifactId, String artifactName, int... indexes) {
    List<Instance> instances = new ArrayList<>();
    for (int n : indexes) {
      String hostName = String.valueOf(n);
      instances.add(
          Instance.builder()
              .appId(APP_ID)
              .uuid(hostName)
              .accountId(ACCOUNT_ID)
              .infraMappingId(INFRA_MAPPING_ID)
              .serviceId(SERVICE_ID)
              .instanceType(InstanceType.PHYSICAL_HOST_INSTANCE)
              .infraMappingType(InfrastructureMappingType.CUSTOM.name())
              .envId(ENV_ID)
              .envType(NON_PROD)
              .lastArtifactId(artifactId)
              .lastArtifactName(artifactName)
              .hostInstanceKey(HostInstanceKey.builder().hostName(hostName).infraMappingId(INFRA_MAPPING_ID).build())
              .instanceInfo(PhysicalHostInstanceInfo.builder()
                                .hostName(hostName)
                                .hostId(hostName)
                                .properties(ImmutableMap.of("hostname", hostName))
                                .build())
              .build());
    }
    return instances;
  }

  private String buildSampleInstancesJson(int... indexes) {
    List<Map<String, Object>> object = new ArrayList<>();
    for (int n : indexes) {
      String hostName = String.valueOf(n);
      object.add(ImmutableMap.of("ip", hostName));
    }
    return JsonUtils.asJson(ImmutableMap.of("Instances", object));
  }

  private String buildEmptyInstancesJson() {
    List<Map<String, Object>> object = new ArrayList<>();
    return JsonUtils.asJson(ImmutableMap.of("Instances", object));
  }

  private InfrastructureMapping buildInfraMapping() {
    CustomInfrastructureMapping infraMapping = CustomInfrastructureMapping.builder().build();
    infraMapping.setDeploymentTypeTemplateVersion("1");
    infraMapping.setCustomDeploymentTemplateId(TEMPLATE_ID);
    infraMapping.setUuid(INFRA_MAPPING_ID);
    infraMapping.setAccountId(ACCOUNT_ID);
    infraMapping.setAppId(APP_ID);
    infraMapping.setEnvId(ENV_ID);
    infraMapping.setServiceId(SERVICE_ID);
    infraMapping.setInfraMappingType(InfrastructureMappingType.CUSTOM.name());
    return infraMapping;
  }

  private void nullUuid(Instance instance) {
    instance.setUuid(null);
  }

  private ShellScriptProvisionExecutionData buildPerpetualTaskResponse(String output) {
    return ShellScriptProvisionExecutionData.builder().executionStatus(ExecutionStatus.SUCCESS).output(output).build();
  }

  private DeploymentSummary buildDeploymentSummary(String scriptOutput) {
    return DeploymentSummary.builder()
        .appId(APP_ID)
        .accountId(ACCOUNT_ID)
        .infraMappingId(INFRA_MAPPING_ID)
        .deploymentInfo(CustomDeploymentTypeInfo.builder().scriptOutput(scriptOutput).build())
        .build();
  }

  private DeploymentSummary buildDeploymentSummary(String scriptOutput, String artifactId, String artifactName,
      String deploymentInfoArtifactId, String deploymentInfoArtifactName) {
    return DeploymentSummary.builder()
        .appId(APP_ID)
        .accountId(ACCOUNT_ID)
        .infraMappingId(INFRA_MAPPING_ID)
        .artifactId(artifactId)
        .artifactName(artifactName)
        .deploymentInfo(CustomDeploymentTypeInfo.builder()
                            .artifactId(deploymentInfoArtifactId)
                            .artifactName(deploymentInfoArtifactName)
                            .scriptOutput(scriptOutput)
                            .build())
        .build();
  }

  private Consumer<Instance> setExecutionId() {
    return instance -> instance.setLastWorkflowExecutionId(WORKFLOW_EXECUTION_ID);
  }

  private Set<String> getHostNames(List<Instance> instances) {
    return instances.stream()
        .map(Instance::getInstanceInfo)
        .map(PhysicalHostInstanceInfo.class ::cast)
        .map(PhysicalHostInstanceInfo::getHostName)
        .collect(Collectors.toSet());
  }
}
