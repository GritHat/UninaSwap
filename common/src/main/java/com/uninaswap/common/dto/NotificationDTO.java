package com.uninaswap.common.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String id;
    private UserDTO recipient;
    private String type;
    private String title;
    private String message;
    private String data; // JSON string for additional data
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Constructors
    public NotificationDTO() {}
    
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
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public UserDTO getRecipient() { return recipient; }
    public void setRecipient(UserDTO recipient) { this.recipient = recipient; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
