package com.familyhub.digital_family_hub.albums;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findByCategoryOrderByCreatedAtDesc(AlbumCategory category);
}
