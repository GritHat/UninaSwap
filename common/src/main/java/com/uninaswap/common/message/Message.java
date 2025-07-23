package com.uninaswap.common.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 */
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
        @JsonSubTypes.Type(value = SearchMessage.class, name = "search"),
        @JsonSubTypes.Type(value = NotificationMessage.class, name = "notification"),
        @JsonSubTypes.Type(value = AnalyticsMessage.class, name = "analytics"),
        @JsonSubTypes.Type(value = ReviewMessage.class, name = "review"),
        @JsonSubTypes.Type(value = ReportMessage.class, name = "report")
})
public abstract class Message {
    /**
     * 
     */
    private String messageId;
    /**
     * 
     */
    private long timestamp;
    /**
     * 
     */
    private String messageType;
    /**
     * 
     */
    private String token;
    /**
     * 
     */
    private boolean success;
    /**
     * 
     */
    private String errorMessage;

    /**
     * 
     */
    public Message() {
        this.messageId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}