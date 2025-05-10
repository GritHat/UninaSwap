package com.uninaswap.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.uninaswap.server.websocket.AuthWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final AuthWebSocketHandler authWebSocketHandler;
    
    public WebSocketConfig(AuthWebSocketHandler authWebSocketHandler) {
        this.authWebSocketHandler = authWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(authWebSocketHandler, "/auth")
                .setAllowedOrigins("*"); // In production, limit this to specific origins
    }
}