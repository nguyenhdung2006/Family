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
        tribute.setTitle(request.title());
        tribute.setStory(request.story());
        return MemorialDTO.TributeResponse.from(tributes.save(tribute));
    }
}
