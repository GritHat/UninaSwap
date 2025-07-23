package com.uninaswap.common.enums;

/**
 * Represents the status of an offer
 */
/**
 * 
 */
public enum OfferStatus {
    /**
     * 
     */
    PENDING("offer.status.pending"),
    /**
     * 
     */
    ACCEPTED("offer.status.accepted"),
    /**
     * 
     */
    PICKUPSCHEDULING("offer.status.pickupscheduling"),
    /**
     * 
     */
    PICKUPRESCHEDULING("offer.status.pickuprescheduling"),
    /**
     * 
     */
    CONFIRMED("offer.status.confirmed"),
    /**
     * 
     */
    SELLERVERIFIED("offer.status.sellerverified"),
    /**
     * 
     */
    BUYERVERIFIED("offer.status.buyerverified"),
    /**
     * 
     */
    CANCELLED("offer.status.cancelled"),
    /**
     * 
     */
    REJECTED("offer.status.rejected"),
    /**
     * 
     */
    WITHDRAWN("offer.status.withdrawn"),
    /**
     * 
     */
    EXPIRED("offer.status.expired"),
    /**
     * 
     */
    COMPLETED("offer.status.completed"),
    /**
     * 
     */
    REVIEWED("offer.status.reviewed");

    /**
     * 
     */
    private final String messageKey;

    /**
     * @param messageKey
     */
    OfferStatus(String messageKey) {
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