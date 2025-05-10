package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.MessageService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.ProfileService;
import com.uninaswap.common.model.User;
import com.uninaswap.common.message.ProfileUpdateMessage;

public class ProfileController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextArea bioField;
    @FXML private ImageView profileImageView;
    @FXML private Label statusLabel;
    
    private final NavigationService navigationService;
    private final MessageService messageService;
    private final UserSessionService sessionService;
    private final ProfileService profileService;
    
    private String tempProfileImagePath;
    
    public ProfileController() {
        this.navigationService = NavigationService.getInstance();
        this.messageService = MessageService.getInstance();
        this.sessionService = UserSessionService.getInstance();
        this.profileService = new ProfileService();
    }
    
    @FXML
    public void initialize() {
        // Verify user is logged in
        if (!sessionService.isLoggedIn()) {
            try {
                navigationService.navigateToLogin(usernameField);
                return;
            } catch (IOException e) {
                showStatus("error.navigation", true);
            }
        }
        
        // Register message handler
        registerMessageHandler();
        
        // Load user information
        loadUserProfile();
    }
    
    /**
     * Registers this controller's message handler with the ProfileService.
     * Called during initialization.
     */
    public void registerMessageHandler() {
        profileService.setUpdateResponseHandler(this::handleProfileResponse);
    }
    
    /**
     * Handle profile update responses from the server
     */
    private void handleProfileResponse(ProfileUpdateMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == ProfileUpdateMessage.Type.UPDATE_RESPONSE) {
                if (response.isSuccess()) {
                    showStatus("profile.save.success", false);
                } else {
                    showStatus(response.getMessage() != null ? 
                        response.getMessage() : "profile.save.error", true);
                }
            }
        });
    }
    
    private void loadUserProfile() {
        // Set non-editable fields
        usernameField.setText(sessionService.getUsername());
        emailField.setText(sessionService.getEmail());
        
        // Set editable fields
        firstNameField.setText(sessionService.get("firstName"));
        lastNameField.setText(sessionService.get("lastName"));
        bioField.setText(sessionService.get("bio"));
        
        // Set profile image
        String imagePath = sessionService.get("profileImagePath");
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(imagePath);
                profileImageView.setImage(image);
                tempProfileImagePath = imagePath;
            } catch (Exception e) {
                // If image loading fails, use default
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
            }
        }
    }
    
    @FXML
    public void handleChangeImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                tempProfileImagePath = selectedFile.toURI().toString();
            } catch (Exception e) {
                showStatus("profile.image.error.load", true);
            }
        }
    }
    
    @FXML
    public void handleSave(ActionEvent event) {
        // Update session with form values
        sessionService.put("firstName", firstNameField.getText());
        sessionService.put("lastName", lastNameField.getText());
        sessionService.put("bio", bioField.getText());
        sessionService.put("profileImagePath", tempProfileImagePath);
        
        // Create user object for update
        User updatedUser = new User();
        updatedUser.setUsername(usernameField.getText());
        updatedUser.setEmail(emailField.getText());
        updatedUser.setFirstName(firstNameField.getText());
        updatedUser.setLastName(lastNameField.getText());
        updatedUser.setBio(bioField.getText());
        updatedUser.setProfileImagePath(tempProfileImagePath);
        
        // Disable save button to prevent multiple submissions
        Button saveButton = (Button) event.getSource();
        saveButton.setDisable(true);
        
        // Show "saving" status
        showStatus("profile.save.inprogress", false);
        
        // Send profile update request
        profileService.updateProfile(updatedUser)
            .exceptionally(ex -> {
                // Handle errors in sending the message
                Platform.runLater(() -> {
                    showStatus("profile.error.connection", true);
                    System.err.println("Error sending profile update: " + ex.getMessage());
                    saveButton.setDisable(false);
                });
                return null;
            });
    }
    
    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            navigationService.navigateToMainDashboard(usernameField);
        } catch (IOException e) {
            showStatus("error.navigation", true);
        }
    }
    
    private void showStatus(String messageKey, boolean isError) {
        statusLabel.setText(messageService.getMessage(messageKey));
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(isError ? "error-message" : "success-message");
    }
}