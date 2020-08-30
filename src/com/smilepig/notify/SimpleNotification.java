package com.smilepig.notify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * Created by zhuhe on 2020/8/30
 */
public class SimpleNotification {

    private NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("protobuf-to-wiki", NotificationDisplayType.BALLOON, true);


    public void notify(Project project, String content) {
        final Notification notification = NOTIFICATION_GROUP.createNotification("protobuf-to-wiki",
                                                                                content,
                                                                                NotificationType.INFORMATION,
                                                                                new WikiGenerateNotificationListener());
        notification.notify(project);
    }
}
