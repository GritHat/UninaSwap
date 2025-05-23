package com.uninaswap.client.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import com.uninaswap.client.constants.EventTypes;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Service for managing application messages and internationalization.
 * This separates message handling from controllers.
 */
public class LocaleService {
    private static LocaleService instance;
    
    // Resource bundle for messages
    private ResourceBundle messageBundle;
    
    // Fallback messages for when the resource bundle doesn't have a key
    private final Map<String, String> fallbackMessages = new HashMap<>();
    
    // Observable property for the current locale
    private final ObjectProperty<Locale> currentLocale = 
        new SimpleObjectProperty<>(Locale.getDefault());
    
    // EventBusService instance for publishing events
    private final EventBusService eventBus = EventBusService.getInstance();
    
    // Singleton pattern
    public static LocaleService getInstance() {
        if (instance == null) {
            instance = new LocaleService();
        }
        return instance;
    }
    
    private LocaleService() {
        // Load saved locale preference on service initialization
        loadSavedLocale();
    }
    
    /**
     * Load the resource bundle for the given locale
     */
    private void loadResourceBundle(Locale locale) {
        try {
            messageBundle = ResourceBundle.getBundle("i18n/messages", locale);
            System.out.println("Loaded message bundle: " + messageBundle.getBaseBundleName() + 
                               " for locale: " + locale.getDisplayName());
        } catch (MissingResourceException e) {
            System.err.println("Warning: Could not load message bundle for locale " + 
                              locale.getDisplayName() + ". Using fallback messages.");
            // Initialize fallback messages as a backup
            initializeFallbackMessages();
        }
    }
    
    /**
     * Set the locale for messages
     */
    public void setLocale(Locale locale) {
        if (locale == null) return;
        
        // Store previous locale for event data
        Locale oldLocale = currentLocale.get();
        
        // Load the new resource bundle
        loadResourceBundle(locale);
        
        // Update the current locale
        currentLocale.set(locale);
        
        // Store the selected locale in preferences
        Preferences prefs = Preferences.userNodeForPackage(LocaleService.class);
        prefs.put("locale.language", locale.getLanguage());
        prefs.put("locale.country", locale.getCountry());
        
        // Publish locale changed event with old and new locales
        Map<String, Locale> eventData = Map.of(
            "oldLocale", oldLocale,
            "newLocale", locale
        );
        eventBus.publishEvent(EventTypes.LOCALE_CHANGED, eventData);
    }
    
    /**
     * Set the language using language code (e.g., "it" for Italian)
     */
    public void setLanguage(String language) {
        setLocale(Locale.of(language));
    }
    
    /**
     * Get the current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale.get();
    }
    
    /**
     * Get the locale property for binding in JavaFX UI
     */
    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }
    
    /**
     * Get the current resource bundle
     */
    public ResourceBundle getResourceBundle() {
        return messageBundle;
    }
    
    /**
     * Get a message by key
     */
    public String getMessage(String key) {
        try {
            if (messageBundle != null && messageBundle.containsKey(key)) {
                return messageBundle.getString(key);
            }
        } catch (MissingResourceException e) {
            // Fall through to fallback
            System.err.println("Warning: Missing resource for key: " + key);
        }
        
        // Use fallback message if the key is not found in the bundle
        return fallbackMessages.getOrDefault(key, "Missing message: " + key);
    }
    
    /**
     * Get a message by key with parameters
     */
    public String getMessage(String key, Object... params) {
        String pattern = getMessage(key);
        return MessageFormat.format(pattern, params);
    }

    private void initializeFallbackMessages() {
        // Login messages
        fallbackMessages.put("login.error.username.required", "Username is required");
        fallbackMessages.put("login.error.password.required", "Password is required");
        fallbackMessages.put("login.error.connection", "Failed to connect to server");
        fallbackMessages.put("login.info.logging", "Logging in...");
        fallbackMessages.put("login.success", "Login successful");
        fallbackMessages.put("login.error.failed", "Login failed");
        
        // Register messages
        fallbackMessages.put("register.error.username.required", "Username is required");
        fallbackMessages.put("register.error.email.required", "Email is required");
        fallbackMessages.put("register.error.email.invalid", "Invalid email format");
        fallbackMessages.put("register.error.password.required", "Password is required");
        fallbackMessages.put("register.error.password.mismatch", "Passwords do not match");
        fallbackMessages.put("register.info.registering", "Registering...");
        fallbackMessages.put("register.success", "Registration successful. You can now login.");
        fallbackMessages.put("register.error.connection", "Failed to connect to server");
        fallbackMessages.put("register.error.failed", "Registration failed");
        
        // Navigation messages
        fallbackMessages.put("navigation.error.load.register", "Failed to load register view");
        fallbackMessages.put("navigation.error.load.login", "Failed to load login view");
        fallbackMessages.put("navigation.error.load.dashboard", "Failed to load dashboard");
        
        // General messages
        fallbackMessages.put("validation.success", "Validation successful");
        
        // Dashboard messages
        fallbackMessages.put("dashboard.welcome", "Welcome to UninaSwap Dashboard");
        fallbackMessages.put("dashboard.welcome.user", "Welcome, {0}");
        fallbackMessages.put("dashboard.status.ready", "Ready");
        fallbackMessages.put("dashboard.status.connected", "Connected");
        fallbackMessages.put("dashboard.status.loaded", "Dashboard loaded");
        fallbackMessages.put("dashboard.error.logout", "Error logging out: {0}");
        fallbackMessages.put("dashboard.view.dashboard", "Dashboard view selected");
        fallbackMessages.put("dashboard.view.markets", "Markets view selected");
        fallbackMessages.put("dashboard.view.portfolio", "Portfolio view selected");
        fallbackMessages.put("dashboard.view.trade", "Trade view selected");
        fallbackMessages.put("dashboard.view.settings", "Settings view selected");

        // Profile messages
        fallbackMessages.put("profile.save.success", "Profile changes saved successfully");
        fallbackMessages.put("profile.save.error", "Failed to save profile changes");
        fallbackMessages.put("profile.save.inprogress", "Saving profile changes...");
        fallbackMessages.put("profile.error.connection", "Failed to connect to server");
    }

    /**
     * Load saved locale preference on service initialization
     */
    private void loadSavedLocale() {
        Preferences prefs = Preferences.userNodeForPackage(LocaleService.class);
        String language = prefs.get("locale.language", Locale.ENGLISH.getLanguage());
        String country = prefs.get("locale.country", Locale.ENGLISH.getCountry());
        setLocale(Locale.of(language, country));
    }
}