package com.uninaswap.client.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.common.message.AuthMessage;

/**
 * Service for handling authentication-related operations.
 * This separates authentication logic from controllers.
 */
public class AuthenticationService {
    private static AuthenticationService instance;
    
    // Singleton pattern
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    private final WebSocketClient webSocketClient;
    
    private AuthenticationService() {
        this.webSocketClient = WebSocketManager.getClient();
        try {
            if (!webSocketClient.isConnected()) {
                webSocketClient.connect("ws://localhost:8080/auth");
            }
        } catch (Exception e) {
            System.err.println("Error connecting to authentication server: " + e.getMessage());
        }
    }
    
    /**
     * Send login request to server
     */
    public CompletableFuture<Void> login(String username, String password) {
        AuthMessage loginRequest = new AuthMessage();
        loginRequest.setType(AuthMessage.Type.LOGIN_REQUEST);
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        return webSocketClient.sendMessage(loginRequest);
    }
    
    /**
     * Send registration request to server
     */
    public CompletableFuture<Void> register(String username, String email, String password) {
        AuthMessage registerRequest = new AuthMessage();
        registerRequest.setType(AuthMessage.Type.REGISTER_REQUEST);
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        return webSocketClient.sendMessage(registerRequest);
    }
    
    /**
     * Set message handler for auth responses
     */
    public void setAuthResponseHandler(Consumer<AuthMessage> handler) {
        webSocketClient.setMessageHandler(handler);
    }
    
    /**
     * Get the WebSocket client instance
     */
    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
    
    /**
     * Check if WebSocket is connected
     */
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isConnected();
    }
}