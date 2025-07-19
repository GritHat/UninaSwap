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

import java.util.function.Consumer;

public class UserMenuDropdownController {
    
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
    @FXML private ComboBox<String> themeCombo;
    @FXML private Button logoutBtn;
    
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    
    private Consumer<Void> onCloseCallback;
    
    @FXML
    public void initialize() {
        setupUserInfo();
        setupThemeComboBox();
    }
    
    public void setOnCloseCallback(Consumer<Void> callback) {
        this.onCloseCallback = callback;
    }
    
    private void setupUserInfo() {
        if (userSessionService.isLoggedIn()) {
            displayNameText.setText(userSessionService.getUser().getUsername());
            usernameText.setText("@" + userSessionService.getUser().getUsername());
            
            // Load user avatar
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
    
    @FXML
    private void handleViewProfile() {
        closeDropdown();
        try {
            navigationService.loadProfileView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to profile: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleInventory() {
        closeDropdown();
        try {
            navigationService.loadInventoryView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to inventory: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleMyListings() {
        closeDropdown();
        try {
            // TODO : implement navigation to My Listings
            System.out.println("Navigating to My Listings");
            //navigationService.loadMyListingsView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to listings: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFavorites() {
        closeDropdown();
        try {
            // TODO : implement navigation to Favorites
            System.out.println("Navigating to Favorites");
            //navigationService.loadFavoritesView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to favorites: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleOffers() {
        closeDropdown();
        try {
            navigationService.loadOffersView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to offers: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFollowers() {
        closeDropdown();
        try {
            // TODO : implement navigation to Followers
            System.out.println("Navigating to Followers");
            // navigationService.loadFollowersView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to followers: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSettings() {
        closeDropdown();
        try {
            // TODO : implement navigation to Settings
            System.out.println("Navigating to Settings");
            // navigationService.loadSettings();
        } catch (Exception e) {
            System.err.println("Failed to navigate to settings: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        closeDropdown();
        
        // Show confirmation dialog
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
    
    private void applyTheme(String theme) {
        try {
            // TODO: Implement theme switching when ThemeService is available
            System.out.println("Applying theme: " + theme);
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + e.getMessage());
        }
    }
    
    private void closeDropdown() {
        if (onCloseCallback != null) {
            onCloseCallback.accept(null);
        }
    }
}