package io.harness.ng.core.buckets.resources.s3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.harness.annotations.dev.OwnedBy;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import static io.harness.annotations.dev.HarnessTeam.CDC;

@OwnedBy(CDC)
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("BucketResponse")
@Schema(name = "BucketResponse", description = "This contains details of the Bucket Response")
public class BucketResponseDTO {
    String bucketName;
}
