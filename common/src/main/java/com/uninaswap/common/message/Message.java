package com.uninaswap.common.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "messageType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthMessage.class, name = "auth"),
        @JsonSubTypes.Type(value = ProfileUpdateMessage.class, name = "profile"),
        @JsonSubTypes.Type(value = ImageMessage.class, name = "image"),
        @JsonSubTypes.Type(value = ItemMessage.class, name = "item"),
        @JsonSubTypes.Type(value = ListingMessage.class, name = "listing"),
        @JsonSubTypes.Type(value = OfferMessage.class, name = "offer"),
        @JsonSubTypes.Type(value = FavoriteMessage.class, name = "favorite"),
        @JsonSubTypes.Type(value = FollowerMessage.class, name = "follower"),
        @JsonSubTypes.Type(value = PickupMessage.class, name = "pickup"),
        @JsonSubTypes.Type(value = SearchMessage.class, name = "search")
})
public abstract class Message {
    private String messageId;
    private long timestamp;
    private String messageType;
    private String token;
    private boolean success;
    private String errorMessage;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}