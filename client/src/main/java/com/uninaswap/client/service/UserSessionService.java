package com.uninaswap.client.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing the current user's session throughout the application.
 */
public class UserSessionService {
    private static UserSessionService instance;
    
    // Session data
    private String username;
    private boolean loggedIn = false;
    private final Map<String, Object> sessionData = new HashMap<>();
    
    // Singleton pattern
    public static UserSessionService getInstance() {
        if (instance == null) {
            instance = new UserSessionService();
        }
        return instance;
    }
    
    private UserSessionService() {
        // Private constructor to enforce singleton
    }
    
    /**
     * Start a new user session after successful login
     */
    public void startSession(String username) {
        this.username = username;
        this.loggedIn = true;
        // Additional initialization can happen here
        System.out.println("Session started for user: " + username);
    }
    
    /**
     * End the user session (logout)
     */
    public void endSession() {
        this.username = null;
        this.loggedIn = false;
        this.sessionData.clear();
        System.out.println("Session ended");
    }
    
    /**
     * Check if a user is logged in
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    /**
     * Get the current username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Store additional data in the session
     */
    public void put(String key, Object value) {
        sessionData.put(key, value);
    }
    
    /**
     * Retrieve data from the session
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sessionData.get(key);
    }
    
    /**
     * Check if the session contains a specific key
     */
    public boolean contains(String key) {
        return sessionData.containsKey(key);
    }
    
    /**
     * Remove data from the session
     */
    public void remove(String key) {
        sessionData.remove(key);
    }
}