/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.beans.execution;

import io.harness.annotation.RecasterAlias;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@Value
@Builder
@TypeAlias("repository")
@RecasterAlias("io.harness.beans.execution.Repository")
public class Repository {
  private String name;
  private String namespace;
  private String link;
  private String branch;
  private boolean isPrivate;
  private String httpURL;
  private String sshURL;
  private String slug; // Repository name along with namespace
}
