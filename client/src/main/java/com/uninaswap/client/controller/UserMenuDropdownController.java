package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class UserMenuDropdownController implements Refreshable {
    
    @FXML private ImageView userAvatar;
    @FXML private Text displayNameText;
    @FXML private Text usernameText;
    @FXML private Button viewProfileBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button listingsBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button offersBtn;
    @FXML private Button followersBtn;
    @FXML private Button settingsBtn;
    @FXML private Button supportBtn;
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<Locale> languageCombo;
    @FXML private Button logoutBtn;
    
    // Additional UI elements for localization
    @FXML private Text themeLabel;
    @FXML private Text languageLabel;
    
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    
    private Consumer<Void> onCloseCallback;
    
    // Supported languages (same as FooterController)
    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
        "English", Locale.ENGLISH,
        "Italiano", Locale.ITALIAN
    );
    
    @FXML
    public void initialize() {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        setupUserInfo();
        setupThemeComboBox();
        setupLanguageComboBox();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("user.menu.debug.initialized", "UserMenuDropdown controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update button texts
        updateButtonTexts();
        
        // Update theme and language labels
        updateLabels();
        
        // Refresh combo box items with current locale
        refreshComboBoxItems();
        
        // Refresh user info
        updateUserInfo();
        
        System.out.println(localeService.getMessage("user.menu.debug.ui.refreshed", "UserMenuDropdown UI refreshed"));
    }

    private void updateButtonTexts() {
        if (viewProfileBtn != null) {
            viewProfileBtn.setText(localeService.getMessage("user.menu.view.profile", "View Profile"));
        }
        if (inventoryBtn != null) {
            // Button text is handled by FXML with %user.menu.inventory, but we update tooltip
            Tooltip.install(inventoryBtn, new Tooltip(localeService.getMessage("user.menu.inventory.tooltip", "Manage your inventory")));
        }
        if (listingsBtn != null) {
            Tooltip.install(listingsBtn, new Tooltip(localeService.getMessage("user.menu.listings.tooltip", "View your listings")));
        }
        if (favoritesBtn != null) {
            Tooltip.install(favoritesBtn, new Tooltip(localeService.getMessage("user.menu.favorites.tooltip", "View your favorite items")));
        }
        if (offersBtn != null) {
            Tooltip.install(offersBtn, new Tooltip(localeService.getMessage("user.menu.offers.tooltip", "View your offers")));
        }
        if (followersBtn != null) {
            Tooltip.install(followersBtn, new Tooltip(localeService.getMessage("user.menu.followers.tooltip", "View followers and following")));
        }
        if (settingsBtn != null) {
            Tooltip.install(settingsBtn, new Tooltip(localeService.getMessage("user.menu.settings.tooltip", "Application settings")));
        }
        if (supportBtn != null) {
            Tooltip.install(supportBtn, new Tooltip(localeService.getMessage("user.menu.support.tooltip", "Get help and support")));
        }
        if (logoutBtn != null) {
            Tooltip.install(logoutBtn, new Tooltip(localeService.getMessage("user.menu.logout.tooltip", "Sign out of your account")));
        }
    }

    private void updateLabels() {
        if (themeLabel != null) {
            themeLabel.setText(localeService.getMessage("user.menu.theme", "Theme"));
        }
        if (languageLabel != null) {
            languageLabel.setText(localeService.getMessage("user.menu.language", "Language"));
        }
    }

    private void refreshComboBoxItems() {
        // Refresh theme ComboBox items
        refreshThemeComboBox();
        
        // Refresh language ComboBox display
        refreshLanguageComboBox();
    }

    private void updateUserInfo() {
        if (userSessionService.isLoggedIn()) {
            String username = userSessionService.getUser().getUsername();
            if (displayNameText != null) {
                displayNameText.setText(username != null ? username : 
                    localeService.getMessage("user.menu.unknown.user", "Unknown User"));
            }
            if (usernameText != null) {
                usernameText.setText(username != null ? "@" + username : 
                    localeService.getMessage("user.menu.unknown.username", "@unknown"));
            }
        } else {
            if (displayNameText != null) {
                displayNameText.setText(localeService.getMessage("user.menu.not.logged.in", "Not Logged In"));
            }
            if (usernameText != null) {
                usernameText.setText(localeService.getMessage("user.menu.guest", "@guest"));
            }
        }
    }
    
    public void setOnCloseCallback(Consumer<Void> callback) {
        this.onCloseCallback = callback;
        System.out.println(localeService.getMessage("user.menu.debug.callback.set", "Close callback set"));
    }
    
    private void setupUserInfo() {
        if (userSessionService.isLoggedIn()) {
            try {
                String username = userSessionService.getUser().getUsername();
                if (displayNameText != null) {
                    displayNameText.setText(username != null ? username : 
                        localeService.getMessage("user.menu.unknown.user", "Unknown User"));
                }
                if (usernameText != null) {
                    usernameText.setText(username != null ? "@" + username : 
                        localeService.getMessage("user.menu.unknown.username", "@unknown"));
                }
                
                // Load user avatar
                String imagePath = userSessionService.getUser().getProfileImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    imageService.fetchImage(imagePath)
                        .thenAccept(image -> Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                userAvatar.setImage(image);
                                System.out.println(localeService.getMessage("user.menu.debug.avatar.loaded", "User avatar loaded successfully"));
                            } else {
                                System.out.println(localeService.getMessage("user.menu.debug.avatar.default", "Using default avatar"));
                            }
                        }))
                        .exceptionally(ex -> {
                            System.err.println(localeService.getMessage("user.menu.debug.avatar.error", "Failed to load user avatar: {0}")
                                .replace("{0}", ex.getMessage()));
                            return null;
                        });
                } else {
                    System.out.println(localeService.getMessage("user.menu.debug.avatar.no.path", "No avatar image path available"));
                }
            } catch (Exception e) {
                System.err.println(localeService.getMessage("user.menu.debug.user.info.error", "Error setting up user info: {0}")
                    .replace("{0}", e.getMessage()));
            }
        } else {
            System.out.println(localeService.getMessage("user.menu.debug.not.logged.in", "User not logged in, showing guest info"));
        }
    }
    
    private void setupThemeComboBox() {
        try {
            refreshThemeComboBox();
            
            // Set default theme
            themeCombo.setValue(localeService.getMessage("theme.light", "Light"));
            
            themeCombo.setOnAction(_ -> {
                String selectedTheme = themeCombo.getValue();
                if (selectedTheme != null) {
                    applyTheme(selectedTheme);
                    System.out.println(localeService.getMessage("user.menu.debug.theme.changed", "Theme changed to: {0}")
                        .replace("{0}", selectedTheme));
                }
            });
            
            System.out.println(localeService.getMessage("user.menu.debug.theme.setup", "Theme combo box setup completed"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.theme.setup.error", "Error setting up theme combo box: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void refreshThemeComboBox() {
        if (themeCombo == null) return;
        
        try {
            String currentTheme = themeCombo.getValue();
            themeCombo.getItems().clear();
            
            themeCombo.setItems(FXCollections.observableArrayList(
                localeService.getMessage("theme.light", "Light"),
                localeService.getMessage("theme.dark", "Dark"),
                localeService.getMessage("theme.system", "System")
            ));
            
            // Restore theme selection (find equivalent in new language)
            if (currentTheme != null) {
                if (currentTheme.contains("Light") || currentTheme.contains("Chiaro")) {
                    themeCombo.setValue(localeService.getMessage("theme.light", "Light"));
                } else if (currentTheme.contains("Dark") || currentTheme.contains("Scuro")) {
                    themeCombo.setValue(localeService.getMessage("theme.dark", "Dark"));
                } else {
                    themeCombo.setValue(localeService.getMessage("theme.system", "System"));
                }
            }
            
            System.out.println(localeService.getMessage("user.menu.debug.theme.refreshed", "Theme combo box refreshed"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.theme.refresh.error", "Error refreshing theme combo box: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    private void setupLanguageComboBox() {
        try {
            // Populate ComboBox with language options
            languageCombo.setItems(FXCollections.observableArrayList(SUPPORTED_LANGUAGES.values()));
            
            // Set a custom string converter to display language names
            languageCombo.setConverter(new StringConverter<Locale>() {
                @Override
                public String toString(Locale locale) {
                    if (locale == null) return null;
                    return getLocalizedLanguageName(locale);
                }
                
                @Override
                public Locale fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
            // Set current locale
            languageCombo.setValue(localeService.getCurrentLocale());
            
            // Add listener to change language when selection changes
            languageCombo.valueProperty().addListener((_, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    System.out.println(localeService.getMessage("user.menu.debug.language.changing", "Changing language from {0} to {1}")
                        .replace("{0}", oldValue != null ? oldValue.getDisplayLanguage() : "null")
                        .replace("{1}", newValue.getDisplayLanguage()));
                    
                    localeService.setLocale(newValue);
                    
                    // Refresh will be triggered by locale change event
                }
            });
            
            System.out.println(localeService.getMessage("user.menu.debug.language.setup", "Language combo box setup completed"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.language.setup.error", "Error setting up language combo box: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void refreshLanguageComboBox() {
        if (languageCombo == null) return;
        
        try {
            Locale currentLocale = languageCombo.getValue();
            // Force refresh of the combo box converter to update display text
            languageCombo.setConverter(languageCombo.getConverter());
            // Refresh the selected value to show updated text
            if (currentLocale != null) {
                languageCombo.setValue(null);
                languageCombo.setValue(currentLocale);
            }
            
            System.out.println(localeService.getMessage("user.menu.debug.language.refreshed", "Language combo box refreshed"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.language.refresh.error", "Error refreshing language combo box: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    /**
     * Get localized language name for display
     */
    private String getLocalizedLanguageName(Locale locale) {
        if (locale == null) return null;
        
        return switch (locale.getLanguage()) {
            case "en" -> localeService.getMessage("language.english", "English");
            case "it" -> localeService.getMessage("language.italian", "Italiano");
            default -> locale.getDisplayLanguage(locale);
        };
    }
    
    @FXML
    private void handleViewProfile() {
        closeDropdown();
        try {
            navigationService.navigateToProfileView(userSessionService.getUserViewModel());
            System.out.println(localeService.getMessage("user.menu.debug.navigation.profile", "Navigating to profile view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.profile.error", "Failed to navigate to profile: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleInventory() {
        closeDropdown();
        try {
            navigationService.navigateToInventoryView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.inventory", "Navigating to inventory view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.inventory.error", "Failed to navigate to inventory: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleMyListings() {
        closeDropdown();
        try {
            navigationService.navigateToListingsView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.listings", "Navigating to listings view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.listings.error", "Failed to navigate to listings: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleFavorites() {
        closeDropdown();
        try {
            navigationService.navigateToUserFavoritesView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.favorites", "Navigating to favorites view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.favorites.error", "Failed to navigate to favorites: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleOffers() {
        closeDropdown();
        try {
            navigationService.navigateToOffersView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.offers", "Navigating to offers view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.offers.error", "Failed to navigate to offers: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleFollowers() {
        closeDropdown();
        try {
            navigationService.navigateToUserFollowersView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.followers", "Navigating to followers view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.followers.error", "Failed to navigate to followers: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleSettings() {
        closeDropdown();
        try {
            navigationService.navigateToSettingsView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.settings", "Navigating to settings view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.settings.error", "Failed to navigate to settings: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleSupport() {
        closeDropdown();
        try {
            navigationService.navigateToSupportView();
            System.out.println(localeService.getMessage("user.menu.debug.navigation.support", "Navigating to support view"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.navigation.support.error", "Failed to navigate to support: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    private void handleLogout() {
        closeDropdown();
        
        try {
            // Show confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle(localeService.getMessage("logout.confirm.title", "Confirm Logout"));
            confirmDialog.setHeaderText(localeService.getMessage("logout.confirm.header", "Are you sure you want to logout?"));
            confirmDialog.setContentText(localeService.getMessage("logout.confirm.content", "You will need to login again to access your account."));
            
            // Localize buttons
            ButtonType okButton = new ButtonType(localeService.getMessage("button.ok", "OK"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType(localeService.getMessage("button.cancel", "Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmDialog.getButtonTypes().setAll(okButton, cancelButton);
            
            System.out.println(localeService.getMessage("user.menu.debug.logout.dialog", "Showing logout confirmation dialog"));
            
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == okButton) {
                    try {
                        System.out.println(localeService.getMessage("user.menu.debug.logout.confirmed", "Logout confirmed, signing out"));
                        navigationService.logout();
                    } catch (Exception e) {
                        System.err.println(localeService.getMessage("user.menu.debug.logout.error", "Failed to logout: {0}")
                            .replace("{0}", e.getMessage()));
                    }
                } else {
                    System.out.println(localeService.getMessage("user.menu.debug.logout.cancelled", "Logout cancelled by user"));
                }
            });
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.logout.dialog.error", "Error showing logout dialog: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    private void applyTheme(String theme) {
        try {
            // TODO: Implement theme switching when ThemeService is available
            System.out.println(localeService.getMessage("user.menu.debug.theme.applying", "Applying theme: {0}")
                .replace("{0}", theme != null ? theme : "null"));
            
            // For now, just log the theme change
            System.out.println(localeService.getMessage("user.menu.debug.theme.applied", "Theme applied successfully: {0}")
                .replace("{0}", theme != null ? theme : "null"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.theme.apply.error", "Failed to apply theme: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    private void closeDropdown() {
        try {
            if (onCloseCallback != null) {
                onCloseCallback.accept(null);
                System.out.println(localeService.getMessage("user.menu.debug.dropdown.closed", "User menu dropdown closed"));
            } else {
                System.out.println(localeService.getMessage("user.menu.debug.dropdown.no.callback", "No close callback set"));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.menu.debug.dropdown.close.error", "Error closing dropdown: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return userSessionService.isLoggedIn();
    }

    /**
     * Get current user's display name
     */
    public String getCurrentUserDisplayName() {
        if (userSessionService.isLoggedIn()) {
            String username = userSessionService.getUser().getUsername();
            return username != null ? username : localeService.getMessage("user.menu.unknown.user", "Unknown User");
        }
        return localeService.getMessage("user.menu.not.logged.in", "Not Logged In");
    }

    /**
     * Get current theme selection
     */
    public String getCurrentTheme() {
        return themeCombo != null ? themeCombo.getValue() : null;
    }

    /**
     * Get current language selection
     */
    public Locale getCurrentLanguage() {
        return languageCombo != null ? languageCombo.getValue() : null;
    }

    /**
     * Force refresh of user avatar
     */
    public void refreshUserAvatar() {
        setupUserInfo();
        System.out.println(localeService.getMessage("user.menu.debug.avatar.refresh.requested", "User avatar refresh requested"));
    }

    /**
     * Update user information display
     */
    public void updateUserDisplay() {
        updateUserInfo();
        System.out.println(localeService.getMessage("user.menu.debug.user.display.updated", "User display updated"));
    }

    /**
     * Check if dropdown is properly initialized
     */
    public boolean isInitialized() {
        return themeCombo != null && languageCombo != null && displayNameText != null;
    }

    /**
     * Get supported languages
     */
    public Map<String, Locale> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    /**
     * Set theme programmatically
     */
    public void setTheme(String themeName) {
        if (themeCombo != null && themeName != null) {
            themeCombo.setValue(themeName);
            System.out.println(localeService.getMessage("user.menu.debug.theme.set.programmatically", "Theme set programmatically: {0}")
                .replace("{0}", themeName));
        }
    }

    /**
     * Set language programmatically
     */
    public void setLanguage(Locale locale) {
        if (languageCombo != null && locale != null) {
            languageCombo.setValue(locale);
            System.out.println(localeService.getMessage("user.menu.debug.language.set.programmatically", "Language set programmatically: {0}")
                .replace("{0}", locale.getDisplayLanguage()));
        }
    }
}