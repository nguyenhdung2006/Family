package com.familyhub.digital_family_hub.chat;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    List<ChatRoom> findByParticipantsId(UUID participantId);

    @Query("""
        select count(room) > 0
        from ChatRoom room
        join room.participants participant
        where room.id = :roomId and lower(participant.email) = lower(:email)
        """)
    boolean existsByIdAndParticipantEmailIgnoreCase(@Param("roomId") UUID roomId, @Param("email") String email);
}
