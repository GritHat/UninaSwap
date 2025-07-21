package com.uninaswap.server.entity;

import com.uninaswap.common.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at DESC"),
    @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read"),
    @Index(name = "idx_notification_type", columnList = "type")
})
public class NotificationEntity {
    
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // JSON string for additional data
    
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Constructors
    public NotificationEntity() {}
    
    public NotificationEntity(UserEntity recipient, NotificationType type, String title, String message) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    
    public NotificationEntity(UserEntity recipient, NotificationType type, String title, String message, String data) {
        this(recipient, type, title, message);
        this.data = data;
    }
    
    // Mark as read
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public UserEntity getRecipient() { return recipient; }
    public void setRecipient(UserEntity recipient) { this.recipient = recipient; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { 
        this.read = read; 
        if (read && readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}