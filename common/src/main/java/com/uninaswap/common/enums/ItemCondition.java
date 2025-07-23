package com.uninaswap.common.enums;

/**
 * Represents the condition of an item
 */
/**
 * 
 */
public enum ItemCondition {
    /**
     * 
     */
    NEW("New"),
    /**
     * 
     */
    LIKE_NEW("Like New"),
    /**
     * 
     */
    VERY_GOOD("Very Good"),
    /**
     * 
     */
    GOOD("Good"),
    /**
     * 
     */
    ACCEPTABLE("Acceptable"),
    /**
     * 
     */
    FOR_PARTS("For Parts");
    
    /**
     * 
     */
    private final String displayName;
    
    /**
     * @param displayName
     */
    ItemCondition(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }
}