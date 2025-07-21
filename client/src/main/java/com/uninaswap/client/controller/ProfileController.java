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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.uninaswap.client.util.AlertHelper;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.List;
import java.util.stream.Collectors;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.client.service.ProfileService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.ProfileUpdateMessage;

public class ProfileController implements Refreshable {
    @FXML
    private Label profileTitleLabel;
    @FXML
    private Text usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextArea bioField;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label statusLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label bioLabel;
    @FXML
    private Button changeImageButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField stateProvinceField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField zipPostalCodeField;

    // Add for user listings section
    @FXML
    private VBox userListingsSection;
    @FXML
    private VBox userListingsList;
    @FXML
    private Label userListingsCountLabel;

    // Additional labels for localization
    @FXML
    private Label addressLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Label countryLabel;
    @FXML
    private Label zipLabel;
    @FXML
    private Label personalInfoLabel;
    @FXML
    private Label listingsTitleLabel;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final ProfileService profileService;
    private final ListingService listingService = ListingService.getInstance();

    private String tempProfileImagePath;
    private File tempSelectedImageFile;
    private UserViewModel user;

    // Add fields to track profile ownership
    private boolean isOwnProfile = false;
    private UserViewModel viewedUser;

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
                showStatus("profile.error.navigation", true);
            }
        }

        // Register message handler
        registerMessageHandler();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("profile.debug.initialized", "ProfileController initialized"));
    }

    @Override
    public void refreshUI() {
        // Update labels based on profile ownership
        if (isOwnProfile) {
            if (profileTitleLabel != null) {
                profileTitleLabel.setText(localeService.getMessage("profile.title", "User Profile"));
            }
            if (changeImageButton != null) {
                changeImageButton.setText(localeService.getMessage("button.change.image", "Change Image"));
            }
            if (saveButton != null) {
                saveButton.setText(localeService.getMessage("button.save", "Save"));
            }
            if (cancelButton != null) {
                cancelButton.setText(localeService.getMessage("button.cancel", "Cancel"));
            }
        } else {
            if (profileTitleLabel != null && viewedUser != null) {
                profileTitleLabel.setText(localeService.getMessage("profile.title.viewing", "{0}'s Profile")
                    .replace("{0}", viewedUser.getDisplayName()));
            }
        }

        // Update field labels
        if (firstNameLabel != null) {
            firstNameLabel.setText(localeService.getMessage("profile.first.name.label", "First Name"));
        }
        if (lastNameLabel != null) {
            lastNameLabel.setText(localeService.getMessage("profile.last.name.label", "Last Name"));
        }
        if (emailLabel != null) {
            emailLabel.setText(localeService.getMessage("profile.email.label", "Email"));
        }
        if (bioLabel != null) {
            bioLabel.setText(localeService.getMessage("profile.biography.label", "Biography"));
        }
        if (addressLabel != null) {
            addressLabel.setText(localeService.getMessage("profile.address.label", "Address"));
        }
        if (cityLabel != null) {
            cityLabel.setText(localeService.getMessage("profile.city.label", "City"));
        }
        if (stateLabel != null) {
            stateLabel.setText(localeService.getMessage("profile.state.label", "State/Province"));
        }
        if (countryLabel != null) {
            countryLabel.setText(localeService.getMessage("profile.country.label", "Country"));
        }
        if (zipLabel != null) {
            zipLabel.setText(localeService.getMessage("profile.zip.label", "ZIP/Postal Code"));
        }
        if (personalInfoLabel != null) {
            personalInfoLabel.setText(localeService.getMessage("profile.personal.info", "Personal Information"));
        }
        if (listingsTitleLabel != null) {
            listingsTitleLabel.setText(localeService.getMessage("profile.listings.title", "User Listings"));
        }

        // Update prompt texts
        if (firstNameField != null) {
            firstNameField.setPromptText(localeService.getMessage("profile.first.name.prompt", "Enter your first name"));
        }
        if (lastNameField != null) {
            lastNameField.setPromptText(localeService.getMessage("profile.last.name.prompt", "Enter your last name"));
        }
        if (emailField != null) {
            emailField.setPromptText(localeService.getMessage("profile.email.prompt", "Enter your email address"));
        }
        if (bioField != null) {
            bioField.setPromptText(localeService.getMessage("profile.biography.prompt", "Tell others about yourself..."));
        }
        if (addressField != null) {
            addressField.setPromptText(localeService.getMessage("profile.address.prompt", "Enter your address"));
        }
        if (cityField != null) {
            cityField.setPromptText(localeService.getMessage("profile.city.prompt", "Enter your city"));
        }
        if (stateProvinceField != null) {
            stateProvinceField.setPromptText(localeService.getMessage("profile.state.prompt", "Enter your state/province"));
        }
        if (countryField != null) {
            countryField.setPromptText(localeService.getMessage("profile.country.prompt", "Enter your country"));
        }
        if (zipPostalCodeField != null) {
            zipPostalCodeField.setPromptText(localeService.getMessage("profile.zip.prompt", "Enter your ZIP/postal code"));
        }

        // Refresh listings section if needed
        if (userListingsCountLabel != null && viewedUser != null) {
            loadUserListings();
        }
    }

    // Update the loadProfile method
    public void loadProfile(UserViewModel user) {
        this.viewedUser = user;
        this.user = user;

        // Check if this is the current user's own profile
        UserViewModel currentUser = sessionService.getUserViewModel();
        this.isOwnProfile = currentUser != null && user.getId().equals(currentUser.getId());

        loadUserProfile();
        loadUserListings();
        setupProfileVisibility();
        
        // Refresh UI after loading profile data
        refreshUI();
        
        System.out.println(localeService.getMessage("profile.debug.profile.loaded", "Profile loaded for user: {0}")
            .replace("{0}", user.getDisplayName()));
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
                    System.out.println(localeService.getMessage("profile.debug.save.success", "Profile update successful"));
                } else {
                    String errorMessage = response.getMessage() != null ? response.getMessage() : "profile.save.error";
                    showStatus(errorMessage, true);
                    System.err.println(localeService.getMessage("profile.debug.save.error", "Profile update failed: {0}")
                        .replace("{0}", response.getMessage()));
                }
            }
        });
    }

    private void loadUserProfile() {
        if (usernameField != null) {
            usernameField.setText(viewedUser.getUsername());
        }

        if (emailField != null) {
            emailField.setText(viewedUser.getEmail());
            emailField.setEditable(isOwnProfile);
        }

        if (firstNameField != null) {
            firstNameField.setText(viewedUser.getFirstName());
            firstNameField.setEditable(isOwnProfile);
        }

        if (lastNameField != null) {
            lastNameField.setText(viewedUser.getLastName());
            lastNameField.setEditable(isOwnProfile);
        }

        if (bioField != null) {
            bioField.setText(viewedUser.getBio());
            bioField.setEditable(isOwnProfile);
        }

        // Load address fields (only for own profile)
        if (isOwnProfile) {
            if (addressField != null) {
                addressField.setText(viewedUser.getAddress());
            }
            if (cityField != null) {
                cityField.setText(viewedUser.getCity());
            }
            if (stateProvinceField != null) {
                stateProvinceField.setText(viewedUser.getAddress()); // You might need a separate state field in UserViewModel
            }
            if (countryField != null) {
                countryField.setText(viewedUser.getCountry());
            }
            if (zipPostalCodeField != null) {
                zipPostalCodeField.setText(viewedUser.getPhoneNumber()); // You might need a separate zip field in UserViewModel
            }
        }

        // Set profile image
        String imagePath = viewedUser.getProfileImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageService.getInstance().fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            profileImageView.setImage(image);
                            tempProfileImagePath = imagePath;
                            System.out.println(localeService.getMessage("profile.debug.image.loaded", "Profile image loaded successfully"));
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            // If image loading fails, use default
                            profileImageView
                                    .setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
                            showStatus("profile.image.error.load", true);
                            System.err.println(localeService.getMessage("profile.debug.image.error", "Error loading profile image: {0}")
                                .replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        }
    }

    private void setupProfileVisibility() {
        // Show/hide address fields based on profile ownership
        if (addressField != null) {
            addressField.setVisible(isOwnProfile);
            addressField.setManaged(isOwnProfile);
        }
        if (cityField != null) {
            cityField.setVisible(isOwnProfile);
            cityField.setManaged(isOwnProfile);
        }
        if (stateProvinceField != null) {
            stateProvinceField.setVisible(isOwnProfile);
            stateProvinceField.setManaged(isOwnProfile);
        }
        if (countryField != null) {
            countryField.setVisible(isOwnProfile);
            countryField.setManaged(isOwnProfile);
        }
        if (zipPostalCodeField != null) {
            zipPostalCodeField.setVisible(isOwnProfile);
            zipPostalCodeField.setManaged(isOwnProfile);
        }

        // Show/hide change image button
        if (changeImageButton != null) {
            changeImageButton.setVisible(isOwnProfile);
            changeImageButton.setManaged(isOwnProfile);
        }

        // Show/hide save/cancel buttons
        if (saveButton != null) {
            saveButton.setVisible(isOwnProfile);
            saveButton.setManaged(isOwnProfile);
        }
        if (cancelButton != null) {
            cancelButton.setVisible(isOwnProfile);
            cancelButton.setManaged(isOwnProfile);
        }
    }

    private void loadUserListings() {
        if (userListingsList == null) return;

        userListingsList.getChildren().clear();

        // Load listings for the viewed user
        listingService.getUserListings(viewedUser.getId())
                .thenAccept(listings -> Platform.runLater(() -> {
                    // Convert DTOs to ViewModels
                    List<ListingViewModel> listingViewModels = listings.stream()
                            .map(ViewModelMapper.getInstance()::toViewModel)
                            .collect(Collectors.toList());
                    
                    userListingsCountLabel.setText(String.valueOf(listingViewModels.size()));

                    if (listingViewModels.isEmpty()) {
                        Label noListingsLabel = new Label(
                                isOwnProfile ?
                                        localeService.getMessage("profile.listings.empty.own", "You haven't created any listings yet.") :
                                        localeService.getMessage("profile.listings.empty.other", "This user hasn't created any listings yet.")
                        );
                        noListingsLabel.getStyleClass().add("placeholder-subtitle");
                        userListingsList.getChildren().add(noListingsLabel);
                    } else {
                        // Show only first few listings with "View All" button
                        int maxListings = Math.min(5, listingViewModels.size());
                        for (int i = 0; i < maxListings; i++) {
                            VBox listingItem = createListingPreviewItem(listingViewModels.get(i));
                            userListingsList.getChildren().add(listingItem);
                        }

                        if (listingViewModels.size() > maxListings) {
                            Button viewAllButton = new Button(
                                    localeService.getMessage("profile.listings.view.all", "View All ({0})")
                                        .replace("{0}", String.valueOf(listingViewModels.size()))
                            );
                            viewAllButton.getStyleClass().add("secondary-button");
                            viewAllButton.setOnAction(e -> handleViewAllListings());
                            userListingsList.getChildren().add(viewAllButton);
                        }
                    }
                    
                    System.out.println(localeService.getMessage("profile.debug.listings.loaded", "Loaded {0} listings for user")
                        .replace("{0}", String.valueOf(listingViewModels.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Label errorLabel = new Label(
                                localeService.getMessage("profile.listings.error", "Error loading listings.")
                        );
                        errorLabel.getStyleClass().add("error-message");
                        userListingsList.getChildren().add(errorLabel);
                        System.err.println(localeService.getMessage("profile.debug.listings.error", "Error loading user listings: {0}")
                            .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    private VBox createListingPreviewItem(ListingViewModel listing) {
        VBox itemContainer = new VBox(8);
        itemContainer.getStyleClass().add("listing-preview-item");

        // Title and status row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(listing.getTitle());
        titleLabel.getStyleClass().add("listing-preview-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        Label statusLabel = new Label(listing.getStatus().getDisplayName());
        statusLabel.getStyleClass().addAll("status-badge", "status-" + listing.getStatus().toString().toLowerCase());

        headerRow.getChildren().addAll(titleLabel, statusLabel);

        // Type and date row
        HBox detailsRow = new HBox(15);
        detailsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label typeLabel = new Label(listing.getListingTypeValue());
        typeLabel.getStyleClass().add("listing-type-label");

        Label dateLabel = new Label(
                listing.getCreatedAt() != null ?
                        listing.getCreatedAt().toLocalDate().toString() :
                        ""
        );
        dateLabel.getStyleClass().add("listing-date-label");

        detailsRow.getChildren().addAll(typeLabel, dateLabel);

        itemContainer.getChildren().addAll(headerRow, detailsRow);

        // Make clickable
        itemContainer.setOnMouseClicked(e -> {
            try {
                navigationService.navigateToListingDetails(listing);
                System.out.println(localeService.getMessage("profile.debug.listing.clicked", "Navigating to listing details: {0}")
                    .replace("{0}", listing.getTitle()));
            } catch (Exception ex) {
                System.err.println(localeService.getMessage("profile.debug.navigation.error", "Error navigating to listing: {0}")
                    .replace("{0}", ex.getMessage()));
            }
        });

        return itemContainer;
    }

    private void handleViewAllListings() {
        if (isOwnProfile) {
            // Navigate to user's own listings page
            try {
                navigationService.navigateToListingsView();
                System.out.println(localeService.getMessage("profile.debug.navigate.own.listings", "Navigating to own listings view"));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("profile.debug.navigation.error", "Error navigating to listing: {0}")
                    .replace("{0}", e.getMessage()));
            }
        } else {
            // Show user's public listings in a dialog or new view
            showUserListingsDialog(viewedUser);
        }
    }

    private void showUserListingsDialog(UserViewModel user) {
        AlertHelper.showInformationAlert(
                localeService.getMessage("profile.user.listings.dialog.title", "User Listings"),
                localeService.getMessage("profile.user.listings.dialog.header", "{0}'s Listings")
                    .replace("{0}", user.getDisplayName()),
                localeService.getMessage("profile.user.listings.dialog.content", 
                    "Feature coming soon: View all user listings in a dedicated dialog.")
        );
        System.out.println(localeService.getMessage("profile.debug.user.listings.dialog", "Showing user listings dialog for: {0}")
            .replace("{0}", user.getDisplayName()));
    }

    @FXML
    public void handleChangeImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(localeService.getMessage("profile.image.chooser.title", "Select Profile Picture"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                    localeService.getMessage("profile.image.filter.name", "Image Files"), 
                    "*.png", "*.jpg", "*.jpeg"));

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
                    System.out.println(localeService.getMessage("profile.debug.image.selected", "Profile image selected and cropped"));
                });
            } catch (Exception e) {
                showStatus("profile.image.error.load", true);
                System.err.println(localeService.getMessage("profile.debug.image.error", "Error loading profile image: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    @FXML
    private void showAnalytics(ActionEvent event) {
        try {
            // Navigate to the analytics view
            navigationService.navigateToAnalyticsView();
            System.out.println(localeService.getMessage("profile.debug.navigate.analytics", "Navigating to analytics view"));
        } catch (IOException e) {
            showStatus("profile.error.navigation.analytics", true);
            System.err.println(localeService.getMessage("profile.debug.navigation.error", "Error navigating to listing: {0}")
                .replace("{0}", e.getMessage()));
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
            cropperStage.setTitle(localeService.getMessage("profile.image.cropper.title", "Crop Profile Image"));
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
            System.out.println(localeService.getMessage("profile.debug.cropper.opened", "Image cropper dialog opened"));
        } catch (IOException e) {
            showStatus("profile.error.image.cropper", true);
            System.err.println(localeService.getMessage("profile.debug.cropper.error", "Error showing image cropper: {0}")
                .replace("{0}", e.getMessage()));
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
            System.out.println(localeService.getMessage("profile.debug.image.converted", "Image converted to temporary file"));

            return tempFile;
        } catch (IOException e) {
            System.err.println(localeService.getMessage("profile.debug.image.convert.error", "Error converting image to file: {0}")
                .replace("{0}", e.getMessage()));
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
        System.out.println(localeService.getMessage("profile.debug.save.started", "Profile save process started"));

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
                        System.out.println(localeService.getMessage("profile.debug.image.uploaded", "Profile image uploaded successfully"));
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showStatus("profile.error.image.upload", true);
                            saveButton.setDisable(false);
                            System.err.println(localeService.getMessage("profile.debug.image.upload.error", "Error uploading image: {0}")
                                .replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        } else {
            // No new image, just save the profile
            saveProfileWithImage();
        }
    }

    // Update the saveProfileWithImage method to include address fields
    private void saveProfileWithImage() {
        // Update session with form values
        if (isOwnProfile) {
            user.setFirstName(firstNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setBio(bioField.getText());
            user.setProfileImagePath(tempProfileImagePath);

            // Update address fields
            user.setAddress(addressField.getText());
            user.setCity(cityField.getText());
            user.setCountry(countryField.getText());
            // Note: You might need to add stateProvince and zipPostalCode fields to UserViewModel

            // Create user object for update
            UserDTO updatedUser = new UserDTO();
            updatedUser.setUsername(usernameField.getText());
            updatedUser.setEmail(emailField.getText());
            updatedUser.setFirstName(firstNameField.getText());
            updatedUser.setLastName(lastNameField.getText());
            updatedUser.setBio(bioField.getText());
            updatedUser.setProfileImagePath(tempProfileImagePath);
            updatedUser.setAddress(addressField.getText());
            updatedUser.setCity(cityField.getText());
            updatedUser.setCountry(countryField.getText());

            // Send profile update request
            profileService.updateProfile(updatedUser)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showStatus("profile.error.connection", true);
                            System.err.println(localeService.getMessage("profile.debug.update.error", "Error sending profile update: {0}")
                                .replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        }
    }

    private void notifyProfileImageChange(String newImagePath) {
        System.out.println(localeService.getMessage("profile.debug.image.change.event", "Publishing profile image change event: {0}")
            .replace("{0}", newImagePath));
        EventBusService.getInstance().publishEvent(EventTypes.PROFILE_IMAGE_CHANGED, newImagePath);
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            navigationService.navigateToMainDashboard(usernameField);
            System.out.println(localeService.getMessage("profile.debug.cancel", "Profile editing cancelled, navigating to dashboard"));
        } catch (IOException e) {
            showStatus("profile.error.navigation", true);
            System.err.println(localeService.getMessage("profile.debug.navigation.error", "Error navigating to listing: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void showStatus(String messageKey, boolean isError) {
        String message = localeService.getMessage(messageKey, messageKey);
        statusLabel.setText(message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(isError ? "error-message" : "success-message");
        
        System.out.println(localeService.getMessage("profile.debug.status.shown", "Status message shown: {0} (error: {1})")
            .replace("{0}", message)
            .replace("{1}", String.valueOf(isError)));
    }
}