package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
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

    public AlbumService(AlbumRepository albums, MediaAssetRepository mediaAssets) {
        this.albums = albums;
        this.mediaAssets = mediaAssets;
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
    public AlbumDTO.Response createAlbum(AlbumDTO.Request request) {
        Album album = new Album();
        album.setTitle(request.title());
        album.setDescription(request.description());
        album.setCategory(request.category());
        return AlbumDTO.Response.from(albums.save(album));
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO.MediaResponse> listAlbumMedia(UUID albumId) {
        return mediaAssets.findByAlbumIdOrderByCapturedAtDesc(albumId).stream()
            .map(AlbumDTO.MediaResponse::from)
            .toList();
    }

    @Transactional
    public AlbumDTO.MediaResponse attachMedia(UUID albumId, AlbumDTO.AttachMediaRequest request) {
        Album album = albums.findById(albumId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        MediaAsset asset = new MediaAsset();
        asset.setAlbum(album);
        asset.setUrl(request.url());
        asset.setMediaType(request.mediaType());
        asset.setCaption(request.caption());
        asset.setCapturedAt(request.capturedAt());
        asset.setStoragePublicId(request.storagePublicId());
        return AlbumDTO.MediaResponse.from(mediaAssets.save(asset));
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }
}
