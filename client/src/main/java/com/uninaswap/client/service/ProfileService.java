package com.uninaswap.client.service;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.ProfileUpdateMessage;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.websocket.WebSocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ProfileService {
    
    private final WebSocketClient webSocketClient;
    
    public ProfileService() {
        this.webSocketClient = WebSocketManager.getClient();
    }
    
    /**
     * Set the handler for profile update responses and register it with WebSocketClient
     */
    public void setUpdateResponseHandler(Consumer<ProfileUpdateMessage> handler) {
        // Register the handler with the WebSocketClient
        webSocketClient.registerMessageHandler(ProfileUpdateMessage.class, handler);
    }
    
    /**
     * Send a profile update request to the server
     * @param user The user object containing updated profile information
     * @return CompletableFuture that completes when the update is sent
     */
    public CompletableFuture<Void> updateProfile(UserDTO user) {
        ProfileUpdateMessage message = new ProfileUpdateMessage();
        message.setType(ProfileUpdateMessage.Type.UPDATE_REQUEST);
        message.setUsername(user.getUsername());
        message.setFirstName(user.getFirstName());
        message.setLastName(user.getLastName());
        message.setBio(user.getBio());
        message.setProfileImagePath(user.getProfileImagePath());
        
        return webSocketClient.sendMessage(message);
    }
}