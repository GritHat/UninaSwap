package com.uninaswap.common.message;

public class ImageMessage extends Message {
    
    public enum Type {
        UPLOAD_REQUEST,
        UPLOAD_RESPONSE,
        FETCH_REQUEST,
        FETCH_RESPONSE
    }
    
    private Type type;
    private String username;
    private String imageId;
    private String imageData; // Base64 encoded image
    private String format; // e.g., "png", "jpg"
    private boolean success;
    private String message;
    
    public ImageMessage() {
        super();
        setMessageType("image");
    }
    
    // Getters and setters
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
    
    public String getImageId() {
        return imageId;
    }
    
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    
    public String getImageData() {
        return imageData;
    }
    
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
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