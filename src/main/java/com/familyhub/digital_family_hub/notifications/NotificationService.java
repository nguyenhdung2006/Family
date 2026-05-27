package com.familyhub.digital_family_hub.notifications;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import java.security.Principal;
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
    private final AppUserRepository users;

    public NotificationService(InAppNotificationRepository notifications, AppUserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO.Response> listNotifications(Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        return notifications.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId()).stream()
            .map(NotificationDTO.Response::from)
            .toList();
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
    public NotificationDTO.Response markRead(UUID id, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        InAppNotification notification = notifications.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        notification.setReadAt(Instant.now());
        return NotificationDTO.Response.from(notifications.save(notification));
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }
}
