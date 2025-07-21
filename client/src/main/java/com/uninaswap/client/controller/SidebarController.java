package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class SidebarController {
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

    // Add FXML references to the button containers
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

    @FXML
    private javafx.scene.control.Label statusLabel;

    private final NavigationService navigationService;
    private final LocaleService localeService = LocaleService.getInstance();
    private final UserSessionService userSessionService = UserSessionService.getInstance();

    private MainController mainController;

    // Track currently selected button
    private VBox currentlySelected;

    // Add these fields to track icon references
    @FXML
    private ImageView offersIcon;

    public SidebarController() {
        this.navigationService = NavigationService.getInstance();
    }

    @FXML
    public void initialize() {
        // Set home as initially selected
        setSelectedButton(homeButton);
    }

    private void setSelectedButton(VBox selectedButton) {
        // Remove selected class from all buttons and reset icons
        clearAllSelections();
        
        // Add selected class to the new button and set white icon
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("selected");
            currentlySelected = selectedButton;
            setWhiteIcon(selectedButton);
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
    }

    private void setWhiteIcon(VBox button) {
        ImageView iconView = getIconFromButton(button);
        String whiteIconPath = getWhiteIconPath(button);
        
        if (iconView != null && whiteIconPath != null) {
            try {
                Image whiteIcon = new Image(getClass().getResourceAsStream(whiteIconPath));
                iconView.setImage(whiteIcon);
            } catch (Exception e) {
                System.err.println("Could not load white icon: " + whiteIconPath + " - " + e.getMessage());
            }
        }
    }

    private void setNormalIcon(VBox button, ImageView iconView, String normalIconPath) {
        if (iconView != null && normalIconPath != null) {
            try {
                Image normalIcon = new Image(getClass().getResourceAsStream(normalIconPath));
                iconView.setImage(normalIcon);
            } catch (Exception e) {
                System.err.println("Could not load normal icon: " + normalIconPath + " - " + e.getMessage());
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

    @FXML
    public void showProfile(MouseEvent event) {
        setSelectedButton(profileButton);
        try {
            Parent profileView = navigationService.loadProfileView(userSessionService.getUserViewModel());
            mainController.setContent(profileView);
        } catch (IOException e) {
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
        } catch (java.io.IOException e) {
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
        } catch (java.io.IOException e) {
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
        } catch (java.io.IOException e) {
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
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(MouseEvent event) {
        // Settings might not need to be selected since it's accessed from user menu
        try {
            Parent settingsView = navigationService.loadSettingsView();
            mainController.setContent(settingsView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSupport(MouseEvent event) {
        try {
            Parent supportView = navigationService.loadSupportView();
            mainController.setContent(supportView);
        } catch (java.io.IOException e) {
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
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(MouseEvent event) {
        try {
            navigationService.logout();
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText(localeService.getMessage("dashboard.error.logout", e.getMessage()));
            }
        }
    }

    // Public method to allow external selection updates
    public void selectHomeButton() {
        setSelectedButton(homeButton);
    }

    public void selectOffersButton() {
        setSelectedButton(offersButton);
    }

    public void selectInventoryButton() {
        setSelectedButton(inventoryButton);
    }

    public void selectNotificationsButton() {
        setSelectedButton(notificationsButton);
    }

    public void selectAddListingButton() {
        setSelectedButton(listingButton);
    }

    public void selectProfileButton() {
        setSelectedButton(profileButton);
    }

    public void selectListingsButton() {
        setSelectedButton(listingButton);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
