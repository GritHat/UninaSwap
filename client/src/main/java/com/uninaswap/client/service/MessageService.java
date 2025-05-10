package com.uninaswap.client.service;

import java.util.HashMap;
import java.util.Map;
import java.text.MessageFormat;

/**
 * Service for managing application messages and internationalization.
 * This separates message handling from controllers.
 */
public class MessageService {
    private static MessageService instance;
    
    // Singleton pattern
    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }
    
    // Fallback messages for when the resource bundle doesn't have a key
    private final Map<String, String> fallbackMessages = new HashMap<>();
    
    private MessageService() {
        // Initialize fallback messages
        initializeFallbackMessages();
    }
    
    private void initializeFallbackMessages() {
        // Login messages
        fallbackMessages.put("login.error.username.required", "Username is required");
        fallbackMessages.put("login.error.password.required", "Password is required");
        fallbackMessages.put("login.error.connection", "Failed to connect to server");
        fallbackMessages.put("login.info.logging", "Logging in...");
        fallbackMessages.put("login.success", "Login successful");
        fallbackMessages.put("login.error.failed", "Login failed");
        
        // Register messages
        fallbackMessages.put("register.error.username.required", "Username is required");
        fallbackMessages.put("register.error.email.required", "Email is required");
        fallbackMessages.put("register.error.email.invalid", "Invalid email format");
        fallbackMessages.put("register.error.password.required", "Password is required");
        fallbackMessages.put("register.error.password.mismatch", "Passwords do not match");
        fallbackMessages.put("register.info.registering", "Registering...");
        fallbackMessages.put("register.success", "Registration successful. You can now login.");
        fallbackMessages.put("register.error.connection", "Failed to connect to server");
        fallbackMessages.put("register.error.failed", "Registration failed");
        
        // Navigation messages
        fallbackMessages.put("navigation.error.load.register", "Failed to load register view");
        fallbackMessages.put("navigation.error.load.login", "Failed to load login view");
        fallbackMessages.put("navigation.error.load.dashboard", "Failed to load dashboard");
        
        // General messages
        fallbackMessages.put("validation.success", "Validation successful");
    }
    
    /**
     * Get a message by key
     */
    public String getMessage(String key) {
        return fallbackMessages.getOrDefault(key, "Missing message: " + key);
    }
    
    /**
     * Get a message by key with parameters
     */
    public String getMessage(String key, Object... params) {
        String pattern = getMessage(key);
        return MessageFormat.format(pattern, params);
    }
}