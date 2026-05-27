package com.familyhub.digital_family_hub.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class NotificationServiceTest {

    private final InAppNotificationRepository notifications = mock(InAppNotificationRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final NotificationService service = new NotificationService(notifications, users);

    @Test
    void listNotificationsOnlyReturnsCurrentUsersNotifications() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        InAppNotification notification = notification(UUID.randomUUID(), user);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(notifications.findByRecipientIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(notification));

        List<NotificationDTO.Response> responses = service.listNotifications(principal(user.getEmail()));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(notification.getId());
        verify(notifications).findByRecipientIdOrderByCreatedAtDesc(user.getId());
        verify(notifications, never()).findAll();
    }

    @Test
    void markReadOnlyUpdatesCurrentUsersNotification() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        UUID notificationId = UUID.randomUUID();
        InAppNotification notification = notification(notificationId, user);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notifications.save(any(InAppNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationDTO.Response response = service.markRead(notificationId, principal(user.getEmail()));

        assertThat(response.id()).isEqualTo(notificationId);
        assertThat(response.readAt()).isNotNull();
        verify(notifications).save(notification);
    }

    @Test
    void createNotificationAssignsCurrentUserAsOwner() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(notifications.save(any(InAppNotification.class))).thenAnswer(invocation -> {
            InAppNotification saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        NotificationDTO.Response response = service.createNotification(request("Family dinner"), principal(user.getEmail()));

        assertThat(response.createdById()).isEqualTo(user.getId());
        assertThat(response.title()).isEqualTo("Family dinner");
    }

    @Test
    void updateNotificationAllowsOwner() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        UUID notificationId = UUID.randomUUID();
        InAppNotification notification = notification(notificationId, user);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notifications.save(any(InAppNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationDTO.Response response = service.updateNotification(
            notificationId,
            request("Updated dinner"),
            principal(user.getEmail())
        );

        assertThat(response.id()).isEqualTo(notificationId);
        assertThat(response.createdById()).isEqualTo(user.getId());
        assertThat(response.title()).isEqualTo("Updated dinner");
        verify(notifications).save(notification);
    }

    @Test
    void updateNotificationHidesNotificationsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID notificationId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification(notificationId, otherUser)));

        assertThatThrownBy(() -> service.updateNotification(notificationId, request("Nope"), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(notifications, never()).save(any());
    }

    @Test
    void deleteNotificationAllowsOwner() {
        AppUser user = user(UUID.randomUUID(), "member@example.com");
        UUID notificationId = UUID.randomUUID();
        InAppNotification notification = notification(notificationId, user);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));

        service.deleteNotification(notificationId, principal(user.getEmail()));

        verify(notifications).delete(notification);
    }

    @Test
    void deleteNotificationAllowsAdminForAnyNotification() {
        AppUser admin = user(UUID.randomUUID(), "admin@example.com");
        admin.setRole(UserRole.ADMIN);
        AppUser owner = user(UUID.randomUUID(), "member@example.com");
        UUID notificationId = UUID.randomUUID();
        InAppNotification notification = notification(notificationId, owner);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));

        service.deleteNotification(notificationId, principal(admin.getEmail()));

        verify(notifications).delete(notification);
    }

    @Test
    void deleteNotificationHidesNotificationsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID notificationId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification(notificationId, otherUser)));

        assertThatThrownBy(() -> service.deleteNotification(notificationId, principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(notifications, never()).delete(any());
    }

    @Test
    void markReadHidesNotificationsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com");
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com");
        UUID notificationId = UUID.randomUUID();
        InAppNotification notification = notification(notificationId, otherUser);
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markRead(notificationId, principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(notifications, never()).save(any());
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

    private InAppNotification notification(UUID id, AppUser recipient) {
        InAppNotification notification = new InAppNotification();
        notification.setId(id);
        notification.setRecipient(recipient);
        notification.setType(NotificationType.FAMILY_EVENT);
        notification.setTitle("Family dinner");
        notification.setBody("Dinner at 7");
        return notification;
    }

    private NotificationDTO.Request request(String title) {
        return new NotificationDTO.Request(
            NotificationType.FAMILY_EVENT,
            title,
            "Dinner at 7",
            Instant.parse("2026-05-28T10:00:00Z")
        );
    }
}
