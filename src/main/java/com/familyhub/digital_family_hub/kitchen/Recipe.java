package com.familyhub.digital_family_hub.kitchen;

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
    name = "recipes",
    indexes = {
        @Index(name = "idx_recipes_title", columnList = "title"),
        @Index(name = "idx_recipes_created_by", columnList = "created_by_id")
    }
)
public class Recipe extends AuditableEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false, length = 10000)
    private String ingredients;

    @Column(nullable = false, length = 10000)
    private String instructions;

    private String videoUrl;

    @Column(length = 5000)
    private String notesFromElders;

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

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getNotesFromElders() {
        return notesFromElders;
    }

    public void setNotesFromElders(String notesFromElders) {
        this.notesFromElders = notesFromElders;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }
}
