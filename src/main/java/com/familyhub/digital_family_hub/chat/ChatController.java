package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.family.FamilyBranch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

    private final ChatRoomRepository rooms;
    private final ChatMessageRepository messages;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
        ChatRoomRepository rooms,
        ChatMessageRepository messages,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.rooms = rooms;
        this.messages = messages;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/rooms")
    public List<ChatRoomResponse> listRooms() {
        return rooms.findAll().stream().map(ChatRoomResponse::from).toList();
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(@Valid @RequestBody CreateChatRoomRequest request) {
        ChatRoom room = new ChatRoom();
        room.setName(request.name());
        room.setType(request.type());
        room.setBranch(request.branch());
        return ChatRoomResponse.from(rooms.save(room));
    }

    @GetMapping("/rooms/{roomId}")
    public List<ChatMessageResponse> listMessages(@PathVariable UUID roomId) {
        return messages.findTop50ByRoomIdOrderByCreatedAtDesc(roomId).stream()
            .map(ChatMessageResponse::from)
            .toList();
    }

    @PostMapping("/rooms/{roomId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse sendMessage(
        @PathVariable UUID roomId,
        @Valid @RequestBody SendMessageRequest request
    ) {
        ChatRoom room = rooms.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));

        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setType(request.type());
        message.setBody(request.body());
        message.setMediaUrl(request.mediaUrl());
        message.setDeliveredAt(Instant.now());
        ChatMessage saved = messages.save(message);
        ChatMessageResponse response = ChatMessageResponse.from(saved);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, response);
        return response;
    }

    public record CreateChatRoomRequest(@NotBlank String name, @NotNull ChatRoomType type, FamilyBranch branch) {
    }

    public record SendMessageRequest(@NotNull MessageType type, @NotBlank String body, String mediaUrl) {
    }

    public record ChatRoomResponse(UUID id, String name, ChatRoomType type, FamilyBranch branch) {
        static ChatRoomResponse from(ChatRoom room) {
            return new ChatRoomResponse(room.getId(), room.getName(), room.getType(), room.getBranch());
        }
    }

    public record ChatMessageResponse(
        UUID id,
        UUID roomId,
        MessageType type,
        String body,
        String mediaUrl,
        Instant deliveredAt,
        Instant seenAt
    ) {
        static ChatMessageResponse from(ChatMessage message) {
            return new ChatMessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getType(),
                message.getBody(),
                message.getMediaUrl(),
                message.getDeliveredAt(),
                message.getSeenAt()
            );
        }
    }
}
