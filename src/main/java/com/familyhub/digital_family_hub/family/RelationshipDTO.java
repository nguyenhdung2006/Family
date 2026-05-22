package com.familyhub.digital_family_hub.family;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class RelationshipDTO {

    private RelationshipDTO() {
    }

    public record Request(
        @NotNull UUID sourceMemberId,
        @NotNull UUID targetMemberId,
        @NotNull RelationshipType type,
        @Size(max = 2000) String notes
    ) {
    }

    public record Response(
        UUID id,
        UUID sourceMemberId,
        UUID targetMemberId,
        RelationshipType type,
        String notes
    ) {
        public static Response from(FamilyRelationship relationship) {
            return new Response(
                relationship.getId(),
                relationship.getSourceMember().getId(),
                relationship.getTargetMember().getId(),
                relationship.getType(),
                relationship.getNotes()
            );
        }
    }
}
