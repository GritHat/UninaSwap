package com.uninaswap.client.websocket;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import jakarta.websocket.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uninaswap.common.message.Message;
import com.uninaswap.client.service.UserSessionService;

@ClientEndpoint
public class WebSocketClient {
    private static WebSocketClient instance;
    public static WebSocketClient getInstance() {
        if (instance == null) {
            instance = new WebSocketClient();
        }
        return instance;
    }
    private WebSocketClient() {}

    private Session session;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Map<Class<? extends Message>, Consumer<Message>> messageHandlers = new HashMap<>();

    public void connect(String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
    }

    public void disconnect() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            Message baseMessage = objectMapper.readValue(message, Message.class);
            if (!baseMessage.getMessageType().equals("image"))
                System.out.println("CLIENT RECEIVED: " + message);
            System.out.println(baseMessage.getMessageType());
            Consumer<Message> handler = messageHandlers.get(baseMessage.getClass());

            if (handler != null) {
                handler.accept(baseMessage);
            } else {
                System.out.println(
                        "WARNING: No handler registered for message type: " + baseMessage.getClass().getName());
            }
        } catch (Exception e) {
            System.out.println("ERROR parsing message: " + message);
            e.printStackTrace();
        }
    }

    public <T extends Message> CompletableFuture<Void> sendMessage(T message) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            UserSessionService sessionService = UserSessionService.getInstance();
            if (sessionService.isLoggedIn() && sessionService.getToken() != null) {
                message.setToken(sessionService.getToken());
            }

            if (session != null && session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                System.out.println("SENDING: " + jsonMessage);

                session.getAsyncRemote().sendText(jsonMessage, result -> {
                    if (result.isOK()) {
                        System.out.println("Message sent successfully");
                        future.complete(null);
                    } else {
                        System.out.println("Failed to send message: " + result.getException().getMessage());
                        future.completeExceptionally(result.getException());
                    }
                });
            } else {
                System.out.println("WebSocket session is not open " + (session == null ? "null" : "closed"));
                future.completeExceptionally(new IllegalStateException("WebSocket session is not open" + (session == null ? "null" : "closed")));
            }
        } catch (Exception e) {
            System.out.println("Exception sending message: " + e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Register a handler for a specific message type
     * 
     * @param messageType The class of the message type to handle
     * @param handler     The handler function for messages of this type
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> void registerMessageHandler(Class<T> messageType, Consumer<T> handler) {
        messageHandlers.put(messageType, message -> handler.accept((T) message));
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}