package com.familyhub.digital_family_hub.media;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {
    MediaUploadDTO.Response upload(MultipartFile file);

    void delete(String storagePublicId);
}
