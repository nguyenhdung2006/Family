package com.familyhub.digital_family_hub.family;

import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
import com.familyhub.digital_family_hub.users.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(
    name = "family_members",
    indexes = {
        @Index(name = "idx_family_members_full_name", columnList = "fullName"),
        @Index(name = "idx_family_members_branch", columnList = "branch"),
        @Index(name = "idx_family_members_birth_date", columnList = "birthDate")
    }
)
public class FamilyMember extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String fullName;

    private LocalDate birthDate;

    private LocalDate deathDate;

    private String roleInFamily;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyBranch branch;

    private String avatarUrl;

    @Column(length = 10000)
    private String biography;

    @Column(nullable = false)
    private int generationLevel;

    public boolean isDeceased() {
        return deathDate != null;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getRoleInFamily() {
        return roleInFamily;
    }

    public void setRoleInFamily(String roleInFamily) {
        this.roleInFamily = roleInFamily;
    }

    public FamilyBranch getBranch() {
        return branch;
    }

    public void setBranch(FamilyBranch branch) {
        this.branch = branch;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public int getGenerationLevel() {
        return generationLevel;
    }

    public void setGenerationLevel(int generationLevel) {
        this.generationLevel = generationLevel;
    }
}
