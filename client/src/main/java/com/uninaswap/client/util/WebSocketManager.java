package com.uninaswap.client.util;

import com.uninaswap.client.websocket.WebSocketClient;

public class WebSocketManager {
    private static WebSocketClient instance;
    
    public static WebSocketClient getClient() {
        if (instance == null) {
            instance = new WebSocketClient();
        }
        return instance;
    }
}