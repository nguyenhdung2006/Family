package com.familyhub.digital_family_hub.config;

import com.familyhub.digital_family_hub.auth.JwtAuthenticationFilter;
import com.familyhub.digital_family_hub.auth.OAuth2LoginSuccessHandler;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        ObjectProvider<ClientRegistrationRepository> clientRegistrations,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler
    ) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/auth/logout", "/ws", "/ws/**"))
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                // Public probes and login metadata must stay reachable before HOMETREE_TOKEN exists.
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/login-options").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Family records are readable by VIEWER, but mutations are reserved for ADMIN.
                .requestMatchers(HttpMethod.GET, "/api/family/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/family/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/family/**").hasRole("ADMIN")
                // Content areas allow VIEWER reads while preserving write access for ADMIN and MEMBER.
                .requestMatchers(HttpMethod.GET, "/api/timeline/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/timeline/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/timeline/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/timeline/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/albums/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/albums/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/albums/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/albums/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers("/api/media/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers("/api/messages/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/notifications/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/notifications/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/notifications/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PATCH, "/api/notifications/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/notifications/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/memorials/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/memorials/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/memorials/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/memorials/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/kitchen/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/kitchen/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/kitchen/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/kitchen/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers("/api/**").authenticated()
                // WebSocket role matrix: ADMIN/MEMBER/VIEWER may handshake; STOMP rules below block VIEWER from
                // message-producing /app/** sends and restricted /topic/rooms/** subscriptions.
                .requestMatchers("/ws", "/ws/**").hasAnyRole("ADMIN", "MEMBER", "VIEWER")
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.successHandler(oAuth2LoginSuccessHandler));
        } else {
            http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
        @Value("${hometree.cors.allowed-origins}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
