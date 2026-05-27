package com.familyhub.digital_family_hub.posts;

import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public final class PostDTO {

    private PostDTO() {
    }

    public record Request(
        @NotBlank @Size(max = 8000) String text,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant occurredAt,
        @Size(max = 255) String locationName,
        @NotNull EventType eventType,
        List<UUID> taggedMemberIds,
        @Size(max = 2000) String mediaUrl,
        @Size(max = 255) String mediaStoragePublicId,
        MediaType mediaType
    ) {
        public Request {
            taggedMemberIds = taggedMemberIds == null ? List.of() : List.copyOf(taggedMemberIds);
        }
    }

    public record Response(
        UUID id,
        UUID authorId,
        String authorName,
        String text,
        Instant occurredAt,
        String locationName,
        EventType eventType,
        List<UUID> taggedMemberIds,
        List<MediaResponse> media
    ) {
        public static Response from(MemoryPost post, List<MediaAsset> mediaAssets) {
            return new Response(
                post.getId(),
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getName() : null,
                post.getText(),
                post.getOccurredAt(),
                post.getLocationName(),
                post.getEventType(),
                post.getTaggedMembers().stream().map(member -> member.getId()).toList(),
                mediaAssets.stream().map(MediaResponse::from).toList()
            );
        }
    }

    public record MediaResponse(
        UUID id,
        String url,
        String storagePublicId,
        MediaType mediaType
    ) {
        public static MediaResponse from(MediaAsset mediaAsset) {
            return new MediaResponse(
                mediaAsset.getId(),
                mediaAsset.getUrl(),
                mediaAsset.getStoragePublicId(),
                mediaAsset.getMediaType()
            );
        }
    }
}
