package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ReviewService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.ReviewViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ReviewCreateController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label instructionsLabel;

    @FXML
    private Label reviewingUserLabel;

    @FXML
    private Label offerDetailsLabel;

    @FXML
    private HBox starsContainer;

    @FXML
    private Label scoreLabel;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Label characterCountLabel;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final ReviewService reviewService = ReviewService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    // Data
    private OfferViewModel currentOffer;
    private UserViewModel reviewedUser;
    private double selectedScore = 0.0;
    private ToggleButton[] starButtons = new ToggleButton[5];

    @FXML
    public void initialize() {
        setupLabels();
        setupStarRating();
        setupCommentArea();
        updateSubmitButton();
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("review.create.title", "Write a Review"));
        instructionsLabel.setText(localeService.getMessage("review.create.instructions",
                "Share your experience with this transaction"));

        submitButton.setText(localeService.getMessage("review.create.submit", "Submit Review"));
        cancelButton.setText(localeService.getMessage("review.create.cancel", "Cancel"));
    }

    private void setupStarRating() {
        starsContainer.getChildren().clear();

        ToggleGroup starGroup = new ToggleGroup();

        for (int i = 0; i < 5; i++) {
            final int starValue = i + 1;
            ToggleButton starButton = new ToggleButton("★");
            starButton.getStyleClass().addAll("star-button", "star-empty");
            starButton.setToggleGroup(starGroup);

            starButton.setOnAction(e -> {
                if (starButton.isSelected()) {
                    setStarRating(starValue);
                } else {
                    // Prevent deselection of all stars
                    starButton.setSelected(true);
                }
            });

            starButtons[i] = starButton;
            starsContainer.getChildren().add(starButton);
        }

        updateScoreDisplay();
    }

    private void setupCommentArea() {
        commentTextArea.setPromptText(localeService.getMessage("review.create.comment.prompt",
                "Write your review here (optional)"));
        commentTextArea.setWrapText(true);

        // Add character count listener
        commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
            updateCharacterCount();
            updateSubmitButton();
        });

        updateCharacterCount();
    }

    public void setOffer(OfferViewModel offer) {
        this.currentOffer = offer;

        if (offer != null) {
            Platform.runLater(() -> {
                // Determine who should be reviewed
                UserViewModel currentUser = ViewModelMapper.getInstance().toViewModel(sessionService.getUser());
                if (offer.getOfferingUser().getId().equals(currentUser.getId())) {
                    // Current user made the offer, so review the listing owner
                    reviewedUser = offer.getListing().getUser();
                } else {
                    // Current user owns the listing, so review the offer maker
                    reviewedUser = offer.getOfferingUser();
                }

                updateUI();
            });
        }
    }

    private void updateUI() {
        if (reviewedUser != null) {
            reviewingUserLabel.setText(String.format(
                    localeService.getMessage("review.create.reviewing", "Reviewing: %s"),
                    reviewedUser.getDisplayName()));
        }

        if (currentOffer != null) {
            String offerType = getOfferTypeDisplayName(currentOffer);
            offerDetailsLabel.setText(String.format(
                    localeService.getMessage("review.create.offer.details", "Transaction: %s for \"%s\""),
                    offerType, currentOffer.getListingTitle()));
        }
    }

    private void setStarRating(int rating) {
        selectedScore = rating;

        // Update star button styles
        for (int i = 0; i < 5; i++) {
            ToggleButton star = starButtons[i];
            if (i < rating) {
                star.getStyleClass().removeAll("star-empty");
                star.getStyleClass().add("star-filled");
                star.setSelected(true);
            } else {
                star.getStyleClass().removeAll("star-filled");
                star.getStyleClass().add("star-empty");
                star.setSelected(false);
            }
        }

        updateScoreDisplay();
        updateSubmitButton();
    }

    private void updateScoreDisplay() {
        String scoreText = selectedScore > 0
                ? String.format("%.1f/5.0", selectedScore)
                : localeService.getMessage("review.create.no.rating", "No rating selected");
        scoreLabel.setText(scoreText);
    }

    private void updateCharacterCount() {
        int length = commentTextArea.getText().length();
        int maxLength = 1000;
        characterCountLabel.setText(String.format("%d/%d", length, maxLength));

        if (length > maxLength) {
            characterCountLabel.getStyleClass().add("character-count-exceeded");
        } else {
            characterCountLabel.getStyleClass().remove("character-count-exceeded");
        }
    }

    private void updateSubmitButton() {
        boolean hasValidRating = selectedScore > 0;
        boolean hasValidComment = commentTextArea.getText().length() <= 1000;

        submitButton.setDisable(!hasValidRating || !hasValidComment);
    }

    @FXML
    private void handleSubmit() {
        if (!validateReview()) {
            return;
        }

        // Create review
        ReviewViewModel review = new ReviewViewModel();
        review.setOfferId(currentOffer.getId());
        review.setScore(selectedScore);
        review.setComment(commentTextArea.getText().trim());

        submitButton.setDisable(true);

        reviewService.createReview(review)
                .thenAccept(createdReview -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("review.create.success.title", "Review Submitted"),
                            localeService.getMessage("review.create.success.header", "Thank you!"),
                            localeService.getMessage("review.create.success.message",
                                    "Your review has been submitted successfully."));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("review.create.error.title", "Error"),
                                localeService.getMessage("review.create.error.header", "Failed to submit review"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateReview() {
        // Check if rating is selected
        if (selectedScore <= 0) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("review.validation.title", "Validation Error"),
                    localeService.getMessage("review.validation.header", "Invalid Input"),
                    localeService.getMessage("review.validation.rating.required", "Please select a rating"));
            return false;
        }

        // Check comment length
        if (commentTextArea.getText().length() > 1000) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("review.validation.title", "Validation Error"),
                    localeService.getMessage("review.validation.header", "Invalid Input"),
                    localeService.getMessage("review.validation.comment.too.long",
                            "Comment is too long (max 1000 characters)"));
            return false;
        }

        return true;
    }

    private String getOfferTypeDisplayName(OfferViewModel offer) {
        if (offer.hasMoneyOffer() && offer.hasItemOffer()) {
            return localeService.getMessage("offers.type.mixed", "Mixed");
        } else if (offer.hasMoneyOffer()) {
            return localeService.getMessage("offers.type.money", "Money");
        } else if (offer.hasItemOffer()) {
            return localeService.getMessage("offers.type.trade", "Trade");
        } else {
            return localeService.getMessage("offers.type.unknown", "Unknown");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}