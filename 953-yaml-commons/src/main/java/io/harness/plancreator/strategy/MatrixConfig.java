/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.plancreator.strategy;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.beans.SwaggerConstants.INTEGER_CLASSPATH;
import static io.harness.common.NGExpressionUtils.GENERIC_EXPRESSIONS_PATTERN;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.expression;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.list;
import static io.harness.yaml.schema.beans.SupportedPossibleFieldTypes.runtime;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.OwnedBy;
import io.harness.common.NGExpressionUtils;
import io.harness.exception.InvalidYamlException;
import io.harness.pms.yaml.ParameterField;
import io.harness.pms.yaml.YamlNode;
import io.harness.yaml.YamlSchemaTypes;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@OwnedBy(PIPELINE)
@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RecasterAlias("io.harness.plancreator.strategy.MatrixConfig")
public class MatrixConfig implements MatrixConfigInterface {
  private static String EXCLUDE_KEYWORD = "exclude";
  private static String BATCH_SIZE = "batchSize";

  @ApiModelProperty(hidden = true) @Builder.Default Map<String, AxisConfig> axes = new LinkedHashMap<>();
  @YamlSchemaTypes(value = {runtime, list}) ParameterField<List<ExcludeConfig>> exclude;

  @ApiModelProperty(dataType = INTEGER_CLASSPATH)
  @JsonProperty("maxConcurrency")
  @Min(value = 0)
  @YamlSchemaTypes(value = {expression})
  ParameterField<Integer> maxConcurrency;
  @JsonAnySetter
  void setAxis(String key, Object value) {
    try {
      if (axes == null) {
        axes = new HashMap<>();
      }
      if (key.equals(YamlNode.UUID_FIELD_NAME)) {
        return;
      }
      if (value instanceof List) {
        List<String> stringList = new ArrayList<>();
        for (Object val : (List<Object>) value) {
          stringList.add(String.valueOf(val));
        }
        axes.put(key, new AxisConfig(ParameterField.createValueField(stringList)));
      } else if (value instanceof String) {
        if (NGExpressionUtils.matchesPattern(GENERIC_EXPRESSIONS_PATTERN, value.toString())) {
          axes.put(key, new AxisConfig(ParameterField.createExpressionField(true, (String) value, null, false)));
        } else {
          throw new InvalidYamlException(String.format(
              "Value provided for axes [%s] is string. It should either be a List or an Expression.", key));
        }
      }
    } catch (Exception ex) {
      throw new InvalidYamlException("Unable to parse Matrix yaml. Please ensure that it is in correct format", ex);
    }
  }
}
