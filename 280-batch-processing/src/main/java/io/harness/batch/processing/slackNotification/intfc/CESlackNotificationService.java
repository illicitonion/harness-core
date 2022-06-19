package io.harness.batch.processing.slackNotification.intfc;

import software.wings.beans.notification.SlackNotificationConfiguration;

public interface CESlackNotificationService {
    void sendMessage(SlackNotificationConfiguration slackConfig, String slackChannel, String senderName,
        String message, String accountId);
}
