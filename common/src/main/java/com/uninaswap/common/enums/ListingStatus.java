package com.uninaswap.common.enums;

/**
 * Represents the status of a listing in the system
 */
public enum ListingStatus {
    ACTIVE,      // Listing is currently active and available
    PENDING,     // Listing is awaiting approval
    COMPLETED,   // Transaction has been completed
    CANCELLED,   // Listing was cancelled by the creator
    EXPIRED      // Listing has expired (e.g., auction ended)
}