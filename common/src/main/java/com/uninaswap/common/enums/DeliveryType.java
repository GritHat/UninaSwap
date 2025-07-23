package com.uninaswap.common.enums;

/**
 * 
 */
public enum DeliveryType {
    /**
     * 
     */
    PICKUP("Pickup"),
    /**
     * 
     */
    SHIPPING("Shipping"),
    /**
     * 
     */
    BOTH("Both");

    /**
     * 
     */
    private final String messageKey;

    /**
     * @param messageKey
     */
    DeliveryType(String messageKey) {
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

    /**
     *
     */
    @Override
    public String toString() {
        return messageKey;
    }
}
