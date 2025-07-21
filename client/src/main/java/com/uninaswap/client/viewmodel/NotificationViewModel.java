package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class NotificationViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final ObjectProperty<UserViewModel> recipient = new SimpleObjectProperty<>();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty message = new SimpleStringProperty();
    private final StringProperty data = new SimpleStringProperty();
    private final BooleanProperty read = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> readAt = new SimpleObjectProperty<>();
    
    // Getters and setters for JavaFX properties
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }
    public StringProperty idProperty() { return id; }
    
    public UserViewModel getRecipient() { return recipient.get(); }
    public void setRecipient(UserViewModel recipient) { this.recipient.set(recipient); }
    public ObjectProperty<UserViewModel> recipientProperty() { return recipient; }
    
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }
    
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }
    
    public String getMessage() { return message.get(); }
    public void setMessage(String message) { this.message.set(message); }
    public StringProperty messageProperty() { return message; }
    
    public String getData() { return data.get(); }
    public void setData(String data) { this.data.set(data); }
    public StringProperty dataProperty() { return data; }
    
    public boolean isRead() { return read.get(); }
    public void setRead(boolean read) { this.read.set(read); }
    public BooleanProperty readProperty() { return read; }
    
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    public LocalDateTime getReadAt() { return readAt.get(); }
    public void setReadAt(LocalDateTime readAt) { this.readAt.set(readAt); }
    public ObjectProperty<LocalDateTime> readAtProperty() { return readAt; }
}