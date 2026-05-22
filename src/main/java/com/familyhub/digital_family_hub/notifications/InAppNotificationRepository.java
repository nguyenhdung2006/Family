package com.familyhub.digital_family_hub.notifications;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {
    List<InAppNotification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}
