package com.familyhub.digital_family_hub.media;

public final class MediaUploadDTO {

    private MediaUploadDTO() {
    }

    public record Response(
        String url,
        String storagePublicId,
        MediaType mediaType,
        String contentType,
        long sizeBytes
    ) {
    }
}
