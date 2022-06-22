/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.cvng.beans;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MetricResponseMappingDTO {
  public static final String CUSTOM_HEALTH_SRC_PATH_ARRAY_DELIMITER = ".[*].";

  String metricValueJsonPath;
  String timestampJsonPath;
  String serviceInstanceJsonPath;
  String timestampFormat;
  String metricJsonPath;
  String relativeTimestampJsonPath;
  String relativeMetricValueJsonPath;

  public String getMetricValueJsonPath() {
    if (Objects.isNull(metricJsonPath) || Objects.isNull(relativeMetricValueJsonPath)) {
      return metricValueJsonPath;
    } else {
      return metricJsonPath + CUSTOM_HEALTH_SRC_PATH_ARRAY_DELIMITER + relativeMetricValueJsonPath;
    }
  }

  public String getTimestampJsonPath() {
    if (Objects.isNull(metricJsonPath) || Objects.isNull(relativeTimestampJsonPath)) {
      return timestampJsonPath;
    } else {
      return metricJsonPath + CUSTOM_HEALTH_SRC_PATH_ARRAY_DELIMITER + relativeTimestampJsonPath;
    }
  }
}
