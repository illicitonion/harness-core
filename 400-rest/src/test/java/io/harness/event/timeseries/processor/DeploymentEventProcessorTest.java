/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.event.timeseries.processor;

import static io.harness.rule.OwnerRule.MILOS;
import static io.harness.rule.OwnerRule.RAMA;

import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.ARTIFACT_ID;
import static software.wings.utils.WingsTestConstants.ENV_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;
import static software.wings.utils.WingsTestConstants.WORKFLOW_EXECUTION_ID;
import static software.wings.utils.WingsTestConstants.WORKFLOW_ID;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.beans.EnvironmentType;
import io.harness.beans.ExecutionStatus;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import io.harness.timescaledb.TimeScaleDBService;

import software.wings.WingsBaseTest;
import software.wings.service.impl.event.timeseries.TimeSeriesEventInfo;
import software.wings.utils.WingsTestConstants;

import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * @author rktummala
 */
public class DeploymentEventProcessorTest extends WingsBaseTest {
  @Mock TimeScaleDBService timeScaleService;
  @Inject @InjectMocks DeploymentEventProcessor deploymentEventProcessor;

  private Connection dbConnection = mock(Connection.class);
  private PreparedStatement preparedStatement = mock(PreparedStatement.class);

  @Before
  public void setup() throws SQLException {
    when(timeScaleService.isValid()).thenReturn(true);
    when(timeScaleService.getDBConnection()).thenReturn(dbConnection);
  }

  @Test
  @Owner(developers = RAMA)
  @Category(UnitTests.class)
  public void shouldListExecutions() throws SQLException {
    Map<String, String> stringData = new HashMap<>();
    populateStringData(stringData);

    Map<String, Long> longData = new HashMap<>();
    populateLongData(123L, 233L, 244L, 33L, longData);

    Map<String, Integer> integerData = new HashMap<>();
    populateIntegerData(1, integerData);

    Map<String, List<String>> listData = new HashMap<>();
    populateListData(listData);

    Map<String, Object> objectData = new HashMap<>();
    populateObjectData(objectData);

    TimeSeriesEventInfo timeSeriesEventInfo = TimeSeriesEventInfo.builder()
                                                  .accountId(ACCOUNT_ID)
                                                  .longData(longData)
                                                  .integerData(integerData)
                                                  .stringData(stringData)
                                                  .listData(listData)
                                                  .data(objectData)
                                                  .build();

    when(dbConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
    deploymentEventProcessor.processEvent(timeSeriesEventInfo);
    verify(preparedStatement).execute();
  }

  @Test
  @Owner(developers = MILOS)
  @Category(UnitTests.class)
  public void shouldThrowSQLException() throws SQLException {
    TimeSeriesEventInfo timeSeriesEventInfo = TimeSeriesEventInfo.builder().build();
    when(dbConnection.prepareStatement(anyString())).thenThrow(new SQLException());

    deploymentEventProcessor.processEvent(timeSeriesEventInfo);
    verify(dbConnection, times(5)).prepareStatement(anyString());
  }

  @Test
  @Owner(developers = MILOS)
  @Category(UnitTests.class)
  public void shouldProcessRunningEvent() throws SQLException {
    Map<String, String> stringData = new HashMap<>();
    populateStringData(stringData);

    Map<String, Long> longData = new HashMap<>();
    populateLongData(null, 233L, null, null, longData);

    Map<String, Integer> integerData = new HashMap<>();
    populateIntegerData(null, integerData);

    Map<String, List<String>> listData = new HashMap<>();
    populateListData(listData);

    Map<String, Object> objectData = new HashMap<>();
    populateObjectData(objectData);

    TimeSeriesEventInfo timeSeriesEventInfo = TimeSeriesEventInfo.builder()
                                                  .accountId(ACCOUNT_ID)
                                                  .longData(longData)
                                                  .integerData(integerData)
                                                  .stringData(stringData)
                                                  .listData(listData)
                                                  .data(objectData)
                                                  .build();

    when(dbConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
    deploymentEventProcessor.processEvent(timeSeriesEventInfo);
    verify(preparedStatement).execute();
  }

  @Test
  @Owner(developers = MILOS)
  @Category(UnitTests.class)
  public void shouldProcessEventWithoutListData() throws SQLException {
    Map<String, String> stringData = new HashMap<>();
    populateStringData(stringData);

    Map<String, Long> longData = new HashMap<>();
    populateLongData(123L, 233L, 244L, 33L, longData);

    Map<String, Integer> integerData = new HashMap<>();
    populateIntegerData(1, integerData);

    Map<String, List<String>> listData = null;
    populateListData(listData);

    Map<String, Object> objectData = new HashMap<>();
    populateObjectData(objectData);

    TimeSeriesEventInfo timeSeriesEventInfo = TimeSeriesEventInfo.builder()
                                                  .accountId(ACCOUNT_ID)
                                                  .longData(longData)
                                                  .integerData(integerData)
                                                  .stringData(stringData)
                                                  .listData(listData)
                                                  .data(objectData)
                                                  .build();

    when(dbConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
    deploymentEventProcessor.processEvent(timeSeriesEventInfo);
    verify(preparedStatement).execute();
  }

  // Inner methods
  private void populateStringData(Map<String, String> stringData) {
    stringData.put(EventProcessor.ACCOUNTID, ACCOUNT_ID);
    stringData.put(EventProcessor.EXECUTIONID, WORKFLOW_EXECUTION_ID);
    stringData.put(EventProcessor.APPID, APP_ID);
    stringData.put(EventProcessor.TRIGGERED_BY, WingsTestConstants.USER_ID);
    stringData.put(EventProcessor.TRIGGER_ID, WingsTestConstants.TRIGGER_ID);
    stringData.put(EventProcessor.STATUS, ExecutionStatus.SUCCESS.name());
    stringData.put(EventProcessor.PARENT_EXECUTION, WingsTestConstants.PIPELINE_WORKFLOW_EXECUTION_ID);
    stringData.put(EventProcessor.STAGENAME, WingsTestConstants.WORKFLOW_NAME);
    stringData.put(EventProcessor.PIPELINE, WingsTestConstants.PIPELINE_NAME);
  }

  private void populateLongData(
      Long rollbackDuration, Long startTime, Long endTime, Long duration, Map<String, Long> longData) {
    longData.put(EventProcessor.ROLLBACK_DURATION, rollbackDuration);
    longData.put(EventProcessor.STARTTIME, startTime);
    longData.put(EventProcessor.ENDTIME, endTime);
    longData.put(EventProcessor.DURATION, duration);
  }

  private void populateIntegerData(Integer deployedCount, Map<String, Integer> integerData) {
    integerData.put(EventProcessor.INSTANCES_DEPLOYED, deployedCount);
  }

  private void populateListData(Map<String, List<String>> listData) {
    if (listData != null) {
      listData.put(EventProcessor.SERVICE_LIST, asList(SERVICE_ID));
      listData.put(EventProcessor.WORKFLOW_LIST, asList(WORKFLOW_ID));
      listData.put(EventProcessor.CLOUD_PROVIDER_LIST, asList(WingsTestConstants.COMPUTE_PROVIDER_ID));
      listData.put(EventProcessor.ENV_LIST, asList(ENV_ID));
      listData.put(EventProcessor.ARTIFACT_LIST, asList(ARTIFACT_ID));
      listData.put(EventProcessor.ENVTYPES, asList(EnvironmentType.PROD.name()));
    }
  }

  private void populateObjectData(Map<String, Object> objectData) {
    Map<String, String> tags = new HashMap<>();
    tags.put("env", "QA");
    tags.put("description", "this is a really really long description");
    objectData.put(EventProcessor.TAGS, tags);
  }
}
