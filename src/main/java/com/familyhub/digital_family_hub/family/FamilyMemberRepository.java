package com.familyhub.digital_family_hub.family;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {
    List<FamilyMember> findByBranchOrderByGenerationLevelAscFullNameAsc(FamilyBranch branch);

    Page<FamilyMember> findByBranch(FamilyBranch branch, Pageable pageable);

    List<FamilyMember> findByDeathDateIsNotNullOrderByDeathDateAsc();
}
