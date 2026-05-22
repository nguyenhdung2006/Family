package com.familyhub.digital_family_hub.notifications;

import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "in_app_notifications",
    indexes = {
        @Index(name = "idx_notifications_recipient_read", columnList = "recipient_id,readAt"),
        @Index(name = "idx_notifications_scheduled", columnList = "scheduledFor")
    }
)
public class InAppNotification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private AppUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String body;

    private Instant scheduledFor;

    private Instant readAt;

    public AppUser getRecipient() {
        return recipient;
    }

    public void setRecipient(AppUser recipient) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Instant scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
