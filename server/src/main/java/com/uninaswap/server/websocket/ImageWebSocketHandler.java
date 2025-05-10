package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.uninaswap.common.message.ImageMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.UserRepository;
import com.uninaswap.server.service.ImageService;

import java.io.IOException;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {
    
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ImageWebSocketHandler(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        System.out.println("SERVER RECEIVED IMAGE REQUEST: " + message.getPayload().substring(0, 100) + "...");
        
        try {
            ImageMessage imageMessage = objectMapper.readValue(message.getPayload(), ImageMessage.class);
            ImageMessage response = new ImageMessage();
            
            switch (imageMessage.getType()) {
                case UPLOAD_REQUEST:
                    handleImageUpload(imageMessage, response);
                    break;
                
                case FETCH_REQUEST:
                    handleImageFetch(imageMessage, response);
                    break;
                    
                default:
                    response.setSuccess(false);
                    response.setMessage("Unknown image message type");
            }
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            
        } catch (Exception e) {
            System.err.println("Error handling image message: " + e.getMessage());
            
            ImageMessage errorResponse = new ImageMessage();
            errorResponse.setType(ImageMessage.Type.UPLOAD_RESPONSE); // Default to upload response
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error processing image: " + e.getMessage());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
    
    private void handleImageUpload(ImageMessage request, ImageMessage response) {
        try {
            // Save the image and get its ID
            String imageId = imageService.saveImage(request.getImageData(), request.getFormat());
            
            // Update user entity with the image ID
            UserEntity user = userRepository.findByUsername(request.getUsername()).orElse(null);
            if (user != null) {
                // Store the image ID in the format "imageId.format"
                user.setProfileImagePath(imageId + "." + request.getFormat());
                userRepository.save(user);
                
                response.setType(ImageMessage.Type.UPLOAD_RESPONSE);
                response.setSuccess(true);
                response.setImageId(imageId);
                response.setMessage("Image uploaded successfully");
            } else {
                response.setType(ImageMessage.Type.UPLOAD_RESPONSE);
                response.setSuccess(false);
                response.setMessage("User not found");
            }
        } catch (IOException e) {
            response.setType(ImageMessage.Type.UPLOAD_RESPONSE);
            response.setSuccess(false);
            response.setMessage("Failed to save image: " + e.getMessage());
        }
    }
    
    private void handleImageFetch(ImageMessage request, ImageMessage response) {
        try {
            String[] parts = request.getImageId().split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid image ID format, expected 'id.format'");
            }
            
            String imageId = parts[0];
            String format = parts[1];
            
            // Retrieve the image
            String imageData = imageService.getImage(imageId, format);
            
            response.setType(ImageMessage.Type.FETCH_RESPONSE);
            response.setSuccess(true);
            response.setImageData(imageData);
            response.setFormat(format);
            response.setImageId(request.getImageId());
            
        } catch (Exception e) {
            response.setType(ImageMessage.Type.FETCH_RESPONSE);
            response.setSuccess(false);
            response.setMessage("Failed to retrieve image: " + e.getMessage());
        }
    }
}