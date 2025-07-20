package com.uninaswap.server.service;

import com.uninaswap.server.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage WebSocket sessions and authentication state
 */
@Service
public class SessionService {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    // Map WebSocketSession IDs to UserEntities for authenticated users
    private final Map<WebSocketSession, UserEntity> authenticatedSessions = new ConcurrentHashMap<>();
    
    // Map tokens to UserEntities for token-based authentication
    private final Map<String, UserEntity> tokenToUserMap = new ConcurrentHashMap<>();
    
    /**
     * Validates if a session is authenticated and returns the associated user
     * 
     * @param session The WebSocket session to validate
     * @return The authenticated user, or null if not authenticated
     */
    public UserEntity validateSession(WebSocketSession session) {
        String sessionId = session.getId();
        UserEntity user = authenticatedSessions.get(session);
        
        if (user == null) {
            logger.debug("Session not authenticated: {}", sessionId);
            return null;
        }
        
        logger.debug("Session authenticated for user: {}", user.getUsername());
        return user;
    }
    
    public void InvalidateTokenAndSessionForUser(UserEntity user) {
        // Remove the user from the authenticated sessions
        authenticatedSessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(user.getId()));
        
        // Invalidate all tokens associated with this user
        tokenToUserMap.entrySet().removeIf(entry -> entry.getValue().getId().equals(user.getId()));
        
        logger.info("Invalidated session and tokens for user: {}", user.getUsername());
    }

    /**
     * Creates an authenticated session for a user
     * 
     * @param session The WebSocket session
     * @param user The authenticated user
     * @return A new authentication token
     */
    public String createAuthenticatedSession(WebSocketSession session, UserEntity user) {
        authenticatedSessions.put(session, user);
        
        // Generate a token that can be used for authentication in other contexts (like HTTP requests)
        String token = UUID.randomUUID().toString();
        tokenToUserMap.put(token, user);
        
        logger.info("Created authenticated session for user: {}", user.getUsername());
        return token;
    }
    
    /**
     * Validates a token and returns the associated user
     * 
     * @param token The authentication token
     * @return The authenticated user, or null if token is invalid
     */
    public UserEntity validateToken(String token) {
        return tokenToUserMap.get(token);
    }
    
    /**
     * Removes an authenticated session when a user logs out or disconnects
     * 
     * @param session The WebSocket session to invalidate
     */
    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        UserEntity user = authenticatedSessions.remove(sessionId);
        
        if (user != null) {
            logger.info("Removed authenticated session for user: {}", user.getUsername());
            
            // Also remove any tokens associated with this user
            tokenToUserMap.entrySet().removeIf(entry -> entry.getValue().equals(user));
        }
    }
    
    /**
     * Invalidate a specific token
     * 
     * @param token The token to invalidate
     */
    public void invalidateToken(String token) {
        UserEntity user = tokenToUserMap.remove(token);
        if (user != null) {
            logger.info("Invalidated token for user: {}", user.getUsername());
        }
    }

    public WebSocketSession getSessionByUserId(long userId) {
        for (Map.Entry<WebSocketSession, UserEntity> entry : authenticatedSessions.entrySet()) {
            if (entry.getValue().getId() == userId) {
                // Assuming you have a way to get the WebSocketSession by session ID
                return entry.getKey();
            }
        }
        return null;
    }
}