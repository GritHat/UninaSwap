package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
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
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.client.service.ProfileService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.ListingService;
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
    @FXML
    private VBox userListingsSection;
    @FXML
    private VBox userListingsList;
    @FXML
    private Label userListingsCountLabel;
    @FXML
    private Label ratingReviewsLabel;
    @FXML
    private Button analyticsButton;
    @FXML
    private Button reportUserButton;
    @FXML
    private HBox viewAllButtonContainer;
    @FXML
    private Button viewAllListingsButton;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final ProfileService profileService;
    private final ListingService listingService = ListingService.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    private String tempProfileImagePath;
    private File tempSelectedImageFile;
    private UserViewModel user;
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
        if (!sessionService.isLoggedIn()) {
            try {
                navigationService.navigateToLogin(usernameField);
                return;
            } catch (IOException e) {
                showStatus("error.navigation", true);
            }
        }
        registerMessageHandler();
    }
    public void loadProfile(UserViewModel user) {
        this.viewedUser = user;
        this.user = user;
        UserViewModel currentUser = sessionService.getUserViewModel();
        this.isOwnProfile = currentUser != null && user.getId().equals(currentUser.getId());

        loadUserProfile();
        loadUserListings();
        setupProfileVisibility();
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
                    showStatus(response.getMessage() != null ? response.getMessage() : "profile.save.error", true);
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
        if (isOwnProfile) {
            if (addressField != null) {
                addressField.setText(viewedUser.getAddress());
            }
            if (cityField != null) {
                cityField.setText(viewedUser.getCity());
            }
            if (stateProvinceField != null) {
                stateProvinceField.setText(viewedUser.getStateProvince()); // You might need a separate state field in UserViewModel
            }
            if (countryField != null) {
                countryField.setText(viewedUser.getCountry());
            }
            if (zipPostalCodeField != null) {
                zipPostalCodeField.setText(viewedUser.getZipPostalCode()); // You might need a separate zip field in UserViewModel
            }
        }
        String imagePath = viewedUser.getProfileImagePath();
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
                            profileImageView
                                    .setImage(new Image(getClass().getResourceAsStream("/images/default_profile.png")));
                            showStatus("profile.image.error.load", true);
                            System.err.println("Error loading image: " + ex.getMessage());
                        });
                        return null;
                    });
        }
    }

    private void setupProfileVisibility() {
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
        if (changeImageButton != null) {
            changeImageButton.setVisible(isOwnProfile);
            changeImageButton.setManaged(isOwnProfile);
        }
        if (saveButton != null) {
            saveButton.setVisible(isOwnProfile);
            saveButton.setManaged(isOwnProfile);
        }
        if (cancelButton != null) {
            cancelButton.setVisible(isOwnProfile);
            cancelButton.setManaged(isOwnProfile);
        }
        if (analyticsButton != null) {
            analyticsButton.setVisible(isOwnProfile);
            analyticsButton.setManaged(isOwnProfile);
        }
        if (reportUserButton != null) {
            reportUserButton.setVisible(!isOwnProfile);
            reportUserButton.setManaged(!isOwnProfile);
        }
        updateRatingReviewsLabel();
    }
    private void updateRatingReviewsLabel() {
        if (ratingReviewsLabel != null && viewedUser != null) {
            double rating = viewedUser.getRating();
            int reviewCount = viewedUser.getReviewCount();
            
            if (reviewCount > 0) {
                String ratingText = String.format("⭐ %.2f/5 (%d %s)", 
                    rating, 
                    reviewCount, 
                    reviewCount == 1 ? 
                        localeService.getMessage("profile.review.singular", "review") : 
                        localeService.getMessage("profile.reviews.plural", "reviews"));
                ratingReviewsLabel.setText(ratingText);
            } else {
                ratingReviewsLabel.setText(localeService.getMessage("profile.no.reviews", "No reviews yet"));
            }
        }
    }

    private void loadUserListings() {
        if (userListingsList == null) return;

        userListingsList.getChildren().clear();
        listingService.getUserListings(viewedUser.getId())
                .thenAccept(listings -> Platform.runLater(() -> {
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
                        if (viewAllButtonContainer != null) {
                            viewAllButtonContainer.setVisible(false);
                            viewAllButtonContainer.setManaged(false);
                        }
                    } else {
                        for (ListingViewModel listing : listingViewModels) {
                            VBox listingItem = createListingPreviewItem(listing);
                            userListingsList.getChildren().add(listingItem);
                        }
                        if (listingViewModels.size() > 10 && isOwnProfile) {
                            viewAllButtonContainer.setVisible(true);
                            viewAllButtonContainer.setManaged(true);
                        } else {
                            viewAllButtonContainer.setVisible(false);
                            viewAllButtonContainer.setManaged(false);
                        }
                    }
                }))
                .exceptionally(_ -> {
                    Platform.runLater(() -> {
                        Label errorLabel = new Label(
                                localeService.getMessage("profile.listings.error", "Error loading listings.")
                        );
                        errorLabel.getStyleClass().add("error-message");
                        userListingsList.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    private VBox createListingPreviewItem(ListingViewModel listing) {
        VBox itemContainer = new VBox(5);
        itemContainer.getStyleClass().add("profile-listing-item");
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("listing-item-header-row");
        ImageView thumbnail = new ImageView();
        thumbnail.setFitHeight(40);
        thumbnail.setFitWidth(40);
        thumbnail.setPreserveRatio(true);
        thumbnail.getStyleClass().add("listing-thumbnail");
        loadListingThumbnail(thumbnail, listing);
        VBox textContainer = new VBox(2);
        textContainer.setMaxWidth(250);

        Label title = new Label(listing.getTitle());
        title.setMaxWidth(250);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.getStyleClass().add("listing-item-title");
        HBox infoRow = new HBox(10);
        infoRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label typeLabel = new Label(listing.getListingTypeValue());
        typeLabel.getStyleClass().add("listing-type-badge");

        Label statusLabel = new Label(listing.getStatus().getDisplayName());
        statusLabel.getStyleClass().addAll("status-badge", 
            "status-" + listing.getStatus().toString().toLowerCase());

        infoRow.getChildren().addAll(typeLabel, statusLabel);

        textContainer.getChildren().addAll(title, infoRow);
        Label dateLabel = new Label();
        if (listing.getCreatedAt() != null) {
            dateLabel.setText(listing.getCreatedAt().toLocalDate().toString());
        }
        dateLabel.getStyleClass().add("listing-date-label");

        headerRow.getChildren().addAll(thumbnail, textContainer);
        itemContainer.setOnMouseClicked(_ -> {
            try {
                navigationService.navigateToListingDetails(listing);
            } catch (Exception ex) {
                System.err.println("Error navigating to listing: " + ex.getMessage());
            }
        });

        // Add hover effect
        itemContainer.setOnMouseEntered(_ -> itemContainer.getStyleClass().add("listing-item-hover"));
        itemContainer.setOnMouseExited(_ -> itemContainer.getStyleClass().remove("listing-item-hover"));

        itemContainer.getChildren().addAll(headerRow, dateLabel);
        return itemContainer;
    }

    private void loadListingThumbnail(ImageView thumbnail, ListingViewModel listing) {
        String imagePath = getFirstListingImagePath(listing);
        
        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
            ImageService.getInstance().fetchImage(imagePath)
                .thenAccept(image -> Platform.runLater(() -> thumbnail.setImage(image)))
                .exceptionally(_ -> {
                    Platform.runLater(() -> setDefaultListingThumbnail(thumbnail));
                    return null;
                });
        } else {
            setDefaultListingThumbnail(thumbnail);
        }
    }

    private String getFirstListingImagePath(ListingViewModel listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            for (ListingItemViewModel item : listing.getItems()) {
                if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                    return item.getImagePath();
                }
            }
        }
        return null;
    }

    private void setDefaultListingThumbnail(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/listings.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default listing thumbnail: " + e.getMessage());
        }
    }

    @FXML
    public void handleChangeImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image sourceImage = new Image(selectedFile.toURI().toString());
                showImageCropper(sourceImage, croppedImage -> {
                    profileImageView.setImage(croppedImage);
                    tempSelectedImageFile = convertImageToTempFile(croppedImage);
                });
            } catch (Exception e) {
                showStatus("profile.image.error.load", true);
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showAnalytics(ActionEvent event) {
        try {
            navigationService.navigateToAnalyticsView();
        } catch (IOException e) {
            System.err.println("Error navigating to analytics: " + e.getMessage());
            showStatus("navigation.error.load.analytics", true);
        }
    }

    /**
     * Shows the image cropper dialog
     * 
     * @param sourceImage The image to crop
     * @param cropCallback Callback to handle the cropped image
     */
    private void showImageCropper(Image sourceImage, Consumer<Image> cropCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImageCropperView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            Parent cropperView = loader.load();
            Stage cropperStage = new Stage();
            cropperStage.initModality(Modality.APPLICATION_MODAL);
            cropperStage.initOwner(profileImageView.getScene().getWindow());
            Scene scene = new Scene(cropperView);
            scene.getStylesheets().add(getClass().getResource("/css/cropper.css").toExternalForm());
            cropperStage.setScene(scene);
            ImageCropperController controller = loader.getController();
            controller.setImage(sourceImage);
            controller.setCropCallback(cropCallback);
            cropperStage.showAndWait();
        } catch (IOException e) {
            showStatus("profile.error.image.cropper", true);
            System.err.println("Error showing image cropper: " + e.getMessage());
        }
    }

    /**
     * Converts a JavaFX Image to a temporary file for upload
     * 
     * @param image The JavaFX Image to convert
     * @return A temporary file containing the image data, or null if conversion failed
     */
    private File convertImageToTempFile(Image image) {
        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            PixelReader pixelReader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(x, y);
                    int argb = convertColorToARGB(color);
                    bufferedImage.setRGB(x, y, argb);
                }
            }
            File tempFile = File.createTempFile("profile_", ".png");
            tempFile.deleteOnExit();
            javax.imageio.ImageIO.write(bufferedImage, "png", tempFile);

            return tempFile;
        } catch (IOException e) {
            System.err.println("Error converting image to file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert JavaFX Color to ARGB int value for BufferedImage
     * 
     * @param color The JavaFX Color to convert
     * @return ARGB int value
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
        Button saveButton = (Button) event.getSource();
        saveButton.setDisable(true);
        showStatus("profile.save.inprogress", false);
        if (tempSelectedImageFile != null) {
            ImageService imageService = ImageService.getInstance();
            imageService.uploadImageViaHttp(tempSelectedImageFile)
                    .thenAccept(imageId -> {
                        tempProfileImagePath = imageId;
                        saveProfileWithImage();
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
            saveProfileWithImage();
        }
    }

    private void saveProfileWithImage() {
        if (isOwnProfile) {
            user.setFirstName(firstNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setBio(bioField.getText());
            user.setProfileImagePath(tempProfileImagePath);
            user.setAddress(addressField.getText());
            user.setCity(cityField.getText());
            user.setCountry(countryField.getText());
            user.setStateProvince(stateProvinceField.getText());
            user.setZipPostalCode(zipPostalCodeField.getText());

            profileService.updateProfile(viewModelMapper.toDTO(user))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showStatus("profile.error.connection", true);
                            System.err.println("Error sending profile update: " + ex.getMessage());
                        });
                        return null;
                    });
        }
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

    @Override
    public void refreshUI() {
        if (isOwnProfile) {
            profileTitleLabel.setText(localeService.getMessage("profile.title"));
            changeImageButton.setText(localeService.getMessage("button.change"));
            saveButton.setText(localeService.getMessage("button.save"));
            cancelButton.setText(localeService.getMessage("button.cancel"));
        } else {
            profileTitleLabel.setText(viewedUser.getDisplayName() + "'s " + localeService.getMessage("profile.title"));
        }

        if (userListingsCountLabel != null) {
            loadUserListings();
        }
    }
    
    @FXML
    private void handleReportUser() {
        if (viewedUser != null) {
            try {
                //navigationService.openReportDialog(viewedUser);
            } catch (Exception e) {
                System.err.println("Error opening report dialog: " + e.getMessage());
                AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.report.user", "Report Error"),
                    "Failed to open report dialog: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewAllListings() {
        if (viewedUser == null) {
            System.err.println("No user available to view listings for");
            return;
        }
        try {
            navigationService.navigateToHomeViewAndSearchListingsByUserId(viewedUser.getId());
        } catch (Exception e) {
            System.err.println("Error navigating to view all listings: " + e.getMessage());
            AlertHelper.showErrorAlert(
                localeService.getMessage("error.title", "Error"),
                localeService.getMessage("error.navigation", "Navigation Error"),
                "Failed to view all listings: " + e.getMessage());
        }
    }
}