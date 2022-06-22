/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.helpers.ext.azure;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.azure.model.AzureConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@OwnedBy(HarnessTeam.CDP)
public class AzureIdentityAccessTokenResponse {
  @JsonProperty(AzureConstants.TOKEN_TYPE) private String tokenType;
  @JsonProperty(AzureConstants.TOKEN_EXPIRES_IN) private Integer expiresIn;
  @JsonProperty(AzureConstants.TOKEN_EXT_EXPIRES_IN) private Integer extExpiresIn;
  @JsonProperty(AzureConstants.ACCESS_TOKEN) private String accessToken;
}
