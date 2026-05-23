package com.familyhub.digital_family_hub.family;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/members")
    public ApiResponse<List<FamilyMemberDTO.Response>> listMembers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.ok(familyService.listMembers(page, size));
    }

    @GetMapping("/branches/{branch}/members")
    public ApiResponse<List<FamilyMemberDTO.Response>> listMembersByBranch(
        @PathVariable FamilyBranch branch,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.ok(familyService.listMembersByBranch(branch, page, size));
    }

    @GetMapping("/members/{id}")
    public ApiResponse<FamilyMemberDTO.Response> getMember(@PathVariable UUID id) {
        return ApiResponse.ok(familyService.getMember(id));
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FamilyMemberDTO.Response> createMember(@Valid @RequestBody FamilyMemberDTO.Request request) {
        return ApiResponse.created(familyService.createMember(request));
    }

    @PutMapping("/members/{id}")
    public ApiResponse<FamilyMemberDTO.Response> updateMember(
        @PathVariable UUID id,
        @Valid @RequestBody FamilyMemberDTO.Request request
    ) {
        return ApiResponse.ok(familyService.updateMember(id, request));
    }

    @GetMapping("/members/{id}/relationships")
    public ApiResponse<List<RelationshipDTO.Response>> listRelationships(@PathVariable UUID id) {
        return ApiResponse.ok(familyService.listRelationships(id));
    }

    @PostMapping("/relationships")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RelationshipDTO.Response> createRelationship(@Valid @RequestBody RelationshipDTO.Request request) {
        return ApiResponse.created(familyService.createRelationship(request));
    }

    @PutMapping("/relationships/{id}")
    public ApiResponse<RelationshipDTO.Response> updateRelationship(
        @PathVariable UUID id,
        @Valid @RequestBody RelationshipDTO.Request request
    ) {
        return ApiResponse.ok(familyService.updateRelationship(id, request));
    }
}
