package com.uninaswap.client.service;

import java.util.HashMap;
import java.util.Map;

import com.uninaswap.common.message.AuthMessage;

/**
 * Service for managing the current user's session throughout the application.
 */
public class UserSessionService {
    private static UserSessionService instance;
    
    // Session data
    private String username;
    private String email;
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
    public void startSession(AuthMessage response) {
        this.username = response.getUsername();
        this.email = response.getEmail();
        this.sessionData.put("firstName", response.getFirstName());
        this.sessionData.put("lastName", response.getLastName());
        this.sessionData.put("bio", response.getBio());
        this.sessionData.put("profileImagePath", response.getProfileImagePath());
        this.loggedIn = true;
        // Additional initialization can happen here
        System.out.println("Session started for user: " + username + " with email: " + email);
    }
    
    /**
     * End the user session (logout)
     */
    public void endSession() {
        this.username = null;
        this.email = null;
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
     * Get the current email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Store additional data in the session
     */
    public void put(String key, Object value) {
        sessionData.put(key, value);
    }
    
    /**
     * Retrieve data from the session.
     * <p>
     * IMPORTANT: This method performs an unchecked cast. The caller is responsible for
     * ensuring that the requested type matches the actual stored type. If types don't match,
     * a ClassCastException will be thrown at runtime.
     * 
     * @param <T> The expected type of the value
     * @param key The key to look up
     * @return The value cast to the expected type, or null if not found
     * @throws ClassCastException if the value cannot be cast to the expected type
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