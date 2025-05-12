package com.uninaswap.client.constants;

/**
 * Constants for event types used with the EventBusService
 */
public final class EventTypes {
    // Prevent instantiation
    private EventTypes() {}
    
    // User events
    public static final String USER_LOGGED_IN = "user.logged_in";
    public static final String USER_LOGGED_OUT = "user.logged_out";
    
    // Locale events
    public static final String LOCALE_CHANGED = "locale.changed";
    
    // Other event types...
    public static final String PROFILE_IMAGE_CHANGED = "profile.image.changed";
}