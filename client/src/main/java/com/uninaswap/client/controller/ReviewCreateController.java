package com.uninaswap.client.controller;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ReviewService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.ReviewViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ReviewCreateController implements Refreshable {

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

    // Additional labels for localization
    @FXML
    private Label ratingHeaderLabel;

    @FXML
    private Label ratingHelpLabel;

    @FXML
    private Label commentHeaderLabel;

    @FXML
    private Label commentHelpLabel;

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
        setupStarRating();
        setupCommentArea();
        updateSubmitButton();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("review.create.debug.initialized", "ReviewCreate controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update static labels
        if (titleLabel != null) {
            titleLabel.setText(localeService.getMessage("review.create.title", "Write a Review"));
        }
        if (instructionsLabel != null) {
            instructionsLabel.setText(localeService.getMessage("review.create.instructions",
                    "Share your experience with this transaction"));
        }
        
        // Update section headers
        if (ratingHeaderLabel != null) {
            ratingHeaderLabel.setText(localeService.getMessage("review.create.rating.header", "Rating"));
        }
        if (ratingHelpLabel != null) {
            ratingHelpLabel.setText(localeService.getMessage("review.create.rating.help", 
                    "Click on a star to rate your experience"));
        }
        if (commentHeaderLabel != null) {
            commentHeaderLabel.setText(localeService.getMessage("review.create.comment.header", 
                    "Review Comment (Optional)"));
        }
        if (commentHelpLabel != null) {
            commentHelpLabel.setText(localeService.getMessage("review.create.comment.help", 
                    "Share details about your experience with this user"));
        }

        // Update button labels
        if (submitButton != null) {
            submitButton.setText(localeService.getMessage("review.create.submit", "Submit Review"));
        }
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("review.create.cancel", "Cancel"));
        }

        // Update prompt text
        if (commentTextArea != null) {
            commentTextArea.setPromptText(localeService.getMessage("review.create.comment.prompt",
                    "Write your review here (optional)"));
        }

        // Update dynamic content if data is loaded
        updateScoreDisplay();
        updateCharacterCount();
        
        if (reviewedUser != null && currentOffer != null) {
            updateTransactionDetails();
        }
    }

    private void setupStarRating() {
        if (starsContainer != null) {
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
            System.out.println(localeService.getMessage("review.create.debug.stars.setup", "Star rating setup completed"));
        }
    }

    private void setupCommentArea() {
        if (commentTextArea != null) {
            commentTextArea.setPromptText(localeService.getMessage("review.create.comment.prompt",
                    "Write your review here (optional)"));
            commentTextArea.setWrapText(true);

            // Add character count listener
            commentTextArea.textProperty().addListener((obs, oldText, newText) -> {
                updateCharacterCount();
                updateSubmitButton();
            });

            updateCharacterCount();
            System.out.println(localeService.getMessage("review.create.debug.comment.setup", "Comment area setup completed"));
        }
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

                updateTransactionDetails();
                System.out.println(localeService.getMessage("review.create.debug.offer.set", 
                    "Offer set for review, target user: {0}")
                        .replace("{0}", reviewedUser.getDisplayName()));
            });
        }
    }

    private void updateTransactionDetails() {
        if (reviewedUser != null && reviewingUserLabel != null) {
            reviewingUserLabel.setText(localeService.getMessage("review.create.reviewing", "Reviewing: {0}")
                .replace("{0}", reviewedUser.getDisplayName()));
        }

        if (currentOffer != null && offerDetailsLabel != null) {
            String offerType = getOfferTypeDisplayName(currentOffer);
            offerDetailsLabel.setText(localeService.getMessage("review.create.offer.details", 
                "Transaction: {0} for \"{1}\"")
                    .replace("{0}", offerType)
                    .replace("{1}", currentOffer.getListingTitle()));
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
        
        System.out.println(localeService.getMessage("review.create.debug.rating.set", 
            "Star rating set to: {0}").replace("{0}", String.valueOf(rating)));
    }

    private void updateScoreDisplay() {
        if (scoreLabel != null) {
            String scoreText = selectedScore > 0
                    ? localeService.getMessage("review.create.score.display", "{0}/5.0")
                        .replace("{0}", String.format("%.1f", selectedScore))
                    : localeService.getMessage("review.create.no.rating", "No rating selected");
            scoreLabel.setText(scoreText);
        }
    }

    private void updateCharacterCount() {
        if (commentTextArea != null && characterCountLabel != null) {
            int length = commentTextArea.getText().length();
            int maxLength = 1000;
            
            characterCountLabel.setText(localeService.getMessage("review.create.character.count", "{0}/{1}")
                .replace("{0}", String.valueOf(length))
                .replace("{1}", String.valueOf(maxLength)));

            if (length > maxLength) {
                characterCountLabel.getStyleClass().add("character-count-exceeded");
            } else {
                characterCountLabel.getStyleClass().remove("character-count-exceeded");
            }
        }
    }
    
    private DoubleProperty scoreProperty() {
        return new SimpleDoubleProperty(selectedScore);
    }

    private void updateSubmitButton() {
        if (submitButton != null && commentTextArea != null) {
            boolean hasValidRating = selectedScore > 0;
            boolean hasValidComment = commentTextArea.getText().length() <= 1000;
            submitButton.setDisable(!hasValidRating || !hasValidComment);
        }
    }

    private boolean isValidReview() {
        return selectedScore > 0 && (commentTextArea == null || commentTextArea.getText().length() <= 1000);
    }

    public void bindSubmitButton(Button externalSubmitButton) {
        if (externalSubmitButton != null && commentTextArea != null) {
            externalSubmitButton.disableProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                    () -> !isValidReview(),
                    scoreProperty(),
                    commentTextArea.textProperty()
                )
            );
            System.out.println(localeService.getMessage("review.create.debug.button.bound", 
                "External submit button bound to validation"));
        }
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

        // Handle empty comment properly
        String comment = commentTextArea.getText().trim();
        review.setComment(comment.isEmpty() ? null : comment);

        submitButton.setDisable(true);
        
        System.out.println(localeService.getMessage("review.create.debug.submitting", 
            "Submitting review with score: {0}").replace("{0}", String.valueOf(selectedScore)));

        reviewService.createReview(review)
                .thenAccept(createdReview -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("review.create.success.title", "Review Submitted"),
                            localeService.getMessage("review.create.success.header", "Thank you!"),
                            localeService.getMessage("review.create.success.message",
                                    "Your review has been submitted successfully."));
                
                    // Trigger refresh of offers to update the status
                    EventBusService.getInstance().publishEvent(EventTypes.OFFER_UPDATED, currentOffer);
                    
                    System.out.println(localeService.getMessage("review.create.debug.success", 
                        "Review submitted successfully"));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("review.create.error.title", "Error"),
                                localeService.getMessage("review.create.error.header", "Failed to submit review"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                        System.err.println(localeService.getMessage("review.create.debug.error", 
                            "Failed to submit review: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        System.out.println(localeService.getMessage("review.create.debug.cancelled", 
            "Review creation cancelled by user"));
        closeWindow();
    }

    private boolean validateReview() {
        // Check if rating is selected
        if (selectedScore <= 0) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("review.validation.title", "Validation Error"),
                    localeService.getMessage("review.validation.header", "Invalid Input"),
                    localeService.getMessage("review.validation.rating.required", "Please select a rating"));
            System.err.println(localeService.getMessage("review.create.debug.validation.rating", 
                "Validation failed: no rating selected"));
            return false;
        }

        // Check comment length (but comment can be empty)
        if (commentTextArea != null && commentTextArea.getText().length() > 1000) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("review.validation.title", "Validation Error"),
                    localeService.getMessage("review.validation.header", "Invalid Input"),
                    localeService.getMessage("review.validation.comment.too.long",
                            "Comment is too long (max 1000 characters)"));
            System.err.println(localeService.getMessage("review.create.debug.validation.comment", 
                "Validation failed: comment too long ({0} characters)")
                    .replace("{0}", String.valueOf(commentTextArea.getText().length())));
            return false;
        }

        System.out.println(localeService.getMessage("review.create.debug.validation.passed", 
            "Review validation passed"));
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
        if (stage != null) {
            stage.close();
        }
    }

    // Legacy method for backward compatibility
    public void submitReview() {
        handleSubmit();
    }
}