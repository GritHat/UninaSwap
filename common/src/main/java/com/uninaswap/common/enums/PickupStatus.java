package com.uninaswap.common.enums;

public enum PickupStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    PickupStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}