package com.uninaswap.client.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Service for managing application messages and internationalization.
 * This separates message handling from controllers.
 */
public class MessageService {
    private static MessageService instance;
    
    // Resource bundle for messages
    private ResourceBundle messageBundle;
    
    // Fallback messages for when the resource bundle doesn't have a key
    private final Map<String, String> fallbackMessages = new HashMap<>();
    
    // Singleton pattern
    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }
    
    private MessageService() {
        // Load the resource bundle for the default locale
        try {
            messageBundle = ResourceBundle.getBundle("i18n/messages", Locale.getDefault());
            System.out.println("Loaded message bundle: " + messageBundle.getBaseBundleName());
        } catch (MissingResourceException e) {
            System.err.println("Warning: Could not load message bundle. Using fallback messages.");
            // Initialize fallback messages as a backup
            initializeFallbackMessages();
        }
    }
    
    /**
     * Set the locale for messages
     */
    public void setLocale(Locale locale) {
        messageBundle = ResourceBundle.getBundle("i18n/messages", locale);
    }
    
    
    /**
     * Get a message by key
     */
    public String getMessage(String key) {
        try {
            if (messageBundle != null && messageBundle.containsKey(key)) {
                return messageBundle.getString(key);
            }
        } catch (MissingResourceException e) {
            // Fall through to fallback
            System.err.println("Warning: Missing resource for key: " + key);
        }
        
        // Use fallback message if the key is not found in the bundle
        return fallbackMessages.getOrDefault(key, "Missing message: " + key);
    }
    
    /**
     * Get a message by key with parameters
     */
    public String getMessage(String key, Object... params) {
        String pattern = getMessage(key);
        return MessageFormat.format(pattern, params);
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
        
        // Dashboard messages
        fallbackMessages.put("dashboard.welcome", "Welcome to UninaSwap Dashboard");
        fallbackMessages.put("dashboard.status.ready", "Ready");
        fallbackMessages.put("dashboard.status.connected", "Connected");
        fallbackMessages.put("dashboard.status.loaded", "Dashboard loaded");
        fallbackMessages.put("dashboard.error.logout", "Error logging out: {0}");
        fallbackMessages.put("dashboard.view.dashboard", "Dashboard view selected");
        fallbackMessages.put("dashboard.view.markets", "Markets view selected");
        fallbackMessages.put("dashboard.view.portfolio", "Portfolio view selected");
        fallbackMessages.put("dashboard.view.trade", "Trade view selected");
        fallbackMessages.put("dashboard.view.settings", "Settings view selected");
    }
}