package com.uninaswap.client.service;

import java.util.regex.Pattern;

/**
 * Service for validating user input in forms.
 * This separates validation logic from controllers.
 */
public class ValidationService {
    private static ValidationService instance;
    
    // Singleton pattern
    public static ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }
    
    private ValidationService() {
        // Private constructor to enforce singleton
    }
    
    // Email regex pattern
    private final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    /**
     * Result class to contain both validation status and message key
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String messageKey;
        
        public ValidationResult(boolean valid, String messageKey) {
            this.valid = valid;
            this.messageKey = messageKey;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessageKey() {
            return messageKey;
        }
    }
    
    /**
     * Validate login form input
     */
    public ValidationResult validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "login.error.username.required");
        }
        
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "login.error.password.required");
        }
        
        return new ValidationResult(true, "validation.success");
    }
    
    /**
     * Validate registration form input
     */
    public ValidationResult validateRegistration(String username, String email, 
                                             String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "register.error.username.required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "register.error.email.required");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "register.error.email.invalid");
        }
        
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "register.error.password.required");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "register.error.password.mismatch");
        }
        
        return new ValidationResult(true, "validation.success");
    }
}