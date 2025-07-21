package com.uninaswap.common.enums;

public enum DeliveryType {
    PICKUP("Pickup"),
    SHIPPING("Shipping");

    private final String messageKey;

    DeliveryType(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDisplayName() {
        return messageKey;
    }

    @Override
    public String toString() {
        return messageKey;
    }
}
