package com.familyhub.digital_family_hub.posts;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.media.MediaAsset;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.media.MediaStorageService;
import com.familyhub.digital_family_hub.media.MediaType;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TimelineService {

    private final MemoryPostRepository posts;
    private final FamilyMemberRepository members;
    private final MediaAssetRepository mediaAssets;
    private final MediaStorageService mediaStorageService;
    private final AppUserRepository users;
    private final AuditLogService auditLogService;

    public TimelineService(
        MemoryPostRepository posts,
        FamilyMemberRepository members,
        MediaAssetRepository mediaAssets,
        MediaStorageService mediaStorageService,
        AppUserRepository users,
        AuditLogService auditLogService
    ) {
        this.posts = posts;
        this.members = members;
        this.mediaAssets = mediaAssets;
        this.mediaStorageService = mediaStorageService;
        this.users = users;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "timeline-posts",
        key = "#page + ':' + #size + ':' + #year + ':' + #eventType + ':' + #authorId"
    )
    public List<PostDTO.Response> listPosts(int page, int size, Integer year, EventType eventType, UUID authorId) {
        PageRequest pageRequest = PageRequest.of(page, normalizeSize(size), Sort.by("occurredAt").descending());
        if (authorId != null) {
            return posts.findByAuthorId(authorId, pageRequest).stream()
                .map(this::postResponse)
                .toList();
        }
        if (year != null) {
            Instant start = LocalDate.of(year, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            return posts.findByOccurredAtBetween(start, end, pageRequest).stream()
                .map(this::postResponse)
                .toList();
        }
        if (eventType != null) {
            return posts.findByEventType(eventType, pageRequest).stream()
                .map(this::postResponse)
                .toList();
        }
        return posts.findAllByOrderByOccurredAtDesc(pageRequest).stream()
            .map(this::postResponse)
            .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "timeline-posts", allEntries = true)
    public PostDTO.Response createPost(PostDTO.Request request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemoryPost post = new MemoryPost();
        post.setAuthor(currentUser);
        post.setText(request.text());
        post.setOccurredAt(request.occurredAt());
        post.setLocationName(request.locationName());
        post.setEventType(request.eventType());
        for (UUID memberId : request.taggedMemberIds()) {
            post.getTaggedMembers().add(members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagged member not found")));
        }
        MemoryPost saved = posts.save(post);
        syncPostMedia(saved, request, currentUser);
        auditLogService.dataChange("create", "memory_post", saved.getId());
        return postResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "timeline-posts", allEntries = true)
    public PostDTO.Response updatePost(UUID postId, PostDTO.Request request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemoryPost post = findPostForMutation(postId, currentUser);
        post.setText(request.text());
        post.setOccurredAt(request.occurredAt());
        post.setLocationName(request.locationName());
        post.setEventType(request.eventType());
        post.getTaggedMembers().clear();
        for (UUID memberId : request.taggedMemberIds()) {
            post.getTaggedMembers().add(members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagged member not found")));
        }
        MemoryPost saved = posts.save(post);
        syncPostMedia(saved, request, currentUser);
        auditLogService.dataChange("update", "memory_post", saved.getId());
        return postResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "timeline-posts", allEntries = true)
    public void deletePost(UUID postId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemoryPost post = findPostForMutation(postId, currentUser);
        deletePostMedia(post.getId());
        posts.delete(post);
        auditLogService.dataChange("delete", "memory_post", postId);
    }

    private PostDTO.Response postResponse(MemoryPost post) {
        return PostDTO.Response.from(post, mediaAssets.findByPostIdOrderByCapturedAtDesc(post.getId()));
    }

    private void syncPostMedia(MemoryPost post, PostDTO.Request request, AppUser currentUser) {
        List<MediaAsset> existingMedia = mediaAssets.findByPostIdOrderByCapturedAtDesc(post.getId());
        String mediaUrl = trimToNull(request.mediaUrl());
        if (mediaUrl == null) {
            deleteMedia(existingMedia);
            return;
        }
        MediaType mediaType = request.mediaType() == null ? MediaType.IMAGE : request.mediaType();
        if (mediaType != MediaType.IMAGE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timeline memories support image attachments only");
        }
        String storagePublicId = trimToNull(request.mediaStoragePublicId());
        if (
            existingMedia.size() == 1
                && mediaUrl.equals(existingMedia.getFirst().getUrl())
                && equalsNullable(storagePublicId, existingMedia.getFirst().getStoragePublicId())
                && mediaType == existingMedia.getFirst().getMediaType()
        ) {
            return;
        }
        deleteMedia(existingMedia);
        MediaAsset mediaAsset = new MediaAsset();
        mediaAsset.setPost(post);
        mediaAsset.setUploadedBy(currentUser);
        mediaAsset.setUrl(mediaUrl);
        mediaAsset.setStoragePublicId(storagePublicId);
        mediaAsset.setMediaType(mediaType);
        mediaAssets.save(mediaAsset);
    }

    private void deletePostMedia(UUID postId) {
        deleteMedia(mediaAssets.findByPostIdOrderByCapturedAtDesc(postId));
    }

    private void deleteMedia(List<MediaAsset> media) {
        media.forEach(asset -> mediaStorageService.delete(asset.getStoragePublicId()));
        mediaAssets.deleteAll(media);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private boolean equalsNullable(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private MemoryPost findPostForMutation(UUID postId, AppUser currentUser) {
        MemoryPost post = posts.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!canMutatePost(post, currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return post;
    }

    private boolean canMutatePost(MemoryPost post, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }
        return post.getAuthor() != null && post.getAuthor().getId().equals(currentUser.getId());
    }
}
