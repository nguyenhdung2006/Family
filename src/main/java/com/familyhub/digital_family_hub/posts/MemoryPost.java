package com.familyhub.digital_family_hub.posts;

import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "memory_posts",
    indexes = {
        @Index(name = "idx_memory_posts_occurred_at", columnList = "occurredAt"),
        @Index(name = "idx_memory_posts_event_type", columnList = "eventType")
    }
)
public class MemoryPost extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false, length = 8000)
    private String text;

    @Column(nullable = false)
    private Instant occurredAt;

    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType = EventType.EVERYDAY;

    @ManyToMany
    @JoinTable(
        name = "memory_post_tagged_members",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<FamilyMember> taggedMembers = new LinkedHashSet<>();

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Set<FamilyMember> getTaggedMembers() {
        return taggedMembers;
    }
}
