package com.familyhub.digital_family_hub.media;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByAlbumIdOrderByCapturedAtDesc(UUID albumId);

    List<MediaAsset> findByPostIdOrderByCapturedAtDesc(UUID postId);

    List<MediaAsset> findByLinkedMemberIdOrderByCapturedAtDesc(UUID linkedMemberId);
}
