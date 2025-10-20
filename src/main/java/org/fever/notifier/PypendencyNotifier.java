package org.fever.notifier;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class PypendencyNotifier {
    public static void notify(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Pypendency notification")
                .createNotification(content, type)
                .notify(project);
    }
}
