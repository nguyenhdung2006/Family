package com.familyhub.digital_family_hub.posts;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
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
    private final AppUserRepository users;
    private final AuditLogService auditLogService;

    public TimelineService(
        MemoryPostRepository posts,
        FamilyMemberRepository members,
        AppUserRepository users,
        AuditLogService auditLogService
    ) {
        this.posts = posts;
        this.members = members;
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
                .map(PostDTO.Response::from)
                .toList();
        }
        if (year != null) {
            Instant start = LocalDate.of(year, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            return posts.findByOccurredAtBetween(start, end, pageRequest).stream()
                .map(PostDTO.Response::from)
                .toList();
        }
        if (eventType != null) {
            return posts.findByEventType(eventType, pageRequest).stream()
                .map(PostDTO.Response::from)
                .toList();
        }
        return posts.findAllByOrderByOccurredAtDesc(pageRequest).stream()
            .map(PostDTO.Response::from)
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
        auditLogService.dataChange("create", "memory_post", saved.getId());
        return PostDTO.Response.from(saved);
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
        auditLogService.dataChange("update", "memory_post", saved.getId());
        return PostDTO.Response.from(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "timeline-posts", allEntries = true)
    public void deletePost(UUID postId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemoryPost post = findPostForMutation(postId, currentUser);
        posts.delete(post);
        auditLogService.dataChange("delete", "memory_post", postId);
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
