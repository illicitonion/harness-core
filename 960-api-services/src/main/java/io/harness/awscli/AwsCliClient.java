package io.harness.awscli;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cli.CliResponse;
import io.harness.logging.LogCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@OwnedBy(HarnessTeam.CDP)
public interface AwsCliClient {
  CliResponse setConfigure(String configureOption, String configureValue, String profile, String directory,
      LogCallback executionLogCallback, long timeoutInMillis)
      throws IOException, InterruptedException, TimeoutException;

  CliResponse stsAssumeRole(String roleArn, String roleSessionName, String externalId, String region, String profile, String directory,
      LogCallback executionLogCallback, long timeoutInMillis)
      throws IOException, InterruptedException, TimeoutException;
}
