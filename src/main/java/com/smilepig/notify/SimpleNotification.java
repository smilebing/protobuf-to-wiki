package com.smilepig.notify;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * Created by zhuhe on 2020/9/19
 */
public class SimpleNotification {

    private volatile static Notification notification;

    private static NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("protobuf-to-wiki", NotificationDisplayType.BALLOON, true);


    public static void notify(Project project, String content, String alertTitle) {
        if (notification == null) {
            synchronized (SimpleNotification.class) {
                if (notification == null) {
                    notification = NOTIFICATION_GROUP.createNotification(alertTitle,
                                                                         content,
                                                                         NotificationType.INFORMATION,
                                                                         new WikiGenerateNotificationListener());
                }
            }
        }
        notification.setContent(content);
        notification.setTitle(alertTitle);
        notification.notify(project);
    }
}
