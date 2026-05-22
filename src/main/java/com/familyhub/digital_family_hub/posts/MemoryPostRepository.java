package com.familyhub.digital_family_hub.posts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryPostRepository extends JpaRepository<MemoryPost, UUID> {
    Page<MemoryPost> findAllByOrderByOccurredAtDesc(Pageable pageable);

    List<MemoryPost> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant startInclusive, Instant endExclusive);

    Page<MemoryPost> findByOccurredAtBetween(Instant startInclusive, Instant endExclusive, Pageable pageable);

    List<MemoryPost> findByEventTypeOrderByOccurredAtDesc(EventType eventType);

    Page<MemoryPost> findByEventType(EventType eventType, Pageable pageable);

    Page<MemoryPost> findByAuthorId(UUID authorId, Pageable pageable);
}
