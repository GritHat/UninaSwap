package com.uninaswap.server.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.AuthMessage;
import com.uninaswap.server.service.AuthService;
import com.uninaswap.server.service.SessionService;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.mapper.UserMapper;

import java.util.Optional;

@Component
public class AuthWebSocketHandler extends TextWebSocketHandler {
    
    private final AuthService authService;
    private final SessionService sessionService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    public AuthWebSocketHandler(AuthService authService, SessionService sessionService, UserMapper userMapper) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.userMapper = userMapper;
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            AuthMessage authMessage = objectMapper.readValue(message.getPayload(), AuthMessage.class);
            
            System.out.println("SERVER RECEIVED: " + message.getPayload());
            System.out.println("Message type: " + authMessage.getType() + ", Username: " + authMessage.getUsername());
            
            AuthMessage response = new AuthMessage();
            
            switch (authMessage.getType()) {
                case LOGIN_REQUEST: processUserAuthentication(authMessage, response, session); break;
                case REGISTER_REQUEST: processUserRegistration(authMessage, response); break;
                default:
                    response.setSuccess(false);
                    response.setMessage("Unknown message type: " + authMessage.getType());
                    System.out.println("Unknown message type: " + authMessage.getType());
            }
            
            String responseJson = objectMapper.writeValueAsString(response);
            System.out.println("SERVER SENDING: " + responseJson);
            session.sendMessage(new TextMessage(responseJson));
            
        } catch (Exception e) {
            System.err.println("Error processing auth message: " + e.getMessage());
            e.printStackTrace();
            
            AuthMessage errorResponse = new AuthMessage();
            errorResponse.setType(AuthMessage.Type.LOGIN_RESPONSE); // Use LOGIN_RESPONSE as fallback
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Server error: " + e.getMessage());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void processUserRegistration(AuthMessage authMessage, AuthMessage response) {
        UserDTO newUser = new UserDTO();
        newUser.setUsername(authMessage.getUsername());
        newUser.setEmail(authMessage.getEmail());
        newUser.setPassword(authMessage.getPassword());
        
        boolean registered = authService.register(newUser);
        
        response.setType(AuthMessage.Type.REGISTER_RESPONSE);
        response.setSuccess(registered);
        response.setMessage(registered ? "Registration successful" : "Username or email already exists");
    }

    private void processUserAuthentication(AuthMessage authMessage, AuthMessage response, WebSocketSession session) {
        Optional<UserEntity> userOpt = authService.authenticateAndGetUser(
            authMessage.getUsername(), 
            authMessage.getPassword()
        );
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            
            // Create authenticated session and get token
            String token = sessionService.createAuthenticatedSession(session, user);
            
            response.setType(AuthMessage.Type.LOGIN_RESPONSE);
            response.setSuccess(true);
            response.setMessage("Authentication successful");
            setUserDetails(response, user);
            response.setToken(token);
        } else {
            response.setType(AuthMessage.Type.LOGIN_RESPONSE);
            response.setSuccess(false);
            response.setMessage("Invalid username or password");
        }
    }

    private void setUserDetails(AuthMessage response, UserEntity user) {
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBio(user.getBio());
        response.setProfileImagePath(user.getProfileImagePath());
        response.setUser(userMapper.toDto(user));
        response.setMessage("Login successful");
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessionService.removeSession(session);
        System.out.println("WebSocket connection closed: " + session.getId() + ", status: " + status);
    }
}