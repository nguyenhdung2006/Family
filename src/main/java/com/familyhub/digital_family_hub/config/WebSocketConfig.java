package com.familyhub.digital_family_hub.config;

import com.familyhub.digital_family_hub.auth.JwtAuthenticationFilter;
import com.familyhub.digital_family_hub.auth.JwtService;
import jakarta.servlet.http.Cookie;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
        registry.addEndpoint("/ws")
            .setAllowedOrigins(allowedOrigins.split(","))
            .setHandshakeHandler(new JwtHandshakeHandler(jwtService))
            .addInterceptors(new HttpSessionHandshakeInterceptor())
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
            return message;
        }

        private void authenticateConnect(StompHeaderAccessor accessor) {
            if (accessor.getUser() instanceof StompPrincipal) {
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

        private void validateSubscription(SimpMessageHeaderAccessor accessor) {
            String destination = accessor.getDestination();
            Principal user = accessor.getUser();
            if (destination != null && destination.startsWith("/topic/rooms/")) {
                if (!(user instanceof StompPrincipal principal) || "VIEWER".equals(principal.role())) {
                    throw new IllegalArgumentException("Not allowed to subscribe to chat rooms");
                }
            }
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
            if (request instanceof ServletServerHttpRequest servletRequest) {
                Cookie[] cookies = servletRequest.getServletRequest().getCookies();
                if (cookies != null) {
                    return Arrays.stream(cookies)
                        .filter(cookie -> JwtAuthenticationFilter.TOKEN_COOKIE.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .map(jwtService::validate)
                        .flatMap(java.util.Optional::stream)
                        .findFirst()
                        .<Principal>map(principal -> new StompPrincipal(principal.email(), principal.role().name()))
                        .orElseGet(() -> super.determineUser(request, wsHandler, attributes));
                }
            }
            return super.determineUser(request, wsHandler, attributes);
        }
    }

    private record StompPrincipal(String name, String role) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
