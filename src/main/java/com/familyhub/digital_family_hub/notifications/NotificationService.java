package com.familyhub.digital_family_hub.notifications;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

    private final InAppNotificationRepository notifications;

    public NotificationService(InAppNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO.Response> listNotifications() {
        return notifications.findAll().stream().map(NotificationDTO.Response::from).toList();
    }

    @Transactional
    public NotificationDTO.Response createNotification(NotificationDTO.Request request) {
        InAppNotification notification = new InAppNotification();
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setBody(request.body());
        notification.setScheduledFor(request.scheduledFor());
        return NotificationDTO.Response.from(notifications.save(notification));
    }

    @Transactional
    public NotificationDTO.Response markRead(UUID id) {
        InAppNotification notification = notifications.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setReadAt(Instant.now());
        return NotificationDTO.Response.from(notifications.save(notification));
    }
}
