package com.familyhub.digital_family_hub.albums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.media.MediaStorageService;
import com.familyhub.digital_family_hub.media.MediaType;
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

class AlbumServiceTest {

    private final AlbumRepository albums = mock(AlbumRepository.class);
    private final MediaAssetRepository mediaAssets = mock(MediaAssetRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final MediaStorageService mediaStorageService = mock(MediaStorageService.class);
    private final AlbumService service = new AlbumService(albums, mediaAssets, users, mediaStorageService);

    @Test
    void updateAlbumAllowsOwner() {
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        Album album = album(albumId, owner);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        when(albums.findById(albumId)).thenReturn(Optional.of(album));
        when(albums.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlbumDTO.Response response = service.updateAlbum(albumId, request("Updated album"), principal(owner.getEmail()));

        assertThat(response.id()).isEqualTo(albumId);
        assertThat(response.createdById()).isEqualTo(owner.getId());
        assertThat(response.title()).isEqualTo("Updated album");
        verify(albums).save(album);
    }

    @Test
    void updateAlbumHidesAlbumsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(albums.findById(albumId)).thenReturn(Optional.of(album(albumId, otherUser)));

        assertThatThrownBy(() -> service.updateAlbum(albumId, request("Nope"), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(albums, never()).save(any());
    }

    @Test
    void deleteAlbumRemovesMediaRowsBeforeDeletingAlbum() {
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        Album album = album(albumId, owner);
        MediaAsset media = media(UUID.randomUUID(), album, owner);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        when(albums.findById(albumId)).thenReturn(Optional.of(album));
        when(mediaAssets.findByAlbumIdOrderByCapturedAtDesc(albumId)).thenReturn(List.of(media));

        service.deleteAlbum(albumId, principal(owner.getEmail()));

        verify(mediaAssets).deleteAll(List.of(media));
        verify(mediaStorageService).delete(media.getStoragePublicId());
        verify(albums).delete(album);
    }

    @Test
    void attachMediaHidesAlbumsOwnedByAnotherUser() {
        AppUser currentUser = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        AppUser otherUser = user(UUID.randomUUID(), "other@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(albums.findById(albumId)).thenReturn(Optional.of(album(albumId, otherUser)));

        assertThatThrownBy(() -> service.attachMedia(albumId, attachRequest(), principal(currentUser.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(mediaAssets, never()).save(any());
    }

    @Test
    void removeMediaAllowsAdminForAnyAlbum() {
        AppUser admin = user(UUID.randomUUID(), "admin@example.com", UserRole.ADMIN);
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        Album album = album(albumId, owner);
        MediaAsset media = media(mediaId, album, owner);
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(albums.findById(albumId)).thenReturn(Optional.of(album));
        when(mediaAssets.findById(mediaId)).thenReturn(Optional.of(media));

        service.removeMedia(albumId, mediaId, principal(admin.getEmail()));

        verify(mediaStorageService).delete(media.getStoragePublicId());
        verify(mediaAssets).delete(media);
    }

    @Test
    void removeMediaRejectsMediaFromDifferentAlbum() {
        AppUser owner = user(UUID.randomUUID(), "member@example.com", UserRole.MEMBER);
        UUID albumId = UUID.randomUUID();
        UUID otherAlbumId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        Album album = album(albumId, owner);
        MediaAsset media = media(mediaId, album(otherAlbumId, owner), owner);
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        when(albums.findById(albumId)).thenReturn(Optional.of(album));
        when(mediaAssets.findById(mediaId)).thenReturn(Optional.of(media));

        assertThatThrownBy(() -> service.removeMedia(albumId, mediaId, principal(owner.getEmail())))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(404)
            );
        verify(mediaAssets, never()).delete(any());
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

    private Album album(UUID id, AppUser owner) {
        Album album = new Album();
        album.setId(id);
        album.setCreatedBy(owner);
        album.setTitle("Family album");
        album.setDescription("Old photos");
        album.setCategory(AlbumCategory.EVERYDAY);
        return album;
    }

    private MediaAsset media(UUID id, Album album, AppUser uploadedBy) {
        MediaAsset media = new MediaAsset();
        media.setId(id);
        media.setAlbum(album);
        media.setUploadedBy(uploadedBy);
        media.setUrl("https://example.com/photo.jpg");
        media.setStoragePublicId(id + ".jpg");
        media.setMediaType(MediaType.IMAGE);
        media.setCaption("Photo");
        media.setCapturedAt(Instant.parse("2026-05-28T10:00:00Z"));
        return media;
    }

    private AlbumDTO.Request request(String title) {
        return new AlbumDTO.Request(title, "Description", AlbumCategory.TRAVEL);
    }

    private AlbumDTO.AttachMediaRequest attachRequest() {
        return new AlbumDTO.AttachMediaRequest(
            "https://example.com/photo.jpg",
            "public-id",
            MediaType.IMAGE,
            "Photo",
            Instant.parse("2026-05-28T10:00:00Z")
        );
    }
}
