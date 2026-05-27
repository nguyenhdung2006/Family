package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.media.MediaStorageService;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlbumService {

    private final AlbumRepository albums;
    private final MediaAssetRepository mediaAssets;
    private final AppUserRepository users;
    private final MediaStorageService mediaStorageService;

    public AlbumService(
        AlbumRepository albums,
        MediaAssetRepository mediaAssets,
        AppUserRepository users,
        MediaStorageService mediaStorageService
    ) {
        this.albums = albums;
        this.mediaAssets = mediaAssets;
        this.users = users;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO.Response> listAlbums(int page, int size, AlbumCategory category, UUID createdById) {
        PageRequest pageRequest = PageRequest.of(page, normalizeSize(size), Sort.by("createdAt").descending());
        if (category != null) {
            return albums.findByCategory(category, pageRequest).stream().map(AlbumDTO.Response::from).toList();
        }
        if (createdById != null) {
            return albums.findByCreatedById(createdById, pageRequest).stream().map(AlbumDTO.Response::from).toList();
        }
        return albums.findAll(pageRequest).stream().map(AlbumDTO.Response::from).toList();
    }

    @Transactional
    public AlbumDTO.Response createAlbum(AlbumDTO.Request request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        Album album = new Album();
        applyAlbumRequest(album, request);
        album.setCreatedBy(currentUser);
        return AlbumDTO.Response.from(albums.save(album));
    }

    @Transactional
    public AlbumDTO.Response updateAlbum(UUID albumId, AlbumDTO.Request request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        Album album = findAlbumForMutation(albumId, currentUser);
        applyAlbumRequest(album, request);
        return AlbumDTO.Response.from(albums.save(album));
    }

    @Transactional
    public void deleteAlbum(UUID albumId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        Album album = findAlbumForMutation(albumId, currentUser);
        List<MediaAsset> albumMedia = mediaAssets.findByAlbumIdOrderByCapturedAtDesc(albumId);
        albumMedia.forEach(asset -> mediaStorageService.delete(asset.getStoragePublicId()));
        mediaAssets.deleteAll(albumMedia);
        albums.delete(album);
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO.MediaResponse> listAlbumMedia(UUID albumId) {
        return mediaAssets.findByAlbumIdOrderByCapturedAtDesc(albumId).stream()
            .map(AlbumDTO.MediaResponse::from)
            .toList();
    }

    @Transactional
    public AlbumDTO.MediaResponse attachMedia(UUID albumId, AlbumDTO.AttachMediaRequest request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        Album album = findAlbumForMutation(albumId, currentUser);
        MediaAsset asset = new MediaAsset();
        asset.setAlbum(album);
        asset.setUploadedBy(currentUser);
        asset.setUrl(request.url());
        asset.setMediaType(request.mediaType());
        asset.setCaption(request.caption());
        asset.setCapturedAt(request.capturedAt());
        asset.setStoragePublicId(request.storagePublicId());
        return AlbumDTO.MediaResponse.from(mediaAssets.save(asset));
    }

    @Transactional
    public void removeMedia(UUID albumId, UUID mediaId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        findAlbumForMutation(albumId, currentUser);
        MediaAsset asset = mediaAssets.findById(mediaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
        if (asset.getAlbum() == null || !albumId.equals(asset.getAlbum().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
        }
        mediaStorageService.delete(asset.getStoragePublicId());
        mediaAssets.delete(asset);
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }

    private void applyAlbumRequest(Album album, AlbumDTO.Request request) {
        album.setTitle(request.title());
        album.setDescription(request.description());
        album.setCategory(request.category());
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private Album findAlbumForMutation(UUID albumId, AppUser currentUser) {
        Album album = albums.findById(albumId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        if (!canMutateAlbum(album, currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found");
        }
        return album;
    }

    private boolean canMutateAlbum(Album album, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }
        return album.getCreatedBy() != null && album.getCreatedBy().getId().equals(currentUser.getId());
    }
}
