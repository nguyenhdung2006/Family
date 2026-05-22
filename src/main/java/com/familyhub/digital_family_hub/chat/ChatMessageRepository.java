package com.familyhub.digital_family_hub.chat;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(UUID roomId);

    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);
}
