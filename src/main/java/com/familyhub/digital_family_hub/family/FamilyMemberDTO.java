package com.familyhub.digital_family_hub.family;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class FamilyMemberDTO {

    private FamilyMemberDTO() {
    }

    public record Request(
        @NotBlank @Size(max = 255) String fullName,
        LocalDate birthDate,
        LocalDate deathDate,
        @Size(max = 255) String roleInFamily,
        @NotNull FamilyBranch branch,
        @Size(max = 2000) String avatarUrl,
        @Size(max = 10000) String biography,
        @Min(0) int generationLevel
    ) {
    }

    public record Response(
        UUID id,
        String fullName,
        LocalDate birthDate,
        LocalDate deathDate,
        String roleInFamily,
        FamilyBranch branch,
        String avatarUrl,
        String biography,
        int generationLevel,
        boolean deceased
    ) {
        public static Response from(FamilyMember member) {
            return new Response(
                member.getId(),
                member.getFullName(),
                member.getBirthDate(),
                member.getDeathDate(),
                member.getRoleInFamily(),
                member.getBranch(),
                member.getAvatarUrl(),
                member.getBiography(),
                member.getGenerationLevel(),
                member.isDeceased()
            );
        }
    }
}
