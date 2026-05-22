package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/memorials")
public class MemorialController {

    private final FamilyMemberRepository members;
    private final MemorialTributeRepository tributes;

    public MemorialController(FamilyMemberRepository members, MemorialTributeRepository tributes) {
        this.members = members;
        this.tributes = tributes;
    }

    @GetMapping
    public List<MemorialMemberResponse> listMemorialMembers() {
        return members.findByDeathDateIsNotNullOrderByDeathDateAsc().stream()
            .map(MemorialMemberResponse::from)
            .toList();
    }

    @GetMapping("/{memberId}/tributes")
    public List<TributeResponse> listTributes(@PathVariable UUID memberId) {
        return tributes.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
            .map(TributeResponse::from)
            .toList();
    }

    @PostMapping("/{memberId}/tributes")
    @ResponseStatus(HttpStatus.CREATED)
    public TributeResponse createTribute(
        @PathVariable UUID memberId,
        @Valid @RequestBody CreateTributeRequest request
    ) {
        FamilyMember member = members.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found"));
        if (!member.isDeceased()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tributes are only enabled for deceased members");
        }
        MemorialTribute tribute = new MemorialTribute();
        tribute.setMember(member);
        tribute.setTitle(request.title());
        tribute.setStory(request.story());
        return TributeResponse.from(tributes.save(tribute));
    }

    public record CreateTributeRequest(@NotBlank String title, @NotBlank String story) {
    }

    public record MemorialMemberResponse(UUID id, String fullName, String roleInFamily) {
        static MemorialMemberResponse from(FamilyMember member) {
            return new MemorialMemberResponse(member.getId(), member.getFullName(), member.getRoleInFamily());
        }
    }

    public record TributeResponse(UUID id, UUID memberId, String title, String story) {
        static TributeResponse from(MemorialTribute tribute) {
            return new TributeResponse(
                tribute.getId(),
                tribute.getMember().getId(),
                tribute.getTitle(),
                tribute.getStory()
            );
        }
    }
}
