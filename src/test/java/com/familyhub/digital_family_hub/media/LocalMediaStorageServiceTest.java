package com.familyhub.digital_family_hub.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class LocalMediaStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadStoresFileUnderLocalRootAndReturnsLocalUrl() {
        LocalMediaStorageService service = service();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "family photo.jpg",
            "image/jpeg",
            "image-bytes".getBytes()
        );

        MediaUploadDTO.Response response = service.upload(file);

        assertThat(response.url()).startsWith("http://localhost:8080/local-media/");
        assertThat(response.storagePublicId()).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve(response.storagePublicId()))).isTrue();
    }

    @Test
    void deleteOnlyRemovesFilesInsideLocalRoot() {
        LocalMediaStorageService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "image-bytes".getBytes());
        MediaUploadDTO.Response response = service.upload(file);

        service.delete(response.storagePublicId());
        service.delete("../outside.png");

        assertThat(Files.exists(tempDir.resolve(response.storagePublicId()))).isFalse();
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        LocalMediaStorageService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> service.upload(file))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400)
            );
    }

    private LocalMediaStorageService service() {
        return new LocalMediaStorageService(
            tempDir,
            "http://localhost:8080",
            "/local-media/",
            1024,
            2048
        );
    }
}
