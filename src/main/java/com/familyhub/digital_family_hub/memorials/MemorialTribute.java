package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "memorial_tributes",
    indexes = {
        @Index(name = "idx_memorial_tributes_member", columnList = "member_id"),
        @Index(name = "idx_memorial_tributes_author", columnList = "author_id")
    }
)
public class MemorialTribute extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private FamilyMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 10000)
    private String story;

    public FamilyMember getMember() {
        return member;
    }

    public void setMember(FamilyMember member) {
        this.member = member;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }
}
