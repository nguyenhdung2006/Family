package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private final ChatRoomRepository rooms;
    private final ChatMessageRepository messages;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditLogService auditLogService;

    public ChatService(
        ChatRoomRepository rooms,
        ChatMessageRepository messages,
        SimpMessagingTemplate messagingTemplate,
        AuditLogService auditLogService
    ) {
        this.rooms = rooms;
        this.messages = messages;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<MessageDTO.RoomResponse> listRooms() {
        return rooms.findAll().stream().map(MessageDTO.RoomResponse::from).toList();
    }

    @Transactional
    public MessageDTO.RoomResponse createRoom(MessageDTO.RoomRequest request) {
        ChatRoom room = new ChatRoom();
        room.setName(request.name());
        room.setType(request.type());
        room.setBranch(request.branch());
        return MessageDTO.RoomResponse.from(rooms.save(room));
    }

    @Transactional(readOnly = true)
    public List<MessageDTO.Response> listMessages(UUID roomId, int page, int size) {
        return messages.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, normalizeSize(size))).stream()
            .map(MessageDTO.Response::from)
            .toList();
    }

    @Transactional
    public MessageDTO.Response sendMessage(UUID roomId, MessageDTO.SendRequest request) {
        ChatRoom room = rooms.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));

        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setType(request.type());
        message.setBody(request.body());
        message.setMediaUrl(request.mediaUrl());
        message.setDeliveredAt(Instant.now());
        ChatMessage saved = messages.save(message);
        auditLogService.messageSent(roomId, saved.getId());
        MessageDTO.Response response = MessageDTO.Response.from(saved);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, response);
        return response;
    }

    @Transactional
    public MessageDTO.Response markSeen(UUID messageId) {
        ChatMessage message = messages.findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat message not found"));
        message.setSeenAt(Instant.now());
        return MessageDTO.Response.from(messages.save(message));
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }
}
