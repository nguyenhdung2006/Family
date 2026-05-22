package com.familyhub.digital_family_hub.media;

import com.familyhub.digital_family_hub.albums.Album;
import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.posts.MemoryPost;
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
import java.time.Instant;

@Entity
@Table(
    name = "media_assets",
    indexes = {
        @Index(name = "idx_media_assets_media_type", columnList = "mediaType"),
        @Index(name = "idx_media_assets_captured_at", columnList = "capturedAt")
    }
)
public class MediaAsset extends AuditableEntity {

    @Column(nullable = false, length = 2000)
    private String url;

    private String storagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(length = 2000)
    private String caption;

    private Instant capturedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private AppUser uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private MemoryPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_member_id")
    private FamilyMember linkedMember;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStoragePublicId() {
        return storagePublicId;
    }

    public void setStoragePublicId(String storagePublicId) {
        this.storagePublicId = storagePublicId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }

    public AppUser getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(AppUser uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public MemoryPost getPost() {
        return post;
    }

    public void setPost(MemoryPost post) {
        this.post = post;
    }

    public FamilyMember getLinkedMember() {
        return linkedMember;
    }

    public void setLinkedMember(FamilyMember linkedMember) {
        this.linkedMember = linkedMember;
    }
}
