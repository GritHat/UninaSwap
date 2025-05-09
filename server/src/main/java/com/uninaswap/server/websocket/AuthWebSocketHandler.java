package com.uninaswap.server.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.message.AuthMessage;
import com.uninaswap.common.model.User;
import com.uninaswap.server.service.AuthService;

@Component
public class AuthWebSocketHandler extends TextWebSocketHandler {
    
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AuthWebSocketHandler(AuthService authService) {
        this.authService = authService;
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("SERVER RECEIVED: " + message.getPayload());
        
        try {
            AuthMessage authMessage = objectMapper.readValue(message.getPayload(), AuthMessage.class);
            System.out.println("Message type: " + authMessage.getType() + ", Username: " + authMessage.getUsername());
            
            AuthMessage response = new AuthMessage();
            
            switch (authMessage.getType()) {
                case LOGIN_REQUEST:
                    boolean authenticated = authService.authenticate(
                        authMessage.getUsername(), 
                        authMessage.getPassword()
                    );
                    
                    System.out.println("Authentication result for " + authMessage.getUsername() + ": " + authenticated);
                    
                    response.setType(AuthMessage.Type.LOGIN_RESPONSE);
                    response.setSuccess(authenticated);
                    response.setMessage(authenticated ? "Login successful" : "Invalid credentials");
                    break;
                    
                case REGISTER_REQUEST:
                    User newUser = new User();
                    newUser.setUsername(authMessage.getUsername());
                    newUser.setEmail(authMessage.getEmail());
                    newUser.setPassword(authMessage.getPassword());
                    
                    boolean registered = authService.register(newUser);
                    
                    response.setType(AuthMessage.Type.REGISTER_RESPONSE);
                    response.setSuccess(registered);
                    response.setMessage(registered ? "Registration successful" : "Username or email already exists");
                    break;
                    
                default:
                    response.setSuccess(false);
                    response.setMessage("Unknown message type: " + authMessage.getType());
                    System.out.println("Unknown message type: " + authMessage.getType());
            }
            
            String responseJson = objectMapper.writeValueAsString(response);
            System.out.println("SERVER SENDING: " + responseJson);
            session.sendMessage(new TextMessage(responseJson));
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            
            // Send error response
            AuthMessage errorResponse = new AuthMessage();
            errorResponse.setType(AuthMessage.Type.LOGIN_RESPONSE); // Use LOGIN_RESPONSE as fallback
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Server error: " + e.getMessage());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("WebSocket connection closed: " + session.getId() + ", status: " + status);
    }
}