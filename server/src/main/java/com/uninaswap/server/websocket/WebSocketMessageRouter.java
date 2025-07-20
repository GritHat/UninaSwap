package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.message.AuthMessage;
import com.uninaswap.common.message.AuthMessage.Type;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.service.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageRouter.class);
    
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
            ReviewWebSocketHandler reviewHandler,
            FavoriteWebSocketHandler favoriteHandler,
            SearchWebSocketHandler searchHandler,
            NotificationWebSocketHandler notificationHandler,
            AnalyticsWebSocketHandler analyticsHandler,
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
        handlerMap.put("review", reviewHandler);
        handlerMap.put("search", searchHandler);
        handlerMap.put("notification", notificationHandler);
        handlerMap.put("analytics", analyticsHandler);
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
            if (typeNode != null && !typeNode.asText().equals("auth"))
            {
                if (tokenNode != null && tokenNode.isTextual()) {
                    String token = tokenNode.asText();
                    UserEntity user = sessionService.validateToken(token);

                    if (user != null) {
                        // Update session authentication if token is valid
                        sessionService.createAuthenticatedSession(session, user);
                    } else {
                        logger.info("Invalid token for session: {}", session.getId());
                        AuthMessage response = new AuthMessage();
                        response.setSuccess(false);
                        response.setType(Type.AUTH_ERROR_RESPONSE);
                        response.setErrorMessage("Invalid token");
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        return;
                    }
                } else {
                    AuthMessage response = new AuthMessage();
                    response.setSuccess(false);
                    response.setType(Type.AUTH_ERROR_RESPONSE);
                    response.setErrorMessage("Token is required for this message type");
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

                    logger.info("Unauthenticated session for message type: {}", typeNode.asText());
                    return;
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