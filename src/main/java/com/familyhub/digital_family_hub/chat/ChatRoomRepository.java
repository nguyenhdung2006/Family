package com.familyhub.digital_family_hub.chat;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
}
