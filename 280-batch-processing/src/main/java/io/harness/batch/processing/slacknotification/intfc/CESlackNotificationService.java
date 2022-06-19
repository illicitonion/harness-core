package io.harness.batch.processing.slacknotification.intfc;

import io.harness.annotations.dev.OwnedBy;
import software.wings.beans.notification.SlackNotificationConfiguration;

import static io.harness.annotations.dev.HarnessTeam.CE;

@OwnedBy(CE)
public interface CESlackNotificationService {
    void sendMessage(SlackNotificationConfiguration slackConfig, String slackChannel, String senderName,
        String message, String accountId);
}
