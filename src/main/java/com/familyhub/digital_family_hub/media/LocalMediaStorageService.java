package com.familyhub.digital_family_hub.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public class LocalMediaStorageService implements MediaStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMediaStorageService.class);

    private final Path storageRoot;
    private final String publicBaseUrl;
    private final String publicPath;
    private final long maxImageBytes;
    private final long maxVideoBytes;

    public LocalMediaStorageService(
        Path storageRoot,
        String publicBaseUrl,
        String publicPath,
        long maxImageBytes,
        long maxVideoBytes
    ) {
        this.storageRoot = storageRoot.toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.publicPath = normalizePublicPath(publicPath);
        this.maxImageBytes = maxImageBytes;
        this.maxVideoBytes = maxVideoBytes;
    }

    @Override
    public MediaUploadDTO.Response upload(MultipartFile file) {
        MediaType mediaType = resolveMediaType(file.getContentType());
        validateSize(file, mediaType);

        try {
            Files.createDirectories(storageRoot);
            String filename = UUID.randomUUID() + extensionFor(file, mediaType);
            Path target = storageRoot.resolve(filename).normalize();
            if (!target.startsWith(storageRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload path");
            }
            file.transferTo(target);
            return new MediaUploadDTO.Response(
                publicBaseUrl + publicPath + filename,
                filename,
                mediaType,
                file.getContentType(),
                file.getSize()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store local media");
        }
    }

    @Override
    public void delete(String storagePublicId) {
        if (storagePublicId == null || storagePublicId.isBlank()) {
            return;
        }
        Path filename = Path.of(storagePublicId).getFileName();
        if (filename == null || !filename.toString().equals(storagePublicId)) {
            LOGGER.warn("Skipped unsafe local media delete request for storage id {}", storagePublicId);
            return;
        }

        try {
            Path target = storageRoot.resolve(filename).normalize();
            if (!target.startsWith(storageRoot)) {
                LOGGER.warn("Skipped local media delete outside storage root for storage id {}", storagePublicId);
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            LOGGER.warn("Failed to delete local media file {}", storagePublicId, exception);
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image and video uploads are allowed");
    }

    private void validateSize(MultipartFile file, MediaType mediaType) {
        long maxBytes = mediaType == MediaType.IMAGE ? maxImageBytes : maxVideoBytes;
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is too large");
        }
    }

    private String extensionFor(MultipartFile file, MediaType mediaType) {
        String original = file.getOriginalFilename();
        if (original != null) {
            String filename = Path.of(original).getFileName().toString();
            int dot = filename.lastIndexOf('.');
            if (dot >= 0 && dot < filename.length() - 1) {
                String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (extension.matches("[a-z0-9]{1,10}")) {
                    return "." + extension;
                }
            }
        }
        if ("image/png".equals(file.getContentType())) {
            return ".png";
        }
        if ("image/jpeg".equals(file.getContentType())) {
            return ".jpg";
        }
        if ("video/mp4".equals(file.getContentType())) {
            return ".mp4";
        }
        return mediaType == MediaType.IMAGE ? ".img" : ".video";
    }

    private String normalizePublicPath(String value) {
        String path = value == null || value.isBlank() ? "/local-media/" : value.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    private String trimTrailingSlash(String value) {
        String baseUrl = value == null || value.isBlank() ? "http://localhost:8080" : value.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
