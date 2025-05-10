package com.uninaswap.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.uninaswap.server.websocket.WebSocketMessageRouter;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketMessageRouter webSocketMessageRouter;
    
    public WebSocketConfig(WebSocketMessageRouter webSocketMessageRouter) {
        this.webSocketMessageRouter = webSocketMessageRouter;
    }
    
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Use the router as the main entry point for all WebSocket messages
        registry.addHandler(webSocketMessageRouter, "/ws").setAllowedOrigins("*");
    }
}