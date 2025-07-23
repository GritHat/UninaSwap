package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.uninaswap.common.message.ImageMessage;
import com.uninaswap.server.service.ImageService;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    public ImageWebSocketHandler(ImageService imageService, ObjectMapper objectMapper) {
        this.imageService = imageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        System.out.println("SERVER RECEIVED IMAGE REQUEST: "
                + message.getPayload().substring(0, Math.min(100, message.getPayload().length())) + "...");

        try {
            ImageMessage imageMessage = objectMapper.readValue(message.getPayload(), ImageMessage.class);
            ImageMessage response = new ImageMessage();

            if (imageMessage.getType() == ImageMessage.Type.FETCH_REQUEST) {
                handleImageFetch(imageMessage, response);
            } else {
                response.setSuccess(false);
                response.setMessage("Unknown image message type");
            }

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            System.err.println("Error handling image message: " + e.getMessage());

            ImageMessage errorResponse = new ImageMessage();
            errorResponse.setType(ImageMessage.Type.FETCH_RESPONSE); 
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error processing image: " + e.getMessage());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
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