package com.uninaswap.common.enums;

/**
 * Represents the condition of an item
 */
public enum ItemCondition {
    NEW("New"),
    LIKE_NEW("Like New"),
    VERY_GOOD("Very Good"),
    GOOD("Good"),
    ACCEPTABLE("Acceptable"),
    FOR_PARTS("For Parts");
    
    private final String displayName;
    
    ItemCondition(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}