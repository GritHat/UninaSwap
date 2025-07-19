package com.uninaswap.client.service;

import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.ImageMessage;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ImageService {
    
    private static ImageService instance;
    private final WebSocketClient webSocketClient;
    private final Map<String, Image> imageCache = new HashMap<>();
    
    // Replace single handler with a map of pending requests
    private final Map<String, CompletableFuture<Image>> pendingRequests = new ConcurrentHashMap<>();
    
    // Singleton pattern
    public static ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }
    
    private ImageService() {
        this.webSocketClient = WebSocketClient.getInstance();
        this.webSocketClient.registerMessageHandler(ImageMessage.class, this::handleImageResponse);
    }
    
    /**
     * Upload an image via HTTP for profile pictures
     * @param imageFile The image file to upload
     * @return A CompletableFuture with the image path on success
     */
    public CompletableFuture<String> uploadImageViaHttp(File imageFile) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // Create a thread for the upload to avoid blocking the UI
        new Thread(() -> {
            try {
                // Prepare the HTTP client
                HttpClient client = HttpClient.newHttpClient();
                
                // Create a simplified multipart form implementation
                MultipartFormData formData = new MultipartFormData();
                formData.addFilePart("file", imageFile);
                
                // Create the request
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/images/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + formData.getBoundary())
                    .POST(formData.getBodyPublisher())
                    .build();
                
                // Send the request
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Process the response
                if (response.statusCode() == 200) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(
                        new IOException("Failed to upload image: " + response.body()));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();
        
        return future;
    }
    
    /**
     * Fetch an image from the server
     * @param imageId The ID of the image to fetch
     * @return A CompletableFuture with the Image on success
     */
    public CompletableFuture<Image> fetchImage(String imageId) {
        // Check cache first
        if (imageCache.containsKey(imageId)) {
            return CompletableFuture.completedFuture(imageCache.get(imageId));
        }
        
        // Check if there's already a pending request for this image
        CompletableFuture<Image> existingRequest = pendingRequests.get(imageId);
        if (existingRequest != null) {
            return existingRequest;
        }
        
        // Create new request
        CompletableFuture<Image> future = new CompletableFuture<>();
        pendingRequests.put(imageId, future);
        
        // If not in cache, request from server
        try {
            ImageMessage fetchMessage = new ImageMessage();
            fetchMessage.setType(ImageMessage.Type.FETCH_REQUEST);
            fetchMessage.setImageId(imageId);
            
            // Send the message
            webSocketClient.sendMessage(fetchMessage)
                .exceptionally(ex -> {
                    // Remove from pending and complete with error
                    pendingRequests.remove(imageId);
                    future.completeExceptionally(ex);
                    return null;
                });
                
        } catch (Exception e) {
            pendingRequests.remove(imageId);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private void handleImageResponse(ImageMessage message) {
        if (message.getType() == ImageMessage.Type.FETCH_RESPONSE) {
            String imageId = message.getImageId();
            CompletableFuture<Image> pendingRequest = pendingRequests.remove(imageId);
            
            if (pendingRequest != null) {
                if (message.isSuccess() && message.getImageData() != null) {
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(message.getImageData());
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        
                        // Cache the image
                        imageCache.put(imageId, image);
                        
                        // Complete the specific request
                        pendingRequest.complete(image);
                    } catch (Exception e) {
                        pendingRequest.completeExceptionally(
                                new IOException("Error decoding image data: " + e.getMessage()));
                    }
                } else {
                    pendingRequest.completeExceptionally(
                            new IOException(message.getMessage() != null ? message.getMessage() : "Image fetch failed"));
                }
            } else {
                System.err.println("Received image response for unknown request: " + imageId);
            }
        }
    }
    
    // Add method to clear pending requests if needed
    public void clearPendingRequests() {
        pendingRequests.clear();
    }
    
    // Clear the cache when needed
    public void clearCache() {
        imageCache.clear();
    }
    
    // Helper class for multipart uploads
    private static class MultipartFormData {
        private final String boundary;
        private final ByteArrayOutputStream baos;
        
        public MultipartFormData() {
            // Create a unique boundary
            this.boundary = UUID.randomUUID().toString();
            this.baos = new ByteArrayOutputStream();
        }
        
        public String getBoundary() {
            return boundary;
        }
        
        @SuppressWarnings("unused")
        public void addFormField(String name, String value) throws IOException {
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            baos.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        
        public void addFilePart(String name, File file) throws IOException {
            baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            
            // Add file headers
            baos.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" 
                        + file.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            
            // Determine content type
            String contentType = getContentType(file.getName());
            baos.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            
            // Copy file contents directly to output stream
            Files.copy(file.toPath(), baos);
            baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        
        private String getContentType(String filename) {
            if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".png")) {
                return "image/png";
            } else {
                return "application/octet-stream";
            }
        }
        
        public HttpRequest.BodyPublisher getBodyPublisher() throws IOException {
            // Add the final boundary
            baos.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
            
            // Create body publisher from byte array
            return HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray());
        }
    }
}