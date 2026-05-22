package com.familyhub.digital_family_hub.memorials;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemorialTributeRepository extends JpaRepository<MemorialTribute, UUID> {
    List<MemorialTribute> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
