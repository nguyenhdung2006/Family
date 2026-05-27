package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import java.security.Principal;
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
    private final AppUserRepository users;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditLogService auditLogService;

    public ChatService(
        ChatRoomRepository rooms,
        ChatMessageRepository messages,
        AppUserRepository users,
        SimpMessagingTemplate messagingTemplate,
        AuditLogService auditLogService
    ) {
        this.rooms = rooms;
        this.messages = messages;
        this.users = users;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<MessageDTO.RoomResponse> listRooms(Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        return rooms.findByParticipantsId(currentUser.getId()).stream().map(MessageDTO.RoomResponse::from).toList();
    }

    @Transactional
    public MessageDTO.RoomResponse createRoom(MessageDTO.RoomRequest request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        ChatRoom room = new ChatRoom();
        applyRoomRequest(room, request);
        room.getParticipants().add(currentUser);
        return MessageDTO.RoomResponse.from(rooms.save(room));
    }

    @Transactional
    public MessageDTO.RoomResponse updateRoom(UUID roomId, MessageDTO.RoomRequest request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        ChatRoom room = findParticipantRoom(roomId, currentUser);
        applyRoomRequest(room, request);
        return MessageDTO.RoomResponse.from(rooms.save(room));
    }

    @Transactional(readOnly = true)
    public List<MessageDTO.Response> listMessages(UUID roomId, int page, int size, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        findParticipantRoom(roomId, currentUser);
        return messages.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, normalizeSize(size))).stream()
            .map(MessageDTO.Response::from)
            .toList();
    }

    @Transactional
    public MessageDTO.Response sendMessage(UUID roomId, MessageDTO.SendRequest request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        ChatRoom room = findParticipantRoom(roomId, currentUser);

        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSender(currentUser);
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
    public MessageDTO.Response markSeen(UUID messageId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        ChatMessage message = messages.findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat message not found"));
        requireParticipant(message.getRoom(), currentUser);
        message.setSeenAt(Instant.now());
        return MessageDTO.Response.from(messages.save(message));
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private ChatRoom findParticipantRoom(UUID roomId, AppUser currentUser) {
        ChatRoom room = rooms.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));
        requireParticipant(room, currentUser);
        return room;
    }

    private void requireParticipant(ChatRoom room, AppUser currentUser) {
        UUID currentUserId = currentUser.getId();
        boolean participant = room.getParticipants().stream()
            .anyMatch(user -> currentUserId.equals(user.getId()));
        if (!participant) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found");
        }
    }

    private void applyRoomRequest(ChatRoom room, MessageDTO.RoomRequest request) {
        room.setName(request.name());
        room.setType(request.type());
        room.setBranch(request.branch());
    }
}
