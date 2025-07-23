package com.uninaswap.client.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.AuthMessage;

/**
 * Service for handling authentication-related operations.
 * This separates authentication logic from controllers.
 */
/**
 * 
 */
public class AuthenticationService {
    /**
     * 
     */
    private static AuthenticationService instance;

    /**
     * @return
     */
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * 
     */
    private final WebSocketClient webSocketClient;

    /**
     * 
     */
    private AuthenticationService() {
        this.webSocketClient = WebSocketClient.getInstance();
    }

    /**
     * @return
     */
    private boolean connectToAuthEndpoint() {
        try {
            if (!webSocketClient.isConnected()) {
                webSocketClient.connect("ws://localhost:8080/ws");
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send login request to server
     * 
     * @param usernameOrEmail The username or email of the user
     * @param password        The password of the user
     * @return A CompletableFuture that completes when the login request is sent
     *         successfully, or fails with an exception if the connection fails.
     *         If the input contains '@', it is treated as an email; otherwise, it
     */
    /**
     * @param usernameOrEmail
     * @param password
     * @return
     */
    public CompletableFuture<Void> login(String usernameOrEmail, String password) {
        if (!connectToAuthEndpoint())
            return CompletableFuture.failedFuture(new Exception("Failed to connect to authentication endpoint"));

        boolean isEmail = usernameOrEmail.contains("@");
        AuthMessage loginRequest = new AuthMessage();
        loginRequest.setType(AuthMessage.Type.LOGIN_REQUEST);

        if (isEmail) {
            loginRequest.setEmail(usernameOrEmail);
            loginRequest.setPassword(password);
        } else {
            loginRequest.setUsername(usernameOrEmail);
            loginRequest.setPassword(password);
        }

        return webSocketClient.sendMessage(loginRequest);
    }

    /**
     * Send registration request to server
     * 
     * @param firstName The first name of the user
     * @param lastName  The last name of the user
     * @param username  The username of the user
     * @param email     The email of the user
     * @param password  The password of the user
     * @return A CompletableFuture that completes when the registration request is
     *         sent successfully, or fails with an exception if the connection fails.
     *         This method does not check if the username or email is already taken;
     */
    /**
     * @param firstName
     * @param lastName
     * @param username
     * @param email
     * @param password
     * @return
     */
    public CompletableFuture<Void> register(String firstName, String lastName ,String username, String email, String password) {
        if (!connectToAuthEndpoint())
            return CompletableFuture.failedFuture(new Exception("Failed to connect to authentication endpoint"));
        AuthMessage registerRequest = new AuthMessage();
        registerRequest.setType(AuthMessage.Type.REGISTER_REQUEST);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        return webSocketClient.sendMessage(registerRequest);
    }

    /**
     * Set message handler for auth responses
     * 
     * @param handler The handler to process AuthMessage responses
     */
    /**
     * @param handler
     */
    public void setAuthResponseHandler(Consumer<AuthMessage> handler) {
        webSocketClient.registerMessageHandler(AuthMessage.class, handler);
    }

    /**
     * Get the WebSocket client instance
     */
    /**
     * @return
     */
    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}