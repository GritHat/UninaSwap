package com.uninaswap.common.enums;

/**
 * Represents the status of an offer
 */
public enum OfferStatus {
    PENDING,    // Offer is awaiting a response
    ACCEPTED,   // Offer has been accepted
    REJECTED,   // Offer has been rejected
    WITHDRAWN,  // Offer has been withdrawn by the user
    EXPIRED,    // Offer has expired (time limit)
    COMPLETED   // Transaction has been completed
}