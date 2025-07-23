package com.uninaswap.common.enums;

/**
 * Represents the status of a listing in the system
 */
/**
 * 
 */
public enum ListingStatus {
    /**
     * 
     */
    PENDING("listing.status.pending"),
    /**
     * 
     */
    ACTIVE("listing.status.active"),
    /**
     * 
     */
    COMPLETED("listing.status.sold"),
    /**
     * 
     */
    CANCELLED("listing.status.cancelled"),
    /**
     * 
     */
    EXPIRED("listing.status.expired");

    /**
     * 
     */
    private final String messageKey;

    /**
     * @param messageKey
     */
    ListingStatus(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * @return
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return
     */
    public String getDisplayName() {
        return messageKey;
    }
}