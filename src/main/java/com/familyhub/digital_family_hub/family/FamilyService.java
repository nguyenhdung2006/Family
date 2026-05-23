package com.familyhub.digital_family_hub.family;

import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FamilyService {

    private final FamilyMemberRepository members;
    private final FamilyRelationshipRepository relationships;
    private final AuditLogService auditLogService;

    public FamilyService(
        FamilyMemberRepository members,
        FamilyRelationshipRepository relationships,
        AuditLogService auditLogService
    ) {
        this.members = members;
        this.relationships = relationships;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "family-members", key = "'all:' + #page + ':' + #size")
    public List<FamilyMemberDTO.Response> listMembers(int page, int size) {
        return members.findAll(PageRequest.of(page, normalizeSize(size), Sort.by("generationLevel").ascending()))
            .stream()
            .map(FamilyMemberDTO.Response::from)
            .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "family-members", key = "#branch.name() + ':' + #page + ':' + #size")
    public List<FamilyMemberDTO.Response> listMembersByBranch(FamilyBranch branch, int page, int size) {
        return members.findByBranch(
                branch,
                PageRequest.of(page, normalizeSize(size), Sort.by("generationLevel").ascending().and(Sort.by("fullName")))
            )
            .stream()
            .map(FamilyMemberDTO.Response::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public FamilyMemberDTO.Response getMember(UUID id) {
        return FamilyMemberDTO.Response.from(findMember(id, "Family member not found"));
    }

    @Transactional
    @CacheEvict(cacheNames = "family-members", allEntries = true)
    public FamilyMemberDTO.Response createMember(FamilyMemberDTO.Request request) {
        FamilyMember member = new FamilyMember();
        applyMemberRequest(member, request);
        FamilyMember saved = members.save(member);
        auditLogService.dataChange("create", "family_member", saved.getId());
        return FamilyMemberDTO.Response.from(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "family-members", allEntries = true)
    public FamilyMemberDTO.Response updateMember(UUID id, FamilyMemberDTO.Request request) {
        FamilyMember member = findMember(id, "Family member not found");
        applyMemberRequest(member, request);
        FamilyMember saved = members.save(member);
        auditLogService.dataChange("update", "family_member", saved.getId());
        return FamilyMemberDTO.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RelationshipDTO.Response> listRelationships(UUID memberId) {
        return relationships.findBySourceMemberIdOrTargetMemberId(memberId, memberId).stream()
            .map(RelationshipDTO.Response::from)
            .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "family-members", allEntries = true)
    public RelationshipDTO.Response createRelationship(RelationshipDTO.Request request) {
        validateRelationshipRequest(request);
        FamilyMember source = findMember(request.sourceMemberId(), "Source member not found");
        FamilyMember target = findMember(request.targetMemberId(), "Target member not found");

        FamilyRelationship relationship = new FamilyRelationship();
        applyRelationshipRequest(relationship, source, target, request);
        FamilyRelationship saved = relationships.save(relationship);
        auditLogService.dataChange("create", "family_relationship", saved.getId());
        return RelationshipDTO.Response.from(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "family-members", allEntries = true)
    public RelationshipDTO.Response updateRelationship(UUID id, RelationshipDTO.Request request) {
        validateRelationshipRequest(request);
        FamilyRelationship relationship = relationships.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family relationship not found"));
        FamilyMember source = findMember(request.sourceMemberId(), "Source member not found");
        FamilyMember target = findMember(request.targetMemberId(), "Target member not found");
        applyRelationshipRequest(relationship, source, target, request);
        FamilyRelationship saved = relationships.save(relationship);
        auditLogService.dataChange("update", "family_relationship", saved.getId());
        return RelationshipDTO.Response.from(saved);
    }

    private void applyMemberRequest(FamilyMember member, FamilyMemberDTO.Request request) {
        member.setFullName(request.fullName());
        member.setBirthDate(request.birthDate());
        member.setDeathDate(request.deathDate());
        member.setRoleInFamily(request.roleInFamily());
        member.setBranch(request.branch());
        member.setAvatarUrl(request.avatarUrl());
        member.setBiography(request.biography());
        member.setGenerationLevel(request.generationLevel());
    }

    private void applyRelationshipRequest(
        FamilyRelationship relationship,
        FamilyMember source,
        FamilyMember target,
        RelationshipDTO.Request request
    ) {
        relationship.setSourceMember(source);
        relationship.setTargetMember(target);
        relationship.setType(request.type());
        relationship.setNotes(request.notes());
    }

    private void validateRelationshipRequest(RelationshipDTO.Request request) {
        if (request.sourceMemberId().equals(request.targetMemberId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Relationship members must be different");
        }
    }

    private FamilyMember findMember(UUID id, String message) {
        return members.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }
}
