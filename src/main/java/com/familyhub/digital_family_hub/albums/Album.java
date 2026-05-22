package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "albums",
    indexes = {
        @Index(name = "idx_albums_category", columnList = "category"),
        @Index(name = "idx_albums_title", columnList = "title")
    }
)
public class Album extends AuditableEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlbumCategory category = AlbumCategory.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlbumCategory getCategory() {
        return category;
    }

    public void setCategory(AlbumCategory category) {
        this.category = category;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }
}
