package com.familyhub.digital_family_hub.shared.audit;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("hometree.audit");

    public void login(String email) {
        AUDIT_LOG.info("event=login email={}", email);
    }

    public void dataChange(String action, String entity, UUID entityId) {
        AUDIT_LOG.info("event=data_change action={} entity={} entity_id={}", action, entity, entityId);
    }

    public void messageSent(UUID roomId, UUID messageId) {
        AUDIT_LOG.info("event=message_sent room_id={} message_id={}", roomId, messageId);
    }
}
