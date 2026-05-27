package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AlbumDTO {

    private AlbumDTO() {
    }

    public record Request(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 3000) String description,
        @NotNull AlbumCategory category
    ) {
    }

    public record Response(
        UUID id,
        UUID createdById,
        String createdByName,
        String title,
        String description,
        AlbumCategory category
    ) {
        public static Response from(Album album) {
            return new Response(
                album.getId(),
                album.getCreatedBy() != null ? album.getCreatedBy().getId() : null,
                album.getCreatedBy() != null ? album.getCreatedBy().getName() : null,
                album.getTitle(),
                album.getDescription(),
                album.getCategory()
            );
        }
    }

    public record AttachMediaRequest(
        @NotBlank @Size(max = 2000) String url,
        @Size(max = 255) String storagePublicId,
        @NotNull MediaType mediaType,
        @Size(max = 2000) String caption,
        Instant capturedAt
    ) {
    }

    public record MediaResponse(
        UUID id,
        String url,
        MediaType mediaType,
        String caption,
        Instant capturedAt,
        UUID albumId,
        UUID uploadedById
    ) {
        public static MediaResponse from(MediaAsset asset) {
            return new MediaResponse(
                asset.getId(),
                asset.getUrl(),
                asset.getMediaType(),
                asset.getCaption(),
                asset.getCapturedAt(),
                asset.getAlbum() != null ? asset.getAlbum().getId() : null,
                asset.getUploadedBy() != null ? asset.getUploadedBy().getId() : null
            );
        }
    }
}
