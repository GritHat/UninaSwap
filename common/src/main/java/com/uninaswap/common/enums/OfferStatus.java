package com.uninaswap.common.enums;

/**
 * Represents the status of an offer
 */
public enum OfferStatus {
    PENDING("offer.status.pending"),
    ACCEPTED("offer.status.accepted"),
    REJECTED("offer.status.rejected"),
    WITHDRAWN("offer.status.withdrawn"),
    EXPIRED("offer.status.expired"),
    COMPLETED("offer.status.completed");

    private final String messageKey;

    OfferStatus(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDisplayName() {
        return messageKey;
    }
}