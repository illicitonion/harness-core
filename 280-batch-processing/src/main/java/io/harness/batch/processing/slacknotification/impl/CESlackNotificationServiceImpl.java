/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.batch.processing.slacknotification.impl;

import com.google.inject.Inject;
import io.harness.annotations.dev.OwnedBy;
import io.harness.batch.processing.slacknotification.intfc.CESlackNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import software.wings.beans.SlackMessage;
import software.wings.beans.notification.SlackNotificationConfiguration;
import software.wings.beans.notification.SlackNotificationSetting;

import com.google.inject.Singleton;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.wings.service.intfc.SlackMessageSender;

import static io.harness.annotations.dev.HarnessTeam.CE;

@OwnedBy(CE)
@Singleton
@Slf4j
public class CESlackNotificationServiceImpl implements CESlackNotificationService {
  @Autowired private SlackMessageSender slackMessageSender;

  public void sendMessage(SlackNotificationConfiguration slackConfig, String slackChannel, String senderName,
      String message, String accountId) {
    if (Objects.requireNonNull(slackConfig, "slack Config can't be null")
            .equals(SlackNotificationSetting.emptyConfig())) {
      return;
    }

    String webhookUrl = slackConfig.getOutgoingWebhookUrl();
    if (StringUtils.isEmpty(webhookUrl)) {
      log.error("Webhook URL is empty. No message will be sent. Config: {}, Message: {}", slackConfig, message);
      return;
    }

    log.info("Sending message for account {} via manager", accountId);
    slackMessageSender.send(
        new SlackMessage(slackConfig.getOutgoingWebhookUrl(), slackChannel, senderName, message), false, false);
  }
}
