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
import java.util.function.Consumer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    
    private Consumer<ImageMessage> fetchHandler;
    
    private void setImageFetchHandler(Consumer<ImageMessage> handler) {
        this.fetchHandler = handler;
    }
    
    private void handleImageResponse(ImageMessage message) {
        if (message.getType() == ImageMessage.Type.FETCH_RESPONSE && fetchHandler != null) {
            fetchHandler.accept(message);
        }
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