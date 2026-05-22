package com.familyhub.digital_family_hub.system;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
            "status", "UP",
            "service", "hometree",
            "timestamp", Instant.now()
        ));
    }
}
