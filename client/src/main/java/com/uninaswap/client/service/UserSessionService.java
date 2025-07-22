package com.uninaswap.client.service;

import java.util.HashMap;
import java.util.Map;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.AuthMessage;

/**
 * Service for managing the current user's session throughout the application.
 */
public class UserSessionService {
    private static UserSessionService instance;
    private UserDTO user;
    private boolean loggedIn = false;
    private final Map<String, Object> sessionData = new HashMap<>();
    private final EventBusService eventBus = EventBusService.getInstance();
    
    public static UserSessionService getInstance() {
        if (instance == null) {
            instance = new UserSessionService();
        }
        return instance;
    }
    
    private UserSessionService() {
    }
    
    /**
     * Start a new user session after successful login
     * 
     * @param response The AuthMessage containing user data and token
     */
    public void startSession(AuthMessage response) {
        this.user = response.getUser();
        this.loggedIn = true;
        
        if (response.getToken() != null) {
            this.sessionData.put("token", response.getToken());
        }

        EventBusService.getInstance().publishEvent(EventTypes.USER_LOGGED_IN, user);
        
        System.out.println("Session started for user: " + user.getUsername());
    }
    
    /**
     * End the user session (logout)
     */
    public void endSession() {
        eventBus.publishEvent(EventTypes.USER_LOGGED_OUT, null);
        this.user = null;
        this.loggedIn = false;
        this.sessionData.clear();
        
        System.out.println("Session ended and logout event published");
    }
    
    /**
     * Get the current user
     * 
     * @return The UserDTO representing the current user, or null if not logged in
     */
    public UserDTO getUser() {
        return user;
    }

    public UserViewModel getUserViewModel() {
        return ViewModelMapper.getInstance().toViewModel(user);
    }

    /**
     * Check if a user is logged in
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Store additional data in the session
     * 
     * @param key The key to store the data under
     * @param value The value to store
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
     * 
     * @param key The key to check for
     * @return true if the key exists in the session, false otherwise
     */
    public boolean contains(String key) {
        return sessionData.containsKey(key);
    }
    
    /**
     * Remove data from the session
     * 
     * @param key The key to remove
     */
    public void remove(String key) {
        sessionData.remove(key);
    }
    
    /**
     * Get the authentication token
     */
    public String getToken() {
        return (String) sessionData.get("token");
    }
}