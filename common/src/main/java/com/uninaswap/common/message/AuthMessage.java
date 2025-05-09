package com.uninaswap.common.message;

public class AuthMessage {
    public enum Type {
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        REGISTER_REQUEST,
        REGISTER_RESPONSE
    }
    
    private Type type;
    private String username;
    private String password;
    private String email;
    private boolean success;
    private String message;
    
    // Getters and Setters
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}