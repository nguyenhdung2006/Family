package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memorials")
public class MemorialController {

    private final MemorialService memorialService;

    public MemorialController(MemorialService memorialService) {
        this.memorialService = memorialService;
    }

    @GetMapping
    public ApiResponse<List<MemorialDTO.MemorialMemberResponse>> listMemorialMembers() {
        return ApiResponse.ok(memorialService.listMemorialMembers());
    }

    @GetMapping("/{memberId}/tributes")
    public ApiResponse<List<MemorialDTO.TributeResponse>> listTributes(@PathVariable UUID memberId) {
        return ApiResponse.ok(memorialService.listTributes(memberId));
    }

    @PostMapping("/{memberId}/tributes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemorialDTO.TributeResponse> createTribute(
        @PathVariable UUID memberId,
        @Valid @RequestBody MemorialDTO.TributeRequest request,
        Principal principal
    ) {
        return ApiResponse.created(memorialService.createTribute(memberId, request, principal));
    }

    @PutMapping("/{memberId}/tributes/{tributeId}")
    public ApiResponse<MemorialDTO.TributeResponse> updateTribute(
        @PathVariable UUID memberId,
        @PathVariable UUID tributeId,
        @Valid @RequestBody MemorialDTO.TributeRequest request
    ) {
        return ApiResponse.ok(memorialService.updateTribute(memberId, tributeId, request));
    }
}
