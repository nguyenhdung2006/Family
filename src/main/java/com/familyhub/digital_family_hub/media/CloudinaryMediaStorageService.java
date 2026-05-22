package com.familyhub.digital_family_hub.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudinaryMediaStorageService implements MediaStorageService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String cloudName;
    private final String uploadPreset;
    private final long maxImageBytes;
    private final long maxVideoBytes;

    public CloudinaryMediaStorageService(
        ObjectMapper objectMapper,
        @Value("${hometree.storage.cloudinary-cloud-name}") String cloudName,
        @Value("${hometree.storage.cloudinary-upload-preset}") String uploadPreset,
        @Value("${hometree.storage.max-image-bytes}") long maxImageBytes,
        @Value("${hometree.storage.max-video-bytes}") long maxVideoBytes
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.cloudName = cloudName;
        this.uploadPreset = uploadPreset;
        this.maxImageBytes = maxImageBytes;
        this.maxVideoBytes = maxVideoBytes;
    }

    @Override
    public MediaUploadDTO.Response upload(MultipartFile file) {
        validateConfigured();
        MediaType mediaType = resolveMediaType(file.getContentType());
        validateSize(file, mediaType);

        try {
            String boundary = "----hometree-" + UUID.randomUUID();
            byte[] body = multipartBody(boundary, file);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload failed");
            }
            JsonNode json = objectMapper.readTree(response.body());
            return new MediaUploadDTO.Response(
                json.path("secure_url").asText(),
                json.path("public_id").asText(),
                mediaType,
                file.getContentType(),
                file.getSize()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload media");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Media upload interrupted");
        }
    }

    private void validateConfigured() {
        if (cloudName.isBlank() || uploadPreset.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary storage is not configured");
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

    private byte[] multipartBody(String boundary, MultipartFile file) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeFormField(output, boundary, "upload_preset", uploadPreset);
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + safeFilename(file) + "\"\r\n")
            .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(file.getBytes());
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeFormField(ByteArrayOutputStream output, String boundary, String name, String value)
        throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String safeFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            return "upload";
        }
        return original.replace("\"", "");
    }
}
