package io.harness.awscli;


import io.harness.annotations.dev.OwnedBy;
import lombok.experimental.UtilityClass;

import static io.harness.annotations.dev.HarnessTeam.CDP;

@OwnedBy(CDP)
@UtilityClass
public class AwsCliConstants {
  public static final String CONFIGURE_OPTION = "${OPTION}";
  public static final String CONFIGURE_VALUE = "${VALUE}";
  public static final String CONFIGURE_PROFILE = "${PROFILE}";
  public static final String ROLE_ARN = "${ROLE_ARN}";
  public static final String ROLE_SESSION_NAME = "${ROLE_ARN}";


  public static final String SET_CONFIGURE_COMMAND = "aws configure set " + CONFIGURE_OPTION+ " "+CONFIGURE_VALUE+" --profile "+CONFIGURE_PROFILE;

  public static final String STS_COMMAND = "aws sts assume-role --role-arn " + ROLE_ARN + " --role-session-name " + ROLE_SESSION_NAME+
          " --profile "+ CONFIGURE_PROFILE;

}
