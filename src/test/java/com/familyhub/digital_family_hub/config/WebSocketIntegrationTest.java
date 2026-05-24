package com.familyhub.digital_family_hub.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.familyhub.digital_family_hub.albums.AlbumRepository;
import com.familyhub.digital_family_hub.auth.JwtAuthenticationFilter;
import com.familyhub.digital_family_hub.auth.JwtService;
import com.familyhub.digital_family_hub.chat.ChatMessageRepository;
import com.familyhub.digital_family_hub.chat.ChatRoomRepository;
import com.familyhub.digital_family_hub.chat.ChatService;
import com.familyhub.digital_family_hub.chat.MessageDTO;
import com.familyhub.digital_family_hub.chat.MessageType;
import com.familyhub.digital_family_hub.family.FamilyMemberRepository;
import com.familyhub.digital_family_hub.family.FamilyRelationshipRepository;
import com.familyhub.digital_family_hub.kitchen.RecipeRepository;
import com.familyhub.digital_family_hub.media.MediaAssetRepository;
import com.familyhub.digital_family_hub.memorials.MemorialTributeRepository;
import com.familyhub.digital_family_hub.notifications.InAppNotificationRepository;
import com.familyhub.digital_family_hub.posts.MemoryPostRepository;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
        "hometree.cors.allowed-origins=http://localhost:3000",
        "hometree.auth.jwt-secret=test-secret-that-is-long-enough",
        "hometree.auth.jwt-ttl-minutes=30"
    }
)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @MockitoBean
    private FamilyMemberRepository familyMemberRepository;

    @MockitoBean
    private FamilyRelationshipRepository familyRelationshipRepository;

    @MockitoBean
    private MemoryPostRepository memoryPostRepository;

    @MockitoBean
    private AlbumRepository albumRepository;

    @MockitoBean
    private MediaAssetRepository mediaAssetRepository;

    @MockitoBean
    private ChatRoomRepository chatRoomRepository;

    @MockitoBean
    private ChatMessageRepository chatMessageRepository;

    @MockitoBean
    private InAppNotificationRepository inAppNotificationRepository;

    @MockitoBean
    private RecipeRepository recipeRepository;

    @MockitoBean
    private MemorialTributeRepository memorialTributeRepository;

    private WebSocketStompClient stompClient;
    private ThreadPoolTaskScheduler taskScheduler;

    @BeforeEach
    void setUpClient() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.initialize();
        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
            new StringMessageConverter(),
            new ByteArrayMessageConverter(),
            new MappingJackson2MessageConverter()
        )));
        stompClient.setTaskScheduler(taskScheduler);
    }

    @AfterEach
    void stopClient() {
        if (stompClient != null) {
            stompClient.stop();
        }
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    @Test
    void authenticatedMemberConnectsSuccessfully() throws Exception {
        TestSessionHandler handler = new TestSessionHandler();
        StompSession session = connect(UserRole.MEMBER, handler);

        assertThat(session.isConnected()).isTrue();
        disconnect(session);
    }

    @Test
    void memberSubscribesToRoomTopic() throws Exception {
        TestSessionHandler handler = new TestSessionHandler();
        StompSession session = connect(UserRole.MEMBER, handler);
        String destination = "/topic/rooms/" + UUID.randomUUID();
        CompletableFuture<Object> message = new CompletableFuture<>();

        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        session.subscribe(headers, frameCompleter(message));

        for (int attempt = 0; attempt < 10 && !message.isDone(); attempt++) {
            messagingTemplate.convertAndSend(destination, "subscription-probe");
            Thread.sleep(100);
        }

        assertThat(message.get(3, TimeUnit.SECONDS)).isNotNull();
        assertThat(handler.error).isNotDone();
        disconnect(session);
    }

    @Test
    void memberSendsApplicationMessageSuccessfully() throws Exception {
        TestSessionHandler handler = new TestSessionHandler();
        StompSession session = connect(UserRole.MEMBER, handler);
        UUID roomId = UUID.randomUUID();

        StompHeaders headers = new StompHeaders();
        headers.setDestination("/app/rooms/" + roomId);
        session.send(headers, new MessageDTO.SendRequest(MessageType.TEXT, "hello from stomp", null));

        verify(chatService, timeout(3000)).sendMessage(
            eq(roomId),
            any(MessageDTO.SendRequest.class),
            any(Principal.class)
        );
        assertThat(handler.error).isNotDone();
        disconnect(session);
    }

    @Test
    void viewerIsRejectedFromSendingApplicationMessages() throws Exception {
        TestSessionHandler handler = new TestSessionHandler();
        StompSession session = connect(UserRole.VIEWER, handler);

        StompHeaders headers = new StompHeaders();
        headers.setDestination("/app/rooms/" + UUID.randomUUID());
        session.send(headers, new MessageDTO.SendRequest(MessageType.TEXT, "viewer should be rejected", null));

        assertThat(handler.error.get(3, TimeUnit.SECONDS)).isNotNull();
        disconnect(session);
    }

    @Test
    void viewerIsRejectedFromRestrictedRoomSubscriptions() throws Exception {
        TestSessionHandler handler = new TestSessionHandler();
        StompSession session = connect(UserRole.VIEWER, handler);

        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/rooms/" + UUID.randomUUID());
        session.subscribe(headers, emptyFrameHandler());

        assertThat(handler.error.get(3, TimeUnit.SECONDS)).isNotNull();
        disconnect(session);
    }

    private StompSession connect(UserRole role, TestSessionHandler handler) throws Exception {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add(
            HttpHeaders.COOKIE,
            JwtAuthenticationFilter.TOKEN_COOKIE + "=" + jwtService.issueToken(user(role))
        );
        return stompClient.connectAsync(wsUrl(), handshakeHeaders, new StompHeaders(), handler)
            .get(3, TimeUnit.SECONDS);
    }

    private String wsUrl() {
        return "http://localhost:" + port + "/ws";
    }

    private void disconnect(StompSession session) {
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (RuntimeException exception) {
            // Rejected STOMP frames can close the SockJS session before the test asks to disconnect.
        }
    }

    private AppUser user(UserRole role) {
        AppUser user = new AppUser();
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setName(role.name() + " User");
        user.setRole(role);
        return user;
    }

    private StompFrameHandler emptyFrameHandler() {
        return frameCompleter(new CompletableFuture<>());
    }

    private StompFrameHandler frameCompleter(CompletableFuture<Object> message) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                message.complete(payload);
            }
        };
    }

    private static final class TestSessionHandler extends StompSessionHandlerAdapter {

        private final CompletableFuture<Throwable> error = new CompletableFuture<>();

        @Override
        public void handleException(
            StompSession session,
            org.springframework.messaging.simp.stomp.StompCommand command,
            StompHeaders headers,
            byte[] payload,
            Throwable exception
        ) {
            error.complete(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            error.complete(exception);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            error.complete(new IllegalStateException(headers.getFirst("message")));
        }
    }
}
