package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.ImageService;
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

/**
 * 
 */
public class UserMenuDropdownController {
    
    /**
     * 
     */
    @FXML private ImageView userAvatar;
    /**
     * 
     */
    @FXML private Text displayNameText;
    /**
     * 
     */
    @FXML private Text usernameText;
    /**
     * 
     */
    @FXML private Button viewProfileBtn;
    /**
     * 
     */
    @FXML private Button inventoryBtn;
    /**
     * 
     */
    @FXML private Button listingsBtn;
    /**
     * 
     */
    @FXML private Button favoritesBtn;
    /**
     * 
     */
    @FXML private Button offersBtn;
    /**
     * 
     */
    @FXML private Button followersBtn;
    /**
     * 
     */
    @FXML private Button settingsBtn;
    /**
     * 
     */
    @FXML private Button supportBtn;
    /**
     * 
     */
    @FXML private ComboBox<String> themeCombo;
    /**
     * 
     */
    @FXML private ComboBox<Locale> languageCombo;
    /**
     * 
     */
    @FXML private Button logoutBtn;
    
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    /**
     * 
     */
    private final ImageService imageService = ImageService.getInstance();
    
    /**
     * 
     */
    private Consumer<Void> onCloseCallback;
    /**
     * 
     */
    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
        "English", Locale.ENGLISH,
        "Italiano", Locale.ITALIAN
    );
    
    /**
     * 
     */
    @FXML
    public void initialize() {
        setupUserInfo();
        setupThemeComboBox();
        setupLanguageComboBox();
    }
    
    /**
     * @param callback
     */
    public void setOnCloseCallback(Consumer<Void> callback) {
        this.onCloseCallback = callback;
    }
    
    /**
     * 
     */
    private void setupUserInfo() {
        if (userSessionService.isLoggedIn()) {
            displayNameText.setText(userSessionService.getUser().getUsername());
            usernameText.setText("@" + userSessionService.getUser().getUsername());
            String imagePath = userSessionService.getUser().getProfileImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageService.fetchImage(imagePath)
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            userAvatar.setImage(image);
                        }
                    }))
                    .exceptionally(ex -> {
                        System.err.println("Failed to load user avatar in menu: " + ex.getMessage());
                        return null;
                    });
            }
        }
    }
    
    /**
     * 
     */
    private void setupThemeComboBox() {
        themeCombo.setItems(FXCollections.observableArrayList(
            localeService.getMessage("theme.light", "Light"),
            localeService.getMessage("theme.dark", "Dark"),
            localeService.getMessage("theme.system", "System")
        ));
        themeCombo.setValue(localeService.getMessage("theme.light", "Light")); // Default
        
        themeCombo.setOnAction(_ -> {
            String selectedTheme = themeCombo.getValue();
            applyTheme(selectedTheme);
        });
    }
    
    /**
     * 
     */
    private void setupLanguageComboBox() {
        languageCombo.setItems(FXCollections.observableArrayList(SUPPORTED_LANGUAGES.values()));        languageCombo.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                if (locale == null) return null;
                return locale.getDisplayLanguage(locale);
            }
            
            @Override
            public Locale fromString(String string) {
                return null;
            }
        });
        languageCombo.setValue(localeService.getCurrentLocale());
        languageCombo.valueProperty().addListener((_, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                localeService.setLocale(newValue);
                Platform.runLater(() -> {
                    refreshLocalizedLabels();
                });
            }
        });
    }
    
    /**
     * 
     */
    private void refreshLocalizedLabels() {
        String currentTheme = themeCombo.getValue();
        themeCombo.setItems(FXCollections.observableArrayList(
            localeService.getMessage("theme.light", "Light"),
            localeService.getMessage("theme.dark", "Dark"),
            localeService.getMessage("theme.system", "System")
        ));
        if (currentTheme != null) {
            if (currentTheme.contains("Light") || currentTheme.contains("Chiaro")) {
                themeCombo.setValue(localeService.getMessage("theme.light", "Light"));
            } else if (currentTheme.contains("Dark") || currentTheme.contains("Scuro")) {
                themeCombo.setValue(localeService.getMessage("theme.dark", "Dark"));
            } else {
                themeCombo.setValue(localeService.getMessage("theme.system", "System"));
            }
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleViewProfile() {
        closeDropdown();
        try {
            navigationService.navigateToProfileView(userSessionService.getUserViewModel());
        } catch (Exception e) {
            System.err.println("Failed to navigate to profile: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleInventory() {
        closeDropdown();
        try {
            navigationService.navigateToInventoryView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to inventory: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleMyListings() {
        closeDropdown();
        try {
            navigationService.navigateToListingsView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to listings: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleFavorites() {
        closeDropdown();
        try {
            navigationService.navigateToUserFavoritesView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to favorites: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleOffers() {
        closeDropdown();
        try {
            navigationService.navigateToOffersView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to offers: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleFollowers() {
        closeDropdown();
        try {
            navigationService.navigateToUserFollowersView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to followers: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleSettings() {
        closeDropdown();
        try {
            navigationService.navigateToSettingsView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to settings: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleSupport() {
        closeDropdown();
        try {
            navigationService.navigateToSupportView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to support: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleLogout() {
        closeDropdown();
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(localeService.getMessage("logout.confirm.title", "Confirm Logout"));
        confirmDialog.setHeaderText(localeService.getMessage("logout.confirm.header", "Are you sure you want to logout?"));
        confirmDialog.setContentText(localeService.getMessage("logout.confirm.content", "You will need to login again to access your account."));
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    navigationService.logout();
                } catch (Exception e) {
                    System.err.println("Failed to logout: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * @param theme
     */
    private void applyTheme(String theme) {
        try {
            System.out.println("Applying theme: " + theme);
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    private void closeDropdown() {
        if (onCloseCallback != null) {
            onCloseCallback.accept(null);
        }
    }
}