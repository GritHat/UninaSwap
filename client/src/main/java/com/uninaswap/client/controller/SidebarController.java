package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.ObjectInputFilter.Status;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SidebarController {
    @FXML
    private ImageView ProfileImageIcon;
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView inventoryIcon;
    @FXML
    private ImageView allertsIcon;
    @FXML
    private ImageView addListingIcon;
    @FXML
    private ImageView settingsIcon;
    @FXML
    private ImageView supportIcon;
    @FXML
    private ImageView logoutIcon;

    @FXML
    private javafx.scene.control.Label statusLabel; // Add this if you have a Label in your FXML for status messages

    private final ImageService imageService;
    private final NavigationService navigationService;
    private final UserSessionService sessionService;

    private final com.uninaswap.client.service.LocaleService localeService = com.uninaswap.client.service.LocaleService
            .getInstance();

    private MainController mainController;

    public SidebarController() {
        this.imageService = ImageService.getInstance();
        this.navigationService = NavigationService.getInstance();
        this.sessionService = UserSessionService.getInstance();

    }

    @FXML
    public void initialize() {
        // Verificare che ProfileImageIcon sia stato iniettato correttamente
        if (ProfileImageIcon == null) {
            System.err.println("WARNING: ProfileImageIcon not injected from FXML");
            return;
        }

        // Initialization code if needed
        String profileImagePath = sessionService.getUser().getProfileImagePath();
        loadProfileImageInHeader(profileImagePath);

        // Subscribe to profile image change events
        EventBusService.getInstance().subscribe(EventTypes.PROFILE_IMAGE_CHANGED, data -> {
            if (data instanceof String) {
                updateProfileImage((String) data);
            }
        });
    }

    // onMouseClicked event
    @FXML
    public void showProfile(MouseEvent event) {
        try {
            Parent profileView = navigationService.loadProfileView();
            mainController.setContent(profileView);
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }

    @FXML
    public void showHome(MouseEvent event) {
        try {
            Parent homeView = navigationService.loadHomeView();
            mainController.setContent(homeView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void showInventory(MouseEvent event) {
        try {
            Parent inventoryView = navigationService.loadInventoryView();
            mainController.setContent(inventoryView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void addListing(MouseEvent event) {
        try {
            Parent addListingView = navigationService.loadListingCreationView();
            mainController.setContent(addListingView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void showAllerts(MouseEvent event) {
        try {
            Parent allertsView = navigationService.loadAllertsView();
            mainController.setContent(allertsView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void showSettings(MouseEvent event) {
        try {
            Parent settingsView = navigationService.loadSettings();
            mainController.setContent(settingsView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void showSupport(MouseEvent event) {
        try {
            Parent supportView = navigationService.loadSupport();
            mainController.setContent(supportView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void logout(MouseEvent event) {
        try {
            navigationService.logout();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }

    @FXML
    public void handleMouseMove(MouseEvent event) {
        // Get the source of the event (the ImageView that was hovered)
        ImageView source = (ImageView) event.getSource();

        // Apply a visual effect to indicate hover state
        source.setScaleX(1.2);
        source.setScaleY(1.2);
        source.setOpacity(0.8);
    }

    @FXML
    public void handleMouseExit(MouseEvent event) {
        // Reset the visual effect when mouse exits
        ImageView source = (ImageView) event.getSource();

        source.setScaleX(1.0);
        source.setScaleY(1.0);
        source.setOpacity(1.0);
    }

    /**
     * Sets a default profile image when no custom image is available
     */
    private void setDefaultProfileImage() {
        if (ProfileImageIcon == null)
            return;

        try {
            String imagePath = "/images/icons/user_profile.png";
            Image defaultImage = new Image(getClass().getResourceAsStream(imagePath));

            if (defaultImage.isError()) {
                System.err.println("Error loading default profile image: " + defaultImage.getException().getMessage());
                return;
            }

            ProfileImageIcon.setImage(defaultImage);

            // Apply circular clip
            Circle clip = new Circle(13, 13, 13); // Adattato alle dimensioni dell'icona 26x35
            ProfileImageIcon.setClip(clip);
        } catch (Exception e) {
            System.err.println("Exception setting default profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update profile image when profile is changed elsewhere
     */
    public void updateProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            loadProfileImageInHeader(imagePath);
        }
    }

    /**
     * Loads user profile image in the header
     */
    private void loadProfileImageInHeader(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultProfileImage();
            return;
        }

        try {
            imageService.fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            ProfileImageIcon.setImage(image);

                            // Apply circular clip to the image
                            Circle clip = new Circle(13, 13, 13); // Adattato alle dimensioni dell'icona
                            ProfileImageIcon.setClip(clip);
                        });
                    })
                    .exceptionally(ex -> {
                        // If loading fails, set default image
                        System.err.println("Failed to load profile image: " + ex.getMessage());
                        Platform.runLater(this::setDefaultProfileImage);
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Exception in loadProfileImageInHeader: " + e.getMessage());
            e.printStackTrace();
            setDefaultProfileImage();
        }

    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
