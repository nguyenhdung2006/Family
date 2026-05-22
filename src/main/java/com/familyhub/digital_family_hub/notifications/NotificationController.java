package com.familyhub.digital_family_hub.notifications;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InAppNotificationRepository notifications;

    public NotificationController(InAppNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationResponse> listNotifications() {
        return notifications.findAll().stream().map(NotificationResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        InAppNotification notification = new InAppNotification();
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setBody(request.body());
        notification.setScheduledFor(request.scheduledFor());
        return NotificationResponse.from(notifications.save(notification));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        InAppNotification notification = notifications.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setReadAt(Instant.now());
        return NotificationResponse.from(notifications.save(notification));
    }

    public record CreateNotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String body,
        Instant scheduledFor
    ) {
    }

    public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String body,
        Instant scheduledFor,
        Instant readAt
    ) {
        static NotificationResponse from(InAppNotification notification) {
            return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getScheduledFor(),
                notification.getReadAt()
            );
        }
    }
}
