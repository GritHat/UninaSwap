package com.uninaswap.client.service;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.ProfileUpdateMessage;
import com.uninaswap.client.websocket.WebSocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ProfileService {
    
    private final WebSocketClient webSocketClient;
    
    public ProfileService() {
        this.webSocketClient = WebSocketClient.getInstance();
    }
    
    /**
     * Set the handler for profile update responses and register it with WebSocketClient
     * 
     * @param handler The handler to process profile update messages
     */
    public void setUpdateResponseHandler(Consumer<ProfileUpdateMessage> handler) {
        // Register the handler with the WebSocketClient
        webSocketClient.registerMessageHandler(ProfileUpdateMessage.class, handler);
    }
    
    /**
     * Send a profile update request to the server
     * 
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
        message.setZipPostalCode(user.getZipPostalCode());
        message.setStateProvince(user.getStateProvince());
        message.setAddress(user.getAddress());
        message.setCountry(user.getCountry());
        message.setCity(user.getCity());
        
        return webSocketClient.sendMessage(message);
    }
}