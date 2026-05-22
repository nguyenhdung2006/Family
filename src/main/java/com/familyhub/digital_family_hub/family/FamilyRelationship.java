package com.familyhub.digital_family_hub.family;

import com.familyhub.digital_family_hub.shared.domain.AuditableEntity;
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
    name = "family_relationships",
    indexes = {
        @Index(name = "idx_family_relationship_source", columnList = "source_member_id"),
        @Index(name = "idx_family_relationship_target", columnList = "target_member_id"),
        @Index(name = "idx_family_relationship_type", columnList = "type")
    }
)
public class FamilyRelationship extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_member_id", nullable = false)
    private FamilyMember sourceMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_member_id", nullable = false)
    private FamilyMember targetMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType type;

    @Column(length = 2000)
    private String notes;

    public FamilyMember getSourceMember() {
        return sourceMember;
    }

    public void setSourceMember(FamilyMember sourceMember) {
        this.sourceMember = sourceMember;
    }

    public FamilyMember getTargetMember() {
        return targetMember;
    }

    public void setTargetMember(FamilyMember targetMember) {
        this.targetMember = targetMember;
    }

    public RelationshipType getType() {
        return type;
    }

    public void setType(RelationshipType type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
