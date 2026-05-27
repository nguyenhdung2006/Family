package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemorialService {

    private final FamilyMemberRepository members;
    private final MemorialTributeRepository tributes;
    private final AppUserRepository users;

    public MemorialService(FamilyMemberRepository members, MemorialTributeRepository tributes, AppUserRepository users) {
        this.members = members;
        this.tributes = tributes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<MemorialDTO.MemorialMemberResponse> listMemorialMembers() {
        return members.findByDeathDateIsNotNullOrderByDeathDateAsc().stream()
            .map(MemorialDTO.MemorialMemberResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MemorialDTO.TributeResponse> listTributes(UUID memberId) {
        return tributes.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
            .map(MemorialDTO.TributeResponse::from)
            .toList();
    }

    @Transactional
    public MemorialDTO.TributeResponse createTribute(
        UUID memberId,
        MemorialDTO.TributeRequest request,
        Principal principal
    ) {
        AppUser currentUser = resolveCurrentUser(principal);
        FamilyMember member = members.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found"));
        if (!member.isDeceased()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tributes are only enabled for deceased members");
        }
        MemorialTribute tribute = new MemorialTribute();
        tribute.setMember(member);
        tribute.setAuthor(currentUser);
        applyTributeRequest(tribute, request);
        return MemorialDTO.TributeResponse.from(tributes.save(tribute));
    }

    @Transactional
    public MemorialDTO.TributeResponse updateTribute(
        UUID memberId,
        UUID tributeId,
        MemorialDTO.TributeRequest request,
        Principal principal
    ) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemorialTribute tribute = findTributeForMutation(memberId, tributeId, currentUser);
        applyTributeRequest(tribute, request);
        return MemorialDTO.TributeResponse.from(tributes.save(tribute));
    }

    @Transactional
    public void deleteTribute(UUID memberId, UUID tributeId, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        MemorialTribute tribute = findTributeForMutation(memberId, tributeId, currentUser);
        tributes.delete(tribute);
    }

    private void applyTributeRequest(MemorialTribute tribute, MemorialDTO.TributeRequest request) {
        tribute.setTitle(request.title());
        tribute.setStory(request.story());
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private MemorialTribute findTributeForMutation(UUID memberId, UUID tributeId, AppUser currentUser) {
        MemorialTribute tribute = tributes.findById(tributeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribute not found"));
        if (tribute.getMember() == null || !tribute.getMember().getId().equals(memberId) || !canMutateTribute(tribute, currentUser)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribute not found");
        }
        return tribute;
    }

    private boolean canMutateTribute(MemorialTribute tribute, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }
        return tribute.getAuthor() != null && tribute.getAuthor().getId().equals(currentUser.getId());
    }
}
