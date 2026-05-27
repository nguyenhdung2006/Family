package com.familyhub.digital_family_hub.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MediaStorageConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaStorageConfig.class);

    @Bean
    MediaStorageService mediaStorageService(
        ObjectMapper objectMapper,
        Environment environment,
        @Value("${hometree.storage.cloudinary-cloud-name}") String cloudName,
        @Value("${hometree.storage.cloudinary-upload-preset}") String uploadPreset,
        @Value("${hometree.storage.max-image-bytes}") long maxImageBytes,
        @Value("${hometree.storage.max-video-bytes}") long maxVideoBytes,
        @Value("${hometree.storage.local-fallback-enabled}") boolean localFallbackEnabled,
        @Value("${hometree.storage.local-directory}") String localDirectory,
        @Value("${hometree.storage.local-base-url}") String localBaseUrl,
        @Value("${hometree.storage.local-public-path}") String localPublicPath
    ) {
        if (!cloudName.isBlank() && !uploadPreset.isBlank()) {
            LOGGER.info("Media storage mode: Cloudinary");
            return new CloudinaryMediaStorageService(objectMapper, cloudName, uploadPreset, maxImageBytes, maxVideoBytes);
        }

        if (localFallbackEnabled && !isProdProfile(environment)) {
            LOGGER.warn("Media storage mode: local DEV fallback at {}", Path.of(localDirectory).toAbsolutePath().normalize());
            return new LocalMediaStorageService(
                Path.of(localDirectory),
                localBaseUrl,
                localPublicPath,
                maxImageBytes,
                maxVideoBytes
            );
        }

        LOGGER.warn("Media storage mode: Cloudinary unavailable and local fallback disabled");
        return new CloudinaryMediaStorageService(objectMapper, cloudName, uploadPreset, maxImageBytes, maxVideoBytes);
    }

    private boolean isProdProfile(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
