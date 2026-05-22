package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.family.FamilyMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class MemorialDTO {

    private MemorialDTO() {
    }

    public record TributeRequest(@NotBlank @Size(max = 255) String title, @NotBlank @Size(max = 10000) String story) {
    }

    public record MemorialMemberResponse(UUID id, String fullName, String roleInFamily) {
        public static MemorialMemberResponse from(FamilyMember member) {
            return new MemorialMemberResponse(member.getId(), member.getFullName(), member.getRoleInFamily());
        }
    }

    public record TributeResponse(UUID id, UUID memberId, String title, String story) {
        public static TributeResponse from(MemorialTribute tribute) {
            return new TributeResponse(
                tribute.getId(),
                tribute.getMember().getId(),
                tribute.getTitle(),
                tribute.getStory()
            );
        }
    }
}
