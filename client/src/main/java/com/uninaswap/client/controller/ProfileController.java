package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.PixelReader;


import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.ProfileService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.ProfileUpdateMessage;

public class ProfileController implements Refreshable{ 
    @FXML private Label profileTitleLabel;
    @FXML private Text usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextArea bioField;
    @FXML private ImageView profileImageView;
    @FXML private Label statusLabel;
    @FXML private Label emailLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label bioLabel;
    @FXML private Button changeImageButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final ProfileService profileService;
    
    private String tempProfileImagePath;
    private File tempSelectedImageFile;
    
    public ProfileController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
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
        if (usernameField != null) {
            usernameField.setText(sessionService.getUser().getUsername());
        }
        
        if (emailField != null) {
            emailField.setText(sessionService.getUser().getEmail());
        }

        if (firstNameField != null) {
            firstNameField.setText(sessionService.getUser().getFirstName());
        }
        
        if (lastNameField != null) {
            lastNameField.setText(sessionService.getUser().getLastName());
        }
        
        if (bioField != null) {
            bioField.setText(sessionService.getUser().getBio());
        }
        
        // Set profile image
        String imagePath = sessionService.getUser().getProfileImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageService.getInstance().fetchImage(imagePath)
                .thenAccept(image -> {
                    Platform.runLater(() -> {
                        profileImageView.setImage(image);
                        tempProfileImagePath = imagePath;
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // If image loading fails, use default
                        profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
                        showStatus("profile.image.error.load", true);
                        System.err.println("Error loading image: " + ex.getMessage());
                    });
                    return null;
                });
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
                // Load the source image
                Image sourceImage = new Image(selectedFile.toURI().toString());
                
                // Show the image cropper dialog
                showImageCropper(sourceImage, croppedImage -> {
                    // Update UI with the cropped image
                    profileImageView.setImage(croppedImage);
                    
                    // Convert the cropped image to a file for later upload
                    tempSelectedImageFile = convertImageToTempFile(croppedImage);
                });
            } catch (Exception e) {
                showStatus("profile.image.error.load", true);
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shows the image cropper dialog
     */
    private void showImageCropper(Image sourceImage, Consumer<Image> cropCallback) {
        try {
            // Load the cropper FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImageCropperView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            Parent cropperView = loader.load();
            
            // Create dialog
            Stage cropperStage = new Stage();
            //cropperStage.setTitle("Crop Profile Image");
            cropperStage.initModality(Modality.APPLICATION_MODAL);
            cropperStage.initOwner(profileImageView.getScene().getWindow());
            
            // Add CSS
            Scene scene = new Scene(cropperView);
            scene.getStylesheets().add(getClass().getResource("/css/cropper.css").toExternalForm());
            cropperStage.setScene(scene);
            
            // Set up the controller
            ImageCropperController controller = loader.getController();
            controller.setImage(sourceImage);
            controller.setCropCallback(cropCallback);
            
            // Show the cropper dialog
            cropperStage.showAndWait();
        } catch (IOException e) {
            showStatus("profile.error.image.cropper", true);
            System.err.println("Error showing image cropper: " + e.getMessage());
        }
    }
    
    /**
     * Converts a JavaFX Image to a temporary file for upload
     */
    private File convertImageToTempFile(Image image) {
        try {
            // Create a BufferedImage from the JavaFX Image
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            
            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            
            // Copy pixels
            PixelReader pixelReader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(x, y);
                    int argb = convertColorToARGB(color);
                    bufferedImage.setRGB(x, y, argb);
                }
            }
            
            // Create temp file
            File tempFile = File.createTempFile("profile_", ".png");
            tempFile.deleteOnExit();
            
            // Write to file
            javax.imageio.ImageIO.write(bufferedImage, "png", tempFile);
            
            return tempFile;
        } catch (IOException e) {
            System.err.println("Error converting image to file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert JavaFX Color to ARGB int value for BufferedImage
     */
    private int convertColorToARGB(javafx.scene.paint.Color color) {
        int a = (int) (color.getOpacity() * 255);
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    @FXML
    public void handleSave(ActionEvent event) {
        // Disable save button to prevent multiple submissions
        Button saveButton = (Button) event.getSource();
        saveButton.setDisable(true);
        
        // Show "saving" status
        showStatus("profile.save.inprogress", false);
        
        // If a new image was selected, upload it first using HTTP
        if (tempSelectedImageFile != null) {
            ImageService imageService = ImageService.getInstance();
            
            imageService.uploadImageViaHttp(tempSelectedImageFile)
                .thenAccept(imageId -> {
                    // Update the image path to the server-side path
                    tempProfileImagePath = imageId;
                    // Now save the profile with the new image ID
                    saveProfileWithImage();
                    
                    // Notify other parts of the application about the image change
                    notifyProfileImageChange(tempProfileImagePath);
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showStatus("profile.error.image.upload", true);
                        saveButton.setDisable(false);
                        System.err.println("Error uploading image: " + ex.getMessage());
                    });
                    return null;
                });
        } else {
            // No new image, just save the profile
            saveProfileWithImage();
        }
    }
    
    private void saveProfileWithImage() {
        // Update session with form values
        sessionService.getUser().setFirstName(firstNameField.getText());
        sessionService.getUser().setLastName(lastNameField.getText());
        sessionService.getUser().setBio(bioField.getText());
        sessionService.getUser().setProfileImagePath(tempProfileImagePath);

        // Create user object for update
        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername(usernameField.getText());
        updatedUser.setEmail(emailField.getText());
        updatedUser.setFirstName(firstNameField.getText());
        updatedUser.setLastName(lastNameField.getText());
        updatedUser.setBio(bioField.getText());
        updatedUser.setProfileImagePath(tempProfileImagePath);
        
        // Send profile update request
        profileService.updateProfile(updatedUser)
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showStatus("profile.error.connection", true);
                    System.err.println("Error sending profile update: " + ex.getMessage());
                });
                return null;
            });
    }
    
    private void notifyProfileImageChange(String newImagePath) {
        System.out.println("Publishing profile image change event: " + newImagePath);
        EventBusService.getInstance().publishEvent(EventTypes.PROFILE_IMAGE_CHANGED, newImagePath);
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
        statusLabel.setText(localeService.getMessage(messageKey));
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(isError ? "error-message" : "success-message");
    }

    public void refreshUI() {
        profileTitleLabel.setText(localeService.getMessage("profile.title"));
        usernameField.setText(localeService.getMessage("label.username"));
        emailLabel.setText(localeService.getMessage("label.email"));
        firstNameLabel.setText(localeService.getMessage("label.firstname"));
        lastNameLabel.setText(localeService.getMessage("label.lastname"));
        bioLabel.setText(localeService.getMessage("label.bio"));
        changeImageButton.setText(localeService.getMessage("button.change"));
        saveButton.setText(localeService.getMessage("button.save"));
        cancelButton.setText(localeService.getMessage("button.cancel"));
    }
}