package com.familyhub.digital_family_hub.albums;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findByCategoryOrderByCreatedAtDesc(AlbumCategory category);

    Page<Album> findByCategory(AlbumCategory category, Pageable pageable);

    Page<Album> findByCreatedById(UUID createdById, Pageable pageable);
}
