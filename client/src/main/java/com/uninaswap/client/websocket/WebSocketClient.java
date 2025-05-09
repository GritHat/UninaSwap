package com.uninaswap.client.websocket;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import jakarta.websocket.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.message.AuthMessage;

@ClientEndpoint
public class WebSocketClient {
    
    private Session session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<AuthMessage> messageHandler;
    
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
            System.out.println("CLIENT RECEIVED: " + message); // Add debug logging
            AuthMessage authMessage = objectMapper.readValue(message, AuthMessage.class);
            System.out.println("PARSED MESSAGE TYPE: " + authMessage.getType()); 
            if (messageHandler != null) {
                messageHandler.accept(authMessage);
            } else {
                System.out.println("WARNING: No message handler registered");
            }
        } catch (Exception e) {
            System.out.println("ERROR parsing message: " + message);
            e.printStackTrace();
        }
    }
    
    public CompletableFuture<Void> sendMessage(AuthMessage message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            if (session != null && session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                System.out.println("SENDING: " + jsonMessage); // Add debug logging
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
                System.out.println("WebSocket session is not open");
                future.completeExceptionally(new IllegalStateException("WebSocket session is not open"));
            }
        } catch (Exception e) {
            System.out.println("Exception sending message: " + e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    public void setMessageHandler(Consumer<AuthMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }
}