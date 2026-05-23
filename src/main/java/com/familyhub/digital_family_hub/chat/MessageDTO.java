package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.family.FamilyBranch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class MessageDTO {

    private MessageDTO() {
    }

    public record RoomRequest(@NotBlank @Size(max = 255) String name, @NotNull ChatRoomType type, FamilyBranch branch) {
    }

    public record SendRequest(
        @NotNull MessageType type,
        @NotBlank @Size(max = 4000) String body,
        @Size(max = 2000) String mediaUrl
    ) {
    }

    public record RoomResponse(UUID id, String name, ChatRoomType type, FamilyBranch branch) {
        public static RoomResponse from(ChatRoom room) {
            return new RoomResponse(room.getId(), room.getName(), room.getType(), room.getBranch());
        }
    }

    public record Response(
        UUID id,
        UUID roomId,
        UUID senderId,
        String senderName,
        MessageType type,
        String body,
        String mediaUrl,
        Instant deliveredAt,
        Instant seenAt
    ) {
        public static Response from(ChatMessage message) {
            return new Response(
                message.getId(),
                message.getRoom().getId(),
                message.getSender() == null ? null : message.getSender().getId(),
                message.getSender() == null ? null : message.getSender().getName(),
                message.getType(),
                message.getBody(),
                message.getMediaUrl(),
                message.getDeliveredAt(),
                message.getSeenAt()
            );
        }
    }
}
