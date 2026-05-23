package com.familyhub.digital_family_hub.memorials;

import com.familyhub.digital_family_hub.family.FamilyMember;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
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

    public MemorialService(FamilyMemberRepository members, MemorialTributeRepository tributes) {
        this.members = members;
        this.tributes = tributes;
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
    public MemorialDTO.TributeResponse createTribute(UUID memberId, MemorialDTO.TributeRequest request) {
        FamilyMember member = members.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found"));
        if (!member.isDeceased()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tributes are only enabled for deceased members");
        }
        MemorialTribute tribute = new MemorialTribute();
        tribute.setMember(member);
        applyTributeRequest(tribute, request);
        return MemorialDTO.TributeResponse.from(tributes.save(tribute));
    }

    @Transactional
    public MemorialDTO.TributeResponse updateTribute(
        UUID memberId,
        UUID tributeId,
        MemorialDTO.TributeRequest request
    ) {
        MemorialTribute tribute = tributes.findById(tributeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribute not found"));
        if (!tribute.getMember().getId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribute not found for member");
        }
        applyTributeRequest(tribute, request);
        return MemorialDTO.TributeResponse.from(tributes.save(tribute));
    }

    private void applyTributeRequest(MemorialTribute tribute, MemorialDTO.TributeRequest request) {
        tribute.setTitle(request.title());
        tribute.setStory(request.story());
    }
}
