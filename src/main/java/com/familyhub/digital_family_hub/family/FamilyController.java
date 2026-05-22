package com.familyhub.digital_family_hub.family;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyMemberRepository members;
    private final FamilyRelationshipRepository relationships;

    public FamilyController(FamilyMemberRepository members, FamilyRelationshipRepository relationships) {
        this.members = members;
        this.relationships = relationships;
    }

    @GetMapping("/members")
    public List<FamilyMemberResponse> listMembers() {
        return members.findAll().stream().map(FamilyMemberResponse::from).toList();
    }

    @GetMapping("/branches/{branch}/members")
    public List<FamilyMemberResponse> listMembersByBranch(@PathVariable FamilyBranch branch) {
        return members.findByBranchOrderByGenerationLevelAscFullNameAsc(branch).stream()
            .map(FamilyMemberResponse::from)
            .toList();
    }

    @GetMapping("/members/{id}")
    public FamilyMemberResponse getMember(@PathVariable UUID id) {
        return members.findById(id)
            .map(FamilyMemberResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found"));
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyMemberResponse createMember(@Valid @RequestBody CreateFamilyMemberRequest request) {
        FamilyMember member = new FamilyMember();
        member.setFullName(request.fullName());
        member.setBirthDate(request.birthDate());
        member.setDeathDate(request.deathDate());
        member.setRoleInFamily(request.roleInFamily());
        member.setBranch(request.branch());
        member.setAvatarUrl(request.avatarUrl());
        member.setBiography(request.biography());
        member.setGenerationLevel(request.generationLevel());
        return FamilyMemberResponse.from(members.save(member));
    }

    @GetMapping("/members/{id}/relationships")
    public List<FamilyRelationshipResponse> listRelationships(@PathVariable UUID id) {
        return relationships.findBySourceMemberIdOrTargetMemberId(id, id).stream()
            .map(FamilyRelationshipResponse::from)
            .toList();
    }

    @PostMapping("/relationships")
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyRelationshipResponse createRelationship(@Valid @RequestBody CreateRelationshipRequest request) {
        FamilyMember source = members.findById(request.sourceMemberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source member not found"));
        FamilyMember target = members.findById(request.targetMemberId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target member not found"));

        FamilyRelationship relationship = new FamilyRelationship();
        relationship.setSourceMember(source);
        relationship.setTargetMember(target);
        relationship.setType(request.type());
        relationship.setNotes(request.notes());
        return FamilyRelationshipResponse.from(relationships.save(relationship));
    }

    public record CreateFamilyMemberRequest(
        @NotBlank String fullName,
        LocalDate birthDate,
        LocalDate deathDate,
        String roleInFamily,
        @NotNull FamilyBranch branch,
        String avatarUrl,
        String biography,
        int generationLevel
    ) {
    }

    public record CreateRelationshipRequest(
        @NotNull UUID sourceMemberId,
        @NotNull UUID targetMemberId,
        @NotNull RelationshipType type,
        String notes
    ) {
    }

    public record FamilyMemberResponse(
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
        static FamilyMemberResponse from(FamilyMember member) {
            return new FamilyMemberResponse(
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

    public record FamilyRelationshipResponse(
        UUID id,
        UUID sourceMemberId,
        UUID targetMemberId,
        RelationshipType type,
        String notes
    ) {
        static FamilyRelationshipResponse from(FamilyRelationship relationship) {
            return new FamilyRelationshipResponse(
                relationship.getId(),
                relationship.getSourceMember().getId(),
                relationship.getTargetMember().getId(),
                relationship.getType(),
                relationship.getNotes()
            );
        }
    }
}
