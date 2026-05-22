package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.media.MediaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumRepository albums;
    private final MediaAssetRepository mediaAssets;

    public AlbumController(AlbumRepository albums, MediaAssetRepository mediaAssets) {
        this.albums = albums;
        this.mediaAssets = mediaAssets;
    }

    @GetMapping
    public List<AlbumResponse> listAlbums() {
        return albums.findAll().stream().map(AlbumResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumResponse createAlbum(@Valid @RequestBody CreateAlbumRequest request) {
        Album album = new Album();
        album.setTitle(request.title());
        album.setDescription(request.description());
        album.setCategory(request.category());
        return AlbumResponse.from(albums.save(album));
    }

    @GetMapping("/{albumId}/media")
    public List<MediaAssetResponse> listAlbumMedia(@PathVariable UUID albumId) {
        return mediaAssets.findByAlbumIdOrderByCapturedAtDesc(albumId).stream()
            .map(MediaAssetResponse::from)
            .toList();
    }

    @PostMapping("/{albumId}/media")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetResponse attachMedia(@PathVariable UUID albumId, @Valid @RequestBody AttachMediaRequest request) {
        Album album = albums.findById(albumId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        MediaAsset asset = new MediaAsset();
        asset.setAlbum(album);
        asset.setUrl(request.url());
        asset.setMediaType(request.mediaType());
        asset.setCaption(request.caption());
        asset.setCapturedAt(request.capturedAt());
        asset.setStoragePublicId(request.storagePublicId());
        return MediaAssetResponse.from(mediaAssets.save(asset));
    }

    public record CreateAlbumRequest(
        @NotBlank String title,
        String description,
        @NotNull AlbumCategory category
    ) {
    }

    public record AttachMediaRequest(
        @NotBlank String url,
        String storagePublicId,
        @NotNull MediaType mediaType,
        String caption,
        Instant capturedAt
    ) {
    }

    public record AlbumResponse(UUID id, String title, String description, AlbumCategory category) {
        static AlbumResponse from(Album album) {
            return new AlbumResponse(album.getId(), album.getTitle(), album.getDescription(), album.getCategory());
        }
    }

    public record MediaAssetResponse(
        UUID id,
        String url,
        MediaType mediaType,
        String caption,
        Instant capturedAt
    ) {
        static MediaAssetResponse from(MediaAsset asset) {
            return new MediaAssetResponse(
                asset.getId(),
                asset.getUrl(),
                asset.getMediaType(),
                asset.getCaption(),
                asset.getCapturedAt()
            );
        }
    }
}
