package com.uninaswap.common.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "messageType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AuthMessage.class, name = "auth"),
    @JsonSubTypes.Type(value = ProfileUpdateMessage.class, name = "profile")
})
public abstract class Message {
    private String messageId;
    private long timestamp;
    private String messageType; // The discriminator field
    
    public Message() {
        this.messageId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}