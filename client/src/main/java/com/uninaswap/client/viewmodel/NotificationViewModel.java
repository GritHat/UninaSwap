package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * 
 */
public class NotificationViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> recipient = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final StringProperty type = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty title = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty message = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty data = new SimpleStringProperty();
    /**
     * 
     */
    private final BooleanProperty read = new SimpleBooleanProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> readAt = new SimpleObjectProperty<>();
    
    // Getters and setters for JavaFX properties
    /**
     * @return
     */
    public String getId() { return id.get(); }
    /**
     * @param id
     */
    public void setId(String id) { this.id.set(id); }
    /**
     * @return
     */
    public StringProperty idProperty() { return id; }
    
    /**
     * @return
     */
    public UserViewModel getRecipient() { return recipient.get(); }
    /**
     * @param recipient
     */
    public void setRecipient(UserViewModel recipient) { this.recipient.set(recipient); }
    /**
     * @return
     */
    public ObjectProperty<UserViewModel> recipientProperty() { return recipient; }
    
    /**
     * @return
     */
    public String getType() { return type.get(); }
    /**
     * @param type
     */
    public void setType(String type) { this.type.set(type); }
    /**
     * @return
     */
    public StringProperty typeProperty() { return type; }
    
    /**
     * @return
     */
    public String getTitle() { return title.get(); }
    /**
     * @param title
     */
    public void setTitle(String title) { this.title.set(title); }
    /**
     * @return
     */
    public StringProperty titleProperty() { return title; }
    
    /**
     * @return
     */
    public String getMessage() { return message.get(); }
    /**
     * @param message
     */
    public void setMessage(String message) { this.message.set(message); }
    /**
     * @return
     */
    public StringProperty messageProperty() { return message; }
    
    /**
     * @return
     */
    public String getData() { return data.get(); }
    /**
     * @param data
     */
    public void setData(String data) { this.data.set(data); }
    /**
     * @return
     */
    public StringProperty dataProperty() { return data; }
    
    /**
     * @return
     */
    public boolean isRead() { return read.get(); }
    /**
     * @param read
     */
    public void setRead(boolean read) { this.read.set(read); }
    /**
     * @return
     */
    public BooleanProperty readProperty() { return read; }
    
    /**
     * @return
     */
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    /**
     * @return
     */
    public LocalDateTime getReadAt() { return readAt.get(); }
    /**
     * @param readAt
     */
    public void setReadAt(LocalDateTime readAt) { this.readAt.set(readAt); }
    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> readAtProperty() { return readAt; }
}