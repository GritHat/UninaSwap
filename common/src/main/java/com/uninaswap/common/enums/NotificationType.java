package com.uninaswap.common.enums;

public enum NotificationType {
    OFFER_RECEIVED("OFFER_RECEIVED"),
    OFFER_ACCEPTED("OFFER_ACCEPTED"),
    OFFER_REJECTED("OFFER_REJECTED"),
    OFFER_WITHDRAWN("OFFER_WITHDRAWN"),
    AUCTION_ENDING_SOON("AUCTION_ENDING_SOON"),
    AUCTION_WON("AUCTION_WON"),
    AUCTION_OUTBID("AUCTION_OUTBID"),
    LISTING_EXPIRED("LISTING_EXPIRED"),
    PICKUP_SCHEDULED("PICKUP_SCHEDULED"),
    PICKUP_REMINDER("PICKUP_REMINDER"),
    MESSAGE_RECEIVED("MESSAGE_RECEIVED"),
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT"),
    PROFILE_UPDATED("PROFILE_UPDATED"),
    FAVORITE_LISTING_UPDATED("FAVORITE_LISTING_UPDATED");
    
    private final String value;
    
    NotificationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}