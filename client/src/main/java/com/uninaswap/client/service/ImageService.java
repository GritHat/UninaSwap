package com.uninaswap.client.service;

import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.ImageMessage;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ImageService {
    
    private static ImageService instance;
    private final WebSocketClient webSocketClient;
    private final Map<String, Image> imageCache = new HashMap<>();
    
    // Singleton pattern
    public static ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }
    
    private ImageService() {
        this.webSocketClient = WebSocketManager.getClient();
        this.webSocketClient.registerMessageHandler(ImageMessage.class, this::handleImageResponse);
    }
    
    public CompletableFuture<String> uploadImage(String username, File imageFile) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            // Read file bytes
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            
            // Get file extension
            String fileName = imageFile.getName();
            String format = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            // Convert to Base64
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            
            // Create and send message
            ImageMessage uploadMessage = new ImageMessage();
            uploadMessage.setType(ImageMessage.Type.UPLOAD_REQUEST);
            uploadMessage.setUsername(username);
            uploadMessage.setImageData(base64Image);
            uploadMessage.setFormat(format);
            
            // Set up completion handler
            setImageUploadHandler(response -> {
                if (response.isSuccess() && response.getType() == ImageMessage.Type.UPLOAD_RESPONSE) {
                    // Return the image ID
                    future.complete(response.getImageId() + "." + format);
                } else {
                    future.completeExceptionally(new IOException(response.getMessage()));
                }
            });
            
            // Send the message
            webSocketClient.sendMessage(uploadMessage)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
            
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    public CompletableFuture<Image> fetchImage(String imageId) {
        CompletableFuture<Image> future = new CompletableFuture<>();
        
        // Check cache first
        if (imageCache.containsKey(imageId)) {
            future.complete(imageCache.get(imageId));
            return future;
        }
        
        // If not in cache, request from server
        try {
            ImageMessage fetchMessage = new ImageMessage();
            fetchMessage.setType(ImageMessage.Type.FETCH_REQUEST);
            fetchMessage.setImageId(imageId);
            
            // Set up completion handler
            setImageFetchHandler(response -> {
                if (response.isSuccess() && response.getType() == ImageMessage.Type.FETCH_RESPONSE) {
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(response.getImageData());
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        
                        // Cache the image
                        imageCache.put(imageId, image);
                        
                        future.complete(image);
                    } catch (Exception e) {
                        future.completeExceptionally(
                                new IOException("Error decoding image data: " + e.getMessage()));
                    }
                } else {
                    future.completeExceptionally(new IOException(response.getMessage()));
                }
            });
            
            // Send the message
            webSocketClient.sendMessage(fetchMessage)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
                
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private Consumer<ImageMessage> uploadHandler;
    private Consumer<ImageMessage> fetchHandler;
    
    private void setImageUploadHandler(Consumer<ImageMessage> handler) {
        this.uploadHandler = handler;
    }
    
    private void setImageFetchHandler(Consumer<ImageMessage> handler) {
        this.fetchHandler = handler;
    }
    
    private void handleImageResponse(ImageMessage message) {
        switch (message.getType()) {
            case UPLOAD_RESPONSE:
                if (uploadHandler != null) {
                    uploadHandler.accept(message);
                }
                break;
                
            case FETCH_RESPONSE:
                if (fetchHandler != null) {
                    fetchHandler.accept(message);
                }
                break;
                
            default:
                System.out.println("Received unknown image message type: " + message.getType());
        }
    }
    
    // Clear the cache when needed
    public void clearCache() {
        imageCache.clear();
    }
}