package com.uninaswap.common.dto;

import java.time.LocalDateTime;

/**
 * 
 */
public class NotificationDTO {
    /**
     * 
     */
    private String id;
    /**
     * 
     */
    private UserDTO recipient;
    /**
     * 
     */
    private String type;
    /**
     * 
     */
    private String title;
    /**
     * 
     */
    private String message;
    /**
     * 
     */
    private String data; 
    /**
     * 
     */
    private boolean read;
    /**
     * 
     */
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime readAt;
    
    
    /**
     * 
     */
    public NotificationDTO() {}
    
    /**
     * @param id
     * @param recipient
     * @param type
     * @param title
     * @param message
     * @param data
     * @param read
     * @param createdAt
     * @param readAt
     */
    public NotificationDTO(String id, UserDTO recipient, String type, String title, 
                          String message, String data, boolean read, 
                          LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }
    
    
    /**
     * @return
     */
    public String getId() { return id; }
    /**
     * @param id
     */
    public void setId(String id) { this.id = id; }
    
    /**
     * @return
     */
    public UserDTO getRecipient() { return recipient; }
    /**
     * @param recipient
     */
    public void setRecipient(UserDTO recipient) { this.recipient = recipient; }
    
    /**
     * @return
     */
    public String getType() { return type; }
    /**
     * @param type
     */
    public void setType(String type) { this.type = type; }
    
    /**
     * @return
     */
    public String getTitle() { return title; }
    /**
     * @param title
     */
    public void setTitle(String title) { this.title = title; }
    
    /**
     * @return
     */
    public String getMessage() { return message; }
    /**
     * @param message
     */
    public void setMessage(String message) { this.message = message; }
    
    /**
     * @return
     */
    public String getData() { return data; }
    /**
     * @param data
     */
    public void setData(String data) { this.data = data; }
    
    /**
     * @return
     */
    public boolean isRead() { return read; }
    /**
     * @param read
     */
    public void setRead(boolean read) { this.read = read; }
    
    /**
     * @return
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    /**
     * @return
     */
    public LocalDateTime getReadAt() { return readAt; }
    /**
     * @param readAt
     */
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
