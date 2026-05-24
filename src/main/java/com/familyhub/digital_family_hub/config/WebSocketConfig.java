package com.familyhub.digital_family_hub.config;

import com.familyhub.digital_family_hub.auth.JwtAuthenticationFilter;
import com.familyhub.digital_family_hub.auth.JwtService;
import jakarta.servlet.http.Cookie;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String PRINCIPAL_ATTRIBUTE = WebSocketConfig.class.getName() + ".principal";

    private final String allowedOrigins;
    private final JwtService jwtService;

    public WebSocketConfig(
        @Value("${hometree.cors.allowed-origins}") String allowedOrigins,
        JwtService jwtService
    ) {
        this.allowedOrigins = allowedOrigins;
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Keep /ws aligned with the OAuth session model so browser clients can reuse HOMETREE_TOKEN.
        registry.addEndpoint("/ws")
            .setAllowedOrigins(allowedOrigins.split(","))
            .setHandshakeHandler(new JwtHandshakeHandler(jwtService))
            .addInterceptors(new JwtCookieHandshakeInterceptor(jwtService), new HttpSessionHandshakeInterceptor())
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new JwtStompChannelInterceptor(jwtService));
    }

    private static final class JwtStompChannelInterceptor implements ChannelInterceptor {

        private final JwtService jwtService;

        private JwtStompChannelInterceptor(JwtService jwtService) {
            this.jwtService = jwtService;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                authenticateConnect(accessor);
            }
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                validateSubscription(accessor);
            }
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                validateApplicationMessage(accessor);
            }
            return message;
        }

        private void authenticateConnect(StompHeaderAccessor accessor) {
            if (accessor.getUser() instanceof StompPrincipal) {
                return;
            }
            // Prefer principals established before CONNECT because SockJS transports do not behave like plain HTTP headers.
            Optional<StompPrincipal> securityPrincipal = fromAuthentication(accessor.getUser());
            if (securityPrincipal.isPresent()) {
                accessor.setUser(securityPrincipal.get());
                return;
            }
            Optional<StompPrincipal> sessionPrincipal = fromSessionAttributes(accessor.getSessionAttributes());
            if (sessionPrincipal.isPresent()) {
                accessor.setUser(sessionPrincipal.get());
                return;
            }
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization == null || authorization.isEmpty() || !authorization.get(0).startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing WebSocket bearer token");
            }
            String token = authorization.get(0).substring(7);
            JwtService.JwtPrincipal principal = jwtService.validate(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid WebSocket bearer token"));
            accessor.setUser(new StompPrincipal(principal.email(), principal.role().name()));
        }

        private Optional<StompPrincipal> fromAuthentication(Principal principal) {
            if (!(principal instanceof Authentication authentication)) {
                return Optional.empty();
            }
            return roleOf(authentication).map(role -> new StompPrincipal(authentication.getName(), role));
        }

        private Optional<StompPrincipal> fromSessionAttributes(Map<String, Object> sessionAttributes) {
            if (sessionAttributes == null) {
                return Optional.empty();
            }
            Object principal = sessionAttributes.get(PRINCIPAL_ATTRIBUTE);
            if (principal instanceof StompPrincipal stompPrincipal) {
                return Optional.of(stompPrincipal);
            }
            return Optional.empty();
        }

        private void validateSubscription(SimpMessageHeaderAccessor accessor) {
            String destination = accessor.getDestination();
            Principal user = accessor.getUser();
            if (destination != null && destination.startsWith("/topic/rooms/")) {
                // Realtime role matrix: ADMIN/MEMBER can subscribe to room topics; VIEWER cannot read restricted rooms.
                if (!hasMessagingAccess(user)) {
                    throw new IllegalArgumentException("Not allowed to subscribe to chat rooms");
                }
            }
        }

        private void validateApplicationMessage(SimpMessageHeaderAccessor accessor) {
            String destination = accessor.getDestination();
            // Realtime role matrix: ADMIN/MEMBER can SEND to /app/**; VIEWER is passive and cannot produce messages.
            if (destination != null && destination.startsWith("/app/") && !hasMessagingAccess(accessor.getUser())) {
                throw new IllegalArgumentException("Not allowed to send WebSocket messages");
            }
        }

        private boolean hasMessagingAccess(Principal principal) {
            return roleOf(principal)
                .filter(role -> !"VIEWER".equals(role))
                .isPresent();
        }

        private Optional<String> roleOf(Principal principal) {
            if (principal instanceof StompPrincipal stompPrincipal) {
                return Optional.of(stompPrincipal.role());
            }
            if (principal instanceof Authentication authentication) {
                return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .findFirst()
                    .map(role -> role.substring("ROLE_".length()));
            }
            return Optional.empty();
        }
    }

    private static final class JwtCookieHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtService jwtService;

        private JwtCookieHandshakeInterceptor(JwtService jwtService) {
            this.jwtService = jwtService;
        }

        @Override
        public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
        ) {
            // Store the cookie-derived principal for CONNECT so reconnects keep the same session identity.
            principalFromCookie(request, jwtService).ifPresent(principal -> attributes.put(PRINCIPAL_ATTRIBUTE, principal));
            return true;
        }

        @Override
        public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
        ) {
        }
    }

    private static final class JwtHandshakeHandler extends DefaultHandshakeHandler {

        private final JwtService jwtService;

        private JwtHandshakeHandler(JwtService jwtService) {
            this.jwtService = jwtService;
        }

        @Override
        protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            java.util.Map<String, Object> attributes
        ) {
            // Spring's WebSocket user is the anchor for later STOMP authorization checks.
            Object principal = attributes.get(PRINCIPAL_ATTRIBUTE);
            if (principal instanceof StompPrincipal stompPrincipal) {
                return stompPrincipal;
            }
            return principalFromCookie(request, jwtService)
                .<Principal>map(stompPrincipal -> stompPrincipal)
                .orElseGet(() -> super.determineUser(request, wsHandler, attributes));
        }
    }

    private static Optional<StompPrincipal> principalFromCookie(ServerHttpRequest request, JwtService jwtService) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return Optional.empty();
        }
        Cookie[] cookies = servletRequest.getServletRequest().getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> JwtAuthenticationFilter.TOKEN_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .map(jwtService::validate)
            .flatMap(Optional::stream)
            .findFirst()
            .map(principal -> new StompPrincipal(principal.email(), principal.role().name()));
    }

    private record StompPrincipal(String name, String role) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
