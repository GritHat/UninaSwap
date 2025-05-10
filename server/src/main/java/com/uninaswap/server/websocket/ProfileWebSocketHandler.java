package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.uninaswap.common.message.ProfileUpdateMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.UserRepository;

@Component
public class ProfileWebSocketHandler extends TextWebSocketHandler {
    
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ProfileWebSocketHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        System.out.println("SERVER RECEIVED PROFILE REQUEST: " + message.getPayload());
        
        try {
            ProfileUpdateMessage updateMessage = objectMapper.readValue(message.getPayload(), ProfileUpdateMessage.class);
            
            if (updateMessage.getType() == ProfileUpdateMessage.Type.UPDATE_REQUEST) {
                ProfileUpdateMessage response = new ProfileUpdateMessage();
                response.setType(ProfileUpdateMessage.Type.UPDATE_RESPONSE);
                
                // Find user by username
                UserEntity user = userRepository.findByUsername(updateMessage.getUsername()).orElse(null);
                
                if (user != null) {
                    // Update user profile
                    user.setFirstName(updateMessage.getFirstName());
                    user.setLastName(updateMessage.getLastName());
                    user.setBio(updateMessage.getBio());
                    user.setProfileImagePath(updateMessage.getProfileImagePath());
                    
                    // Save updated user
                    userRepository.save(user);
                    
                    response.setSuccess(true);
                    response.setMessage("Profile updated successfully");
                } else {
                    response.setSuccess(false);
                    response.setMessage("User not found");
                }
                
                // Send response
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            
        } catch (Exception e) {
            System.err.println("Error handling profile update: " + e.getMessage());
            
            // Send error response
            ProfileUpdateMessage errorResponse = new ProfileUpdateMessage();
            errorResponse.setType(ProfileUpdateMessage.Type.UPDATE_RESPONSE);
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error processing request: " + e.getMessage());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
}