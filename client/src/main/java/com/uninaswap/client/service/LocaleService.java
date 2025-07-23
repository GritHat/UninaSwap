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
/**
 * 
 */
public class LocaleService {
    /**
     * 
     */
    private static LocaleService instance;
    /**
     * 
     */
    private ResourceBundle messageBundle;
    /**
     * 
     */
    private final Map<String, String> fallbackMessages = new HashMap<>();
    /**
     * 
     */
    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>(Locale.getDefault());
    /**
     * 
     */
    private final EventBusService eventBus = EventBusService.getInstance();

    /**
     * @return
     */
    public static LocaleService getInstance() {
        if (instance == null) {
            instance = new LocaleService();
        }
        return instance;
    }

    /**
     * 
     */
    private LocaleService() {
        loadSavedLocale();
    }

    /**
     * Load the resource bundle for the given locale
     * 
     * @param locale The locale to load messages for
     */
    /**
     * @param locale
     */
    private void loadResourceBundle(Locale locale) {
        try {
            messageBundle = ResourceBundle.getBundle("i18n/messages", locale);
            System.out.println("Loaded message bundle: " + messageBundle.getBaseBundleName() +
                    " for locale: " + locale.getDisplayName());
        } catch (MissingResourceException e) {
            System.err.println("Warning: Could not load message bundle for locale " +
                    locale.getDisplayName() + ". Using fallback messages.");
            initializeFallbackMessages();
        }
    }

    /**
     * Set the locale for messages
     * 
     * @param locale The new locale to set
     */
    /**
     * @param locale
     */
    public void setLocale(Locale locale) {
        if (locale == null)
            return;
        Locale oldLocale = currentLocale.get();
        loadResourceBundle(locale);
        currentLocale.set(locale);
        Preferences prefs = Preferences.userNodeForPackage(LocaleService.class);
        prefs.put("locale.language", locale.getLanguage());
        prefs.put("locale.country", locale.getCountry());
        Map<String, Locale> eventData = Map.of(
                "oldLocale", oldLocale,
                "newLocale", locale);
        eventBus.publishEvent(EventTypes.LOCALE_CHANGED, eventData);
    }

    /**
     * Set the language using language code (e.g., "it" for Italian)
     * 
     * @param language The language code to set
     */
    /**
     * @param language
     */
    public void setLanguage(String language) {
        setLocale(Locale.of(language));
    }

    /**
     * Get the current locale
     * 
     * @return The current locale used for messages
     */
    /**
     * @return
     */
    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    /**
     * Get the locale property for binding in JavaFX UI
     * 
     * @return The current locale property
     */
    /**
     * @return
     */
    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    /**
     * Get the current resource bundle
     * 
     * @return The resource bundle containing messages for the current locale
     */
    /**
     * @return
     */
    public ResourceBundle getResourceBundle() {
        return messageBundle;
    }

    /**
     * Get a message by key
     * 
     * @param key The key for the message
     * @return The message string for the given key, or a fallback message if not found
     */
    /**
     * @param key
     * @return
     */
    public String getMessage(String key) {
        try {
            if (messageBundle != null && messageBundle.containsKey(key)) {
                return messageBundle.getString(key);
            }
        } catch (MissingResourceException e) {
            System.err.println("Warning: Missing resource for key: " + key);
        }
        return fallbackMessages.getOrDefault(key, "Missing message: " + key);
    }

    /**
     * Get a message by key with parameters
     * 
     * @param key    The key for the message
     * @param params Parameters to format the message with
     * @return The formatted message
     */
    /**
     * @param key
     * @param params
     * @return
     */
    public String getMessage(String key, Object... params) {
        String pattern = getMessage(key);
        return MessageFormat.format(pattern, params);
    }

    /**
     * 
     */
    private void initializeFallbackMessages() {
        fallbackMessages.put("login.error.username.required", "Username is required");
        fallbackMessages.put("login.error.password.required", "Password is required");
        fallbackMessages.put("login.error.connection", "Failed to connect to server");
        fallbackMessages.put("login.info.logging", "Logging in...");
        fallbackMessages.put("login.success", "Login successful");
        fallbackMessages.put("login.error.failed", "Login failed");
        fallbackMessages.put("register.error.username.required", "Username is required");
        fallbackMessages.put("register.error.email.required", "Email is required");
        fallbackMessages.put("register.error.email.invalid", "Invalid email format");
        fallbackMessages.put("register.error.password.required", "Password is required");
        fallbackMessages.put("register.error.password.mismatch", "Passwords do not match");
        fallbackMessages.put("register.info.registering", "Registering...");
        fallbackMessages.put("register.success", "Registration successful. You can now login.");
        fallbackMessages.put("register.error.connection", "Failed to connect to server");
        fallbackMessages.put("register.error.failed", "Registration failed");
        fallbackMessages.put("navigation.error.load.register", "Failed to load register view");
        fallbackMessages.put("navigation.error.load.login", "Failed to load login view");
        fallbackMessages.put("navigation.error.load.dashboard", "Failed to load dashboard");
        fallbackMessages.put("validation.success", "Validation successful");
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
        fallbackMessages.put("profile.save.success", "Profile changes saved successfully");
        fallbackMessages.put("profile.save.error", "Failed to save profile changes");
        fallbackMessages.put("profile.save.inprogress", "Saving profile changes...");
        fallbackMessages.put("profile.error.connection", "Failed to connect to server");
    }

    /**
     * Load saved locale preference on service initialization
     * 
     * This method reads the user's saved locale from preferences
     * and sets the current locale accordingly.
     * If no saved locale is found, it defaults to English.
     */
    /**
     * 
     */
    private void loadSavedLocale() {
        Preferences prefs = Preferences.userNodeForPackage(LocaleService.class);
        String language = prefs.get("locale.language", Locale.ENGLISH.getLanguage());
        String country = prefs.get("locale.country", Locale.ENGLISH.getCountry());
        setLocale(Locale.of(language, country));
    }
}