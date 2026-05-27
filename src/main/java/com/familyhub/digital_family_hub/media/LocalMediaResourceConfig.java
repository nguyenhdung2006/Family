package com.familyhub.digital_family_hub.media;

import java.nio.file.Path;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LocalMediaResourceConfig implements WebMvcConfigurer {

    private final Environment environment;
    private final String cloudName;
    private final String uploadPreset;
    private final boolean localFallbackEnabled;
    private final String localDirectory;
    private final String localPublicPath;

    public LocalMediaResourceConfig(
        Environment environment,
        @Value("${hometree.storage.cloudinary-cloud-name}") String cloudName,
        @Value("${hometree.storage.cloudinary-upload-preset}") String uploadPreset,
        @Value("${hometree.storage.local-fallback-enabled}") boolean localFallbackEnabled,
        @Value("${hometree.storage.local-directory}") String localDirectory,
        @Value("${hometree.storage.local-public-path}") String localPublicPath
    ) {
        this.environment = environment;
        this.cloudName = cloudName;
        this.uploadPreset = uploadPreset;
        this.localFallbackEnabled = localFallbackEnabled;
        this.localDirectory = localDirectory;
        this.localPublicPath = localPublicPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!isLocalFallbackActive()) {
            return;
        }
        String pattern = normalizePublicPath(localPublicPath) + "**";
        String location = Path.of(localDirectory).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }

    private boolean isLocalFallbackActive() {
        return cloudName.isBlank()
            && uploadPreset.isBlank()
            && localFallbackEnabled
            && !Arrays.asList(environment.getActiveProfiles()).contains("prod");
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
}
