package com.familyhub.digital_family_hub.notifications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class NotificationDTO {

    private NotificationDTO() {
    }

    public record Request(
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 3000) String body,
        Instant scheduledFor
    ) {
    }

    public record Response(
        UUID id,
        UUID createdById,
        String createdByName,
        NotificationType type,
        String title,
        String body,
        Instant scheduledFor,
        Instant readAt
    ) {
        public static Response from(InAppNotification notification) {
            return new Response(
                notification.getId(),
                notification.getRecipient() != null ? notification.getRecipient().getId() : null,
                notification.getRecipient() != null ? notification.getRecipient().getName() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getScheduledFor(),
                notification.getReadAt()
            );
        }
    }
}
