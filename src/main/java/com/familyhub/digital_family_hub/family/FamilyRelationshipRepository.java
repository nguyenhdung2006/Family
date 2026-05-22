package com.familyhub.digital_family_hub.family;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRelationshipRepository extends JpaRepository<FamilyRelationship, UUID> {
    List<FamilyRelationship> findBySourceMemberIdOrTargetMemberId(UUID sourceMemberId, UUID targetMemberId);
}
