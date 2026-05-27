package com.familyhub.digital_family_hub.posts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.media.MediaStorageService;
import com.familyhub.digital_family_hub.media.MediaType;
import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
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

class TimelineServiceTest {

    private final MemoryPostRepository posts = mock(MemoryPostRepository.class);
    private final FamilyMemberRepository members = mock(FamilyMemberRepository.class);
    private final MediaAssetRepository mediaAssets = mock(MediaAssetRepository.class);
    private final MediaStorageService mediaStorageService = mock(MediaStorageService.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final TimelineService service = new TimelineService(posts, members, mediaAssets, mediaStorageService, users, auditLogService);

    @Test
    void updatePostAllowsAuthor() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        MemoryPost post = post(postId, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(posts.findById(postId)).thenReturn(Optional.of(post));
        when(posts.save(any(MemoryPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId)).thenReturn(List.of());

        PostDTO.Response response = service.updatePost(postId, request("Updated memory"), principal(author.getEmail()));

        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.authorId()).isEqualTo(author.getId());
        assertThat(response.text()).isEqualTo("Updated memory");
        verify(posts).save(post);
    }

    @Test
    void updatePostAttachesImageMedia() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        MemoryPost post = post(postId, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(posts.findById(postId)).thenReturn(Optional.of(post));
        when(posts.save(any(MemoryPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId))
            .thenReturn(List.of())
            .thenReturn(List.of(media(post, "https://example.com/memory.jpg", "timeline/memory.jpg")));
        when(mediaAssets.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostDTO.Response response = service.updatePost(
            postId,
            requestWithMedia("Updated memory", "https://example.com/memory.jpg", "timeline/memory.jpg", MediaType.IMAGE),
            principal(author.getEmail())
        );

        assertThat(response.media()).hasSize(1);
        assertThat(response.media().getFirst().url()).isEqualTo("https://example.com/memory.jpg");
        verify(mediaAssets).save(any(MediaAsset.class));
    }

    @Test
    void updatePostRejectsVideoMedia() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        MemoryPost post = post(postId, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(posts.findById(postId)).thenReturn(Optional.of(post));
        when(posts.save(any(MemoryPost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.updatePost(
            postId,
            requestWithMedia("Updated memory", "https://example.com/video.mp4", "timeline/video.mp4", MediaType.VIDEO),
            principal(author.getEmail())
        ))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400)
            );
        verify(mediaAssets, never()).save(any());
    }

    @Test
    void updatePostHidesPostsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(posts.findById(postId)).thenReturn(Optional.of(post(postId, otherUser)));

        assertThatThrownBy(() -> service.updatePost(postId, request("Nope"), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(posts, never()).save(any());
    }

    @Test
    void deletePostAllowsAuthor() {
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        MemoryPost post = post(postId, author);
        when(users.findByEmailIgnoreCase(author.getEmail())).thenReturn(Optional.of(author));
        when(posts.findById(postId)).thenReturn(Optional.of(post));
        when(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId)).thenReturn(List.of());

        service.deletePost(postId, principal(author.getEmail()));

        verify(posts).delete(post);
    }

    @Test
    void deletePostAllowsAdminForAnyPost() {
        AppUser admin = user(UUID.randomUUID(), "admin@example.com", UserRole.ADMIN);
        AppUser author = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        MemoryPost post = post(postId, author);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(posts.findById(postId)).thenReturn(Optional.of(post));
        when(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId)).thenReturn(List.of());

        service.deletePost(postId, principal(admin.getEmail()));

        verify(posts).delete(post);
    }

    @Test
    void deletePostHidesPostsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID postId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(posts.findById(postId)).thenReturn(Optional.of(post(postId, otherUser)));

        assertThatThrownBy(() -> service.deletePost(postId, principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(posts, never()).delete(any());
    }

    private Principal principal(String email) {
        return () -> email;
    }

    private AppUser user(UUID id, String email, UserRole role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setName("Family Member");
        user.setRole(role);
        return user;
    }

    private MemoryPost post(UUID id, AppUser author) {
        MemoryPost post = new MemoryPost();
        post.setId(id);
        post.setAuthor(author);
        post.setText("Original memory");
        post.setOccurredAt(Instant.parse("2026-05-28T09:00:00Z"));
        post.setEventType(EventType.EVERYDAY);
        return post;
    }

    private PostDTO.Request request(String text) {
        return new PostDTO.Request(
            text,
            Instant.parse("2026-05-28T10:00:00Z"),
            "Home",
            EventType.FAMILY_GATHERING,
            List.of(),
            null,
            null,
            null
        );
    }

    private PostDTO.Request requestWithMedia(String text, String mediaUrl, String mediaStoragePublicId, MediaType mediaType) {
        return new PostDTO.Request(
            text,
            Instant.parse("2026-05-28T10:00:00Z"),
            "Home",
            EventType.FAMILY_GATHERING,
            List.of(),
            mediaUrl,
            mediaStoragePublicId,
            mediaType
        );
    }

    private MediaAsset media(MemoryPost post, String url, String storagePublicId) {
        MediaAsset mediaAsset = new MediaAsset();
        mediaAsset.setId(UUID.randomUUID());
        mediaAsset.setPost(post);
        mediaAsset.setUrl(url);
        mediaAsset.setStoragePublicId(storagePublicId);
        mediaAsset.setMediaType(MediaType.IMAGE);
        return mediaAsset;
    }
}
