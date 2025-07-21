package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SidebarController implements Refreshable {
    @FXML
    private ImageView ProfileImageIcon;
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView inventoryIcon;
    @FXML
    private ImageView notificationsIcon;
    @FXML
    private ImageView listingIcon;
    @FXML
    private ImageView offersIcon;

    @FXML
    private VBox homeButton;
    @FXML
    private VBox offersButton;
    @FXML
    private VBox inventoryButton;
    @FXML
    private VBox notificationsButton;
    @FXML
    private VBox listingButton;
    @FXML
    private VBox profileButton;

    // Caption text elements for localization
    @FXML
    private Text homeCaptionText;
    @FXML
    private Text listingsCaptionText;
    @FXML
    private Text offersCaptionText;
    @FXML
    private Text inventoryCaptionText;
    @FXML
    private Text notificationsCaptionText;
    @FXML
    private Text profileCaptionText;

    @FXML
    private javafx.scene.control.Label statusLabel;

    private final NavigationService navigationService;
    private final LocaleService localeService = LocaleService.getInstance();
    private final UserSessionService userSessionService = UserSessionService.getInstance();

    private MainController mainController;

    // Track currently selected button
    private VBox currentlySelected;

    public SidebarController() {
        this.navigationService = NavigationService.getInstance();
    }

    @FXML
    public void initialize() {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        // Set home as initially selected
        setSelectedButton(homeButton);
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("sidebar.debug.initialized", "Sidebar controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update caption texts if they exist (they should be automatically updated via FXML property binding)
        // However, we can manually update them if needed
        updateCaptionTexts();
        
        System.out.println(localeService.getMessage("sidebar.debug.ui.refreshed", "Sidebar UI refreshed"));
    }

    private void updateCaptionTexts() {
        // Note: In the FXML, captions use property binding like text="%home.caption"
        // These will be automatically updated when the locale changes
        // This method is here for manual updates if needed
        
        // Find Text nodes within each button and update them manually if property binding isn't working
        updateButtonCaptionText(homeButton, localeService.getMessage("home.caption", "Home"));
        updateButtonCaptionText(listingButton, localeService.getMessage("listings.caption", "Listings"));
        updateButtonCaptionText(offersButton, localeService.getMessage("offers.caption", "Offers"));
        updateButtonCaptionText(inventoryButton, localeService.getMessage("inventory.caption", "Inventory"));
        updateButtonCaptionText(notificationsButton, localeService.getMessage("notification.caption", "Notifications"));
        updateButtonCaptionText(profileButton, localeService.getMessage("user.caption", "Profile"));
    }

    private void updateButtonCaptionText(VBox button, String text) {
        if (button != null) {
            button.getChildren().stream()
                .filter(node -> node instanceof Text)
                .map(node -> (Text) node)
                .findFirst()
                .ifPresent(textNode -> textNode.setText(text));
        }
    }

    private void setSelectedButton(VBox selectedButton) {
        // Remove selected class from all buttons and reset icons
        clearAllSelections();
        
        // Add selected class to the new button and set white icon
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("selected");
            currentlySelected = selectedButton;
            setWhiteIcon(selectedButton);
            
            String buttonName = getButtonName(selectedButton);
            System.out.println(localeService.getMessage("sidebar.debug.button.selected", "Sidebar button selected: {0}")
                .replace("{0}", buttonName));
        }
    }

    public void clearAllSelections() {
        // Remove selected class from all sidebar buttons and reset to normal icons
        if (homeButton != null) {
            homeButton.getStyleClass().remove("selected");
            setNormalIcon(homeButton, homeIcon, "/images/icons/home.png");
        }
        if (offersButton != null) {
            offersButton.getStyleClass().remove("selected");
            setNormalIcon(offersButton, offersIcon, "/images/icons/offers.png");
        }
        if (inventoryButton != null) {
            inventoryButton.getStyleClass().remove("selected");
            setNormalIcon(inventoryButton, inventoryIcon, "/images/icons/inventory.png");
        }
        if (notificationsButton != null) {
            notificationsButton.getStyleClass().remove("selected");
            setNormalIcon(notificationsButton, notificationsIcon, "/images/icons/notification.png");
        }
        if (listingButton != null) {
            listingButton.getStyleClass().remove("selected");
            setNormalIcon(listingButton, listingIcon, "/images/icons/listings.png");
        }
        if (profileButton != null) {
            profileButton.getStyleClass().remove("selected");
            setNormalIcon(profileButton, ProfileImageIcon, "/images/icons/default_profile.png");
        }
        
        // Reset the currently selected tracker
        currentlySelected = null;
        
        System.out.println(localeService.getMessage("sidebar.debug.selections.cleared", "All sidebar selections cleared"));
    }

    private void setWhiteIcon(VBox button) {
        ImageView iconView = getIconFromButton(button);
        String whiteIconPath = getWhiteIconPath(button);
        
        if (iconView != null && whiteIconPath != null) {
            try {
                Image whiteIcon = new Image(getClass().getResourceAsStream(whiteIconPath));
                iconView.setImage(whiteIcon);
                System.out.println(localeService.getMessage("sidebar.debug.icon.white.set", "White icon set: {0}")
                    .replace("{0}", whiteIconPath));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("sidebar.debug.icon.white.error", 
                    "Could not load white icon: {0} - {1}")
                        .replace("{0}", whiteIconPath)
                        .replace("{1}", e.getMessage()));
            }
        }
    }

    private void setNormalIcon(VBox button, ImageView iconView, String normalIconPath) {
        if (iconView != null && normalIconPath != null) {
            try {
                Image normalIcon = new Image(getClass().getResourceAsStream(normalIconPath));
                iconView.setImage(normalIcon);
            } catch (Exception e) {
                System.err.println(localeService.getMessage("sidebar.debug.icon.normal.error", 
                    "Could not load normal icon: {0} - {1}")
                        .replace("{0}", normalIconPath)
                        .replace("{1}", e.getMessage()));
            }
        }
    }

    private ImageView getIconFromButton(VBox button) {
        if (button == homeButton) return homeIcon;
        if (button == offersButton) return offersIcon;
        if (button == inventoryButton) return inventoryIcon;
        if (button == notificationsButton) return notificationsIcon;
        if (button == listingButton) return listingIcon;
        if (button == profileButton) return ProfileImageIcon;
        return null;
    }

    private String getWhiteIconPath(VBox button) {
        if (button == homeButton) return "/images/icons/home_w.png";
        if (button == offersButton) return "/images/icons/offers_w.png";
        if (button == inventoryButton) return "/images/icons/inventory_w.png";
        if (button == notificationsButton) return "/images/icons/notification_w.png";
        if (button == listingButton) return "/images/icons/listings_w.png";
        if (button == profileButton) return "/images/icons/default_profile_w.png";
        return null;
    }

    private String getButtonName(VBox button) {
        if (button == homeButton) return localeService.getMessage("home.caption", "Home");
        if (button == offersButton) return localeService.getMessage("offers.caption", "Offers");
        if (button == inventoryButton) return localeService.getMessage("inventory.caption", "Inventory");
        if (button == notificationsButton) return localeService.getMessage("notification.caption", "Notifications");
        if (button == listingButton) return localeService.getMessage("listings.caption", "Listings");
        if (button == profileButton) return localeService.getMessage("user.caption", "Profile");
        return localeService.getMessage("sidebar.unknown.button", "Unknown");
    }

    @FXML
    public void showProfile(MouseEvent event) {
        setSelectedButton(profileButton);
        try {
            Parent profileView = navigationService.loadProfileView(userSessionService.getUserViewModel());
            mainController.setContent(profileView);
            System.out.println(localeService.getMessage("sidebar.debug.navigation.profile", "Navigated to profile view"));
        } catch (IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.profile.error", 
                "Error navigating to profile: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showHome(MouseEvent event) {
        setSelectedButton(homeButton);
        try {
            Parent homeView = navigationService.loadHomeView();
            mainController.setContent(homeView);
            
            // Update header buttons when leaving listing creation
            mainController.updateHeaderButtonSelection("refresh");
            System.out.println(localeService.getMessage("sidebar.debug.navigation.home", "Navigated to home view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.home.error", 
                "Error navigating to home: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showInventory(MouseEvent event) {
        setSelectedButton(inventoryButton);
        try {
            Parent inventoryView = navigationService.loadInventoryView();
            mainController.setContent(inventoryView);
            
            // Update header buttons when leaving listing creation
            mainController.updateHeaderButtonSelection("refresh");
            System.out.println(localeService.getMessage("sidebar.debug.navigation.inventory", "Navigated to inventory view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.inventory.error", 
                "Error navigating to inventory: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showListings(MouseEvent event) {
        setSelectedButton(listingButton);
        try {
            Parent listingsView = navigationService.loadListingsView();
            mainController.setContent(listingsView);
            
            // Update header buttons when navigating
            mainController.updateHeaderButtonSelection("refresh");
            System.out.println(localeService.getMessage("sidebar.debug.navigation.listings", "Navigated to listings view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.listings.error", 
                "Error navigating to listings: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showNotifications(MouseEvent event) {
        setSelectedButton(notificationsButton);
        try {
            navigationService.navigateToNotificationsView();
            // Update header buttons when leaving listing creation
            mainController.updateHeaderButtonSelection("refresh");
            System.out.println(localeService.getMessage("sidebar.debug.navigation.notifications", "Navigated to notifications view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.notifications.error", 
                "Error navigating to notifications: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(MouseEvent event) {
        // Settings might not need to be selected since it's accessed from user menu
        try {
            Parent settingsView = navigationService.loadSettingsView();
            mainController.setContent(settingsView);
            System.out.println(localeService.getMessage("sidebar.debug.navigation.settings", "Navigated to settings view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.settings.error", 
                "Error navigating to settings: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showSupport(MouseEvent event) {
        try {
            Parent supportView = navigationService.loadSupportView();
            mainController.setContent(supportView);
            System.out.println(localeService.getMessage("sidebar.debug.navigation.support", "Navigated to support view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.support.error", 
                "Error navigating to support: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void showOffers(MouseEvent event) {
        setSelectedButton(offersButton);
        try {
            Parent offersView = navigationService.loadOffersView();
            mainController.setContent(offersView);
            
            // Update header buttons when leaving listing creation
            mainController.updateHeaderButtonSelection("refresh");
            System.out.println(localeService.getMessage("sidebar.debug.navigation.offers", "Navigated to offers view"));
        } catch (java.io.IOException e) {
            System.err.println(localeService.getMessage("sidebar.debug.navigation.offers.error", 
                "Error navigating to offers: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(MouseEvent event) {
        try {
            navigationService.logout();
            System.out.println(localeService.getMessage("sidebar.debug.logout.success", "User logged out successfully"));
        } catch (Exception e) {
            String errorMessage = localeService.getMessage("sidebar.error.logout", "Error logging out: {0}")
                .replace("{0}", e.getMessage());
            
            if (statusLabel != null) {
                statusLabel.setText(errorMessage);
            }
            System.err.println(localeService.getMessage("sidebar.debug.logout.error", "Logout error: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods to allow external selection updates
    public void selectHomeButton() {
        setSelectedButton(homeButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.home", "Home button selected externally"));
    }

    public void selectOffersButton() {
        setSelectedButton(offersButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.offers", "Offers button selected externally"));
    }

    public void selectInventoryButton() {
        setSelectedButton(inventoryButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.inventory", "Inventory button selected externally"));
    }

    public void selectNotificationsButton() {
        setSelectedButton(notificationsButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.notifications", "Notifications button selected externally"));
    }

    public void selectAddListingButton() {
        setSelectedButton(listingButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.add.listing", "Add listing button selected externally"));
    }

    public void selectProfileButton() {
        setSelectedButton(profileButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.profile", "Profile button selected externally"));
    }

    public void selectListingsButton() {
        setSelectedButton(listingButton);
        System.out.println(localeService.getMessage("sidebar.debug.external.listings", "Listings button selected externally"));
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        System.out.println(localeService.getMessage("sidebar.debug.main.controller.set", "Main controller reference set"));
    }

    // Utility methods
    public VBox getCurrentlySelected() {
        return currentlySelected;
    }

    public boolean isButtonSelected(String buttonName) {
        VBox button = getButtonByName(buttonName);
        return button != null && button == currentlySelected;
    }

    private VBox getButtonByName(String buttonName) {
        return switch (buttonName.toLowerCase()) {
            case "home" -> homeButton;
            case "offers" -> offersButton;
            case "inventory" -> inventoryButton;
            case "notifications" -> notificationsButton;
            case "listings" -> listingButton;
            case "profile" -> profileButton;
            default -> null;
        };
    }

    public void refreshButtonSelection() {
        // Force refresh of the current selection to update icons and styling
        if (currentlySelected != null) {
            VBox selected = currentlySelected;
            clearAllSelections();
            setSelectedButton(selected);
            System.out.println(localeService.getMessage("sidebar.debug.selection.refreshed", "Button selection refreshed"));
        }
    }
}
