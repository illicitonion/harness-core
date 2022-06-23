/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.steps.jenkins;

import static io.harness.rule.OwnerRule.SHIVAM;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.harness.CategoryTest;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.yaml.ParameterField;
import io.harness.rule.Owner;
import io.harness.steps.jenkins.jenkinsstep.JenkinsBuildStepUtils;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(HarnessTeam.PIPELINE)
public class JenkinsBuildStepUtilsTest extends CategoryTest {
  @Test
  @Owner(developers = SHIVAM)
  @Category(UnitTests.class)
  public void testProcessJiraFieldsInParameters() {
    assertThat(JenkinsBuildStepUtils.processJenkinsFieldsInParameters(null)).isEmpty();
    assertThat(JenkinsBuildStepUtils.processJenkinsFieldsInParameters(Collections.emptyMap())).isEmpty();

    assertThatThrownBy(()
                           -> JenkinsBuildStepUtils.processJenkinsFieldsInParameters(ImmutableMap.of("a",
                               ParameterField.createValueField("va"), "b", ParameterField.createValueField(null), "c",
                               ParameterField.createExpressionField(true, "<+abc>", null, true))))
        .hasMessage("Field [c] has invalid jenkins field value");

    Map<String, String> fields = JenkinsBuildStepUtils.processJenkinsFieldsInParameters(
        ImmutableMap.of("a", ParameterField.createValueField("va"), "b", ParameterField.createValueField(null)));
    assertThat(fields.size()).isEqualTo(1);
    assertThat(fields.get("a")).isEqualTo("va");
  }
}
