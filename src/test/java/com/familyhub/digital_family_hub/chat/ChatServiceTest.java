package com.familyhub.digital_family_hub.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;

class ChatServiceTest {

    private final ChatRoomRepository rooms = mock(ChatRoomRepository.class);
    private final ChatMessageRepository messages = mock(ChatMessageRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final ChatService service = new ChatService(rooms, messages, users, messagingTemplate, auditLogService);

    @Test
    void listRoomsOnlyReturnsRoomsWhereCurrentUserIsParticipant() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        ChatRoom room = room(UUID.randomUUID(), "Family room", user);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(rooms.findByParticipantsId(user.getId())).thenReturn(List.of(room));

        List<MessageDTO.RoomResponse> responses = service.listRooms(principal(user.getEmail()));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(room.getId());
        verify(rooms).findByParticipantsId(user.getId());
        verify(rooms, never()).findAll();
    }

    @Test
    void createRoomAddsCreatorAsParticipant() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(rooms.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        MessageDTO.RoomResponse response = service.createRoom(
            new MessageDTO.RoomRequest("Family room", ChatRoomType.GROUP, null),
            principal(user.getEmail())
        );

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(rooms).save(roomCaptor.capture());
        assertThat(response.id()).isNotNull();
        assertThat(roomCaptor.getValue().getParticipants()).contains(user);
    }

    @Test
    void listMessagesRequiresCurrentUserToBeParticipant() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID roomId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(rooms.findById(roomId)).thenReturn(Optional.of(room(roomId, "Other room", otherUser)));

        assertThatThrownBy(() -> service.listMessages(roomId, 0, 50, principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(messages, never()).findByRoomIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void sendMessageRequiresCurrentUserToBeParticipant() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID roomId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(rooms.findById(roomId)).thenReturn(Optional.of(room(roomId, "Other room", otherUser)));

        assertThatThrownBy(() -> service.sendMessage(
            roomId,
            new MessageDTO.SendRequest(MessageType.TEXT, "hello", null),
            principal(currentUser.getEmail())
        ))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(messages, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void updateRoomRequiresCurrentUserToBeParticipant() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID roomId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(rooms.findById(roomId)).thenReturn(Optional.of(room(roomId, "Other room", otherUser)));

        assertThatThrownBy(() -> service.updateRoom(
            roomId,
            new MessageDTO.RoomRequest("Renamed", ChatRoomType.GROUP, null),
            principal(currentUser.getEmail())
        ))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(rooms, never()).save(any());
    }

    @Test
    void markSeenRequiresCurrentUserToBeParticipantOfMessageRoom() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID());
        message.setRoom(room(UUID.randomUUID(), "Other room", otherUser));
        message.setSender(otherUser);
        message.setType(MessageType.TEXT);
        message.setBody("hello");
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(messages.findById(message.getId())).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.markSeen(message.getId(), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(messages, never()).save(any());
    }

    private Principal principal(String email) {
        return () -> email;
    }

    private AppUser user(UUID id, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setName("Family Member");
        return user;
    }

    private ChatRoom room(UUID id, String name, AppUser participant) {
        ChatRoom room = new ChatRoom();
        room.setId(id);
        room.setName(name);
        room.setType(ChatRoomType.GROUP);
        room.getParticipants().add(participant);
        return room;
    }
}
