package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.service.SessionService;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketMessageRouter extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, TextWebSocketHandler> handlerMap = new HashMap<>();
    private final SessionService sessionService;

    public WebSocketMessageRouter(
            ObjectMapper objectMapper,
            AuthWebSocketHandler authHandler,
            ProfileWebSocketHandler profileHandler,
            ImageWebSocketHandler imageHandler,
            ItemWebSocketHandler itemHandler,
            ListingWebSocketHandler listingHandler,
            OfferWebSocketHandler offerHandler,
            PickupWebSocketHandler pickupHandler,
            ReviewWebSocketHandler reviewHandler, // Add this parameter
            FavoriteWebSocketHandler favoriteHandler,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.sessionService = sessionService;

        // Register handlers for different message types
        handlerMap.put("auth", authHandler);
        handlerMap.put("profile", profileHandler);
        handlerMap.put("favorite", favoriteHandler);
        handlerMap.put("image", imageHandler);
        handlerMap.put("item", itemHandler);
        handlerMap.put("listing", listingHandler);
        handlerMap.put("offer", offerHandler);
        handlerMap.put("pickup", pickupHandler);
        handlerMap.put("review", reviewHandler); // Add this line
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("SERVER RECEIVED: " + payload);

        try {
            // First parse as JsonNode to extract the message type
            JsonNode jsonNode = objectMapper.readTree(payload);
            JsonNode typeNode = jsonNode.get("messageType");

            // Check for token-based authentication
            JsonNode tokenNode = jsonNode.get("token");
            if (tokenNode != null && tokenNode.isTextual()) {
                String token = tokenNode.asText();
                UserEntity user = sessionService.validateToken(token);

                if (user != null) {
                    // Update session authentication if token is valid
                    sessionService.createAuthenticatedSession(session, user);
                }
            }

            if (typeNode == null || !typeNode.isTextual()) {
                System.err.println("Invalid message format: missing or invalid 'messageType'");
                return;
            }

            String messageType = typeNode.asText();
            TextWebSocketHandler handler = handlerMap.get(messageType);

            if (handler != null) {
                // Forward to the appropriate handler
                handler.handleMessage(session, message);
            } else {
                System.err.println("No handler registered for message type: " + messageType);
            }

        } catch (Exception e) {
            System.err.println("Error routing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}