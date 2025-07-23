package com.uninaswap.common.message;

/**
 * 
 */
public class ImageMessage extends Message {

    /**
     * 
     */
    public enum Type {
        FETCH_REQUEST,
        FETCH_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private String username;
    /**
     * 
     */
    private String imageId;
    /**
     * 
     */
    private String imageData; // Base64 encoded image
    /**
     * 
     */
    private String format; // e.g., "png", "jpg"
    /**
     * 
     */
    private String message;

    /**
     * 
     */
    public ImageMessage() {
        super();
        setMessageType("image");
    }

    // Getters and setters
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * @param imageId
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * @return
     */
    public String getImageData() {
        return imageData;
    }

    /**
     * @param imageData
     */
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    /**
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}