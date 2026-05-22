package com.familyhub.digital_family_hub.posts;

import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final MemoryPostRepository posts;
    private final FamilyMemberRepository members;

    public TimelineController(MemoryPostRepository posts, FamilyMemberRepository members) {
        this.posts = posts;
        this.members = members;
    }

    @GetMapping("/posts")
    public List<MemoryPostResponse> listPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) EventType eventType
    ) {
        if (year != null) {
            Instant start = LocalDate.of(year, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            return posts.findByOccurredAtBetweenOrderByOccurredAtDesc(start, end).stream()
                .map(MemoryPostResponse::from)
                .toList();
        }
        if (eventType != null) {
            return posts.findByEventTypeOrderByOccurredAtDesc(eventType).stream()
                .map(MemoryPostResponse::from)
                .toList();
        }
        return posts.findAllByOrderByOccurredAtDesc(PageRequest.of(page, Math.min(size, 50))).stream()
            .map(MemoryPostResponse::from)
            .toList();
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public MemoryPostResponse createPost(@Valid @RequestBody CreateMemoryPostRequest request) {
        MemoryPost post = new MemoryPost();
        post.setText(request.text());
        post.setOccurredAt(request.occurredAt());
        post.setLocationName(request.locationName());
        post.setEventType(request.eventType());
        for (UUID memberId : request.taggedMemberIds()) {
            post.getTaggedMembers().add(members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagged member not found")));
        }
        return MemoryPostResponse.from(posts.save(post));
    }

    public record CreateMemoryPostRequest(
        @NotBlank String text,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant occurredAt,
        String locationName,
        @NotNull EventType eventType,
        List<UUID> taggedMemberIds
    ) {
        public CreateMemoryPostRequest {
            taggedMemberIds = taggedMemberIds == null ? List.of() : List.copyOf(taggedMemberIds);
        }
    }

    public record MemoryPostResponse(
        UUID id,
        String text,
        Instant occurredAt,
        String locationName,
        EventType eventType,
        List<UUID> taggedMemberIds
    ) {
        static MemoryPostResponse from(MemoryPost post) {
            return new MemoryPostResponse(
                post.getId(),
                post.getText(),
                post.getOccurredAt(),
                post.getLocationName(),
                post.getEventType(),
                post.getTaggedMembers().stream().map(member -> member.getId()).toList()
            );
        }
    }
}
