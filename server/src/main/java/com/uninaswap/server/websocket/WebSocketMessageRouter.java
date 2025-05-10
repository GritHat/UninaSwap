package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    public WebSocketMessageRouter(
            ObjectMapper objectMapper,
            AuthWebSocketHandler authHandler,
            ProfileWebSocketHandler profileHandler) {
        this.objectMapper = objectMapper;
        
        // Register handlers for different message types
        handlerMap.put("auth", authHandler);
        handlerMap.put("profile", profileHandler);
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("SERVER RECEIVED: " + payload);
        
        try {
            // First parse as JsonNode to extract the message type
            JsonNode jsonNode = objectMapper.readTree(payload);
            JsonNode typeNode = jsonNode.get("messageType");
            
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