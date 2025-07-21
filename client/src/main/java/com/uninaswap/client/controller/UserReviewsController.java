package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ReviewService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ReviewViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
public class UserReviewsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private Label totalReviewsLabel;

    @FXML
    private HBox ratingStarsBox;

    @FXML
    private ProgressBar ratingBar;

    @FXML
    private TabPane reviewsTabPane;

    @FXML
    private TableView<ReviewViewModel> receivedReviewsTable;

    @FXML
    private TableColumn<ReviewViewModel, String> receivedReviewerColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> receivedScoreColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> receivedCommentColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> receivedDateColumn;

    @FXML
    private TableView<ReviewViewModel> givenReviewsTable;

    @FXML
    private TableColumn<ReviewViewModel, String> givenReviewedUserColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> givenScoreColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> givenCommentColumn;

    @FXML
    private TableColumn<ReviewViewModel, String> givenDateColumn;

    @FXML
    private VBox reviewDetailsSection;

    @FXML
    private Label detailReviewerLabel;

    @FXML
    private Label detailScoreLabel;

    @FXML
    private HBox detailStarsBox;

    @FXML
    private Label detailDateLabel;

    @FXML
    private TextArea detailCommentArea;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final ReviewService reviewService = ReviewService.getInstance();

    // Data
    private UserViewModel currentUser;
    private final ObservableList<ReviewViewModel> receivedReviews = FXCollections.observableArrayList();
    private final ObservableList<ReviewViewModel> givenReviews = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupLabels();
        setupTables();
        setupSelectionHandlers();
        hideReviewDetails();
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("reviews.title", "User Reviews"));
        refreshButton.setText(localeService.getMessage("reviews.refresh", "Refresh"));
        closeButton.setText(localeService.getMessage("reviews.close", "Close"));
    }

    private void setupTables() {
        // Received reviews table
        receivedReviewerColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReviewerName()));

        receivedScoreColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedScore()));

        receivedCommentColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            if (comment != null && comment.length() > 50) {
                comment = comment.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(comment != null ? comment : "");
        });

        receivedDateColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        // Given reviews table
        givenReviewedUserColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReviewedUserName()));

        givenScoreColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedScore()));

        givenCommentColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            if (comment != null && comment.length() > 50) {
                comment = comment.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(comment != null ? comment : "");
        });

        givenDateColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        // Bind tables to observable lists
        receivedReviewsTable.setItems(receivedReviews);
        givenReviewsTable.setItems(givenReviews);
    }

    private void setupSelectionHandlers() {
        receivedReviewsTable.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSelection) -> {
                    if (newSelection != null) {
                        showReviewDetails(newSelection);
                        givenReviewsTable.getSelectionModel().clearSelection();
                    }
                });

        givenReviewsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                showReviewDetails(newSelection);
                receivedReviewsTable.getSelectionModel().clearSelection();
            }
        });
    }

    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                loadReviews();
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getDisplayName());
            updateRatingSummary();
        }
    }

    private void updateRatingSummary() {
        if (currentUser != null) {
            // Get rating summary from service
            reviewService.getUserRatingSummary(currentUser.getId())
                    .thenAccept(summary -> Platform.runLater(() -> {
                        double avgRating = summary.getAverageRating();
                        int totalReviews = summary.getTotalReviews();

                        averageRatingLabel.setText(String.format("%.1f", avgRating));
                        totalReviewsLabel.setText(String.format(
                                localeService.getMessage("reviews.total.count", "%d reviews"),
                                totalReviews));

                        updateRatingVisuals(avgRating);
                    }))
                    .exceptionally(_ -> {
                        Platform.runLater(() -> {
                            averageRatingLabel.setText("N/A");
                            totalReviewsLabel.setText(localeService.getMessage("reviews.no.reviews", "No reviews"));
                            updateRatingVisuals(0.0);
                        });
                        return null;
                    });
        }
    }

    private void updateRatingVisuals(double rating) {
        // Update star display
        ratingStarsBox.getChildren().clear();
        int fullStars = (int) Math.floor(rating);
        boolean hasHalfStar = (rating - fullStars) >= 0.5;

        for (int i = 0; i < fullStars; i++) {
            Label star = new Label("★");
            star.getStyleClass().add("rating-star-filled");
            ratingStarsBox.getChildren().add(star);
        }

        if (hasHalfStar) {
            Label halfStar = new Label("☆");
            halfStar.getStyleClass().add("rating-star-half");
            ratingStarsBox.getChildren().add(halfStar);
        }

        for (int i = fullStars + (hasHalfStar ? 1 : 0); i < 5; i++) {
            Label emptyStar = new Label("☆");
            emptyStar.getStyleClass().add("rating-star-empty");
            ratingStarsBox.getChildren().add(emptyStar);
        }

        // Update progress bar
        ratingBar.setProgress(rating / 5.0);
    }

    private void loadReviews() {
        if (currentUser == null)
            return;

        // Load received reviews
        reviewService.getReceivedReviews(currentUser.getId())
                .thenAccept(reviews -> Platform.runLater(() -> {
                    receivedReviews.clear();
                    reviews.forEach(reviewDTO -> {
                        // Convert DTO to ViewModel if needed
                        // For now, assuming we get ViewModels directly
                        // receivedReviews.add(viewModelMapper.toViewModel(reviewDTO));
                    });
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("reviews.error.title", "Error"),
                            localeService.getMessage("reviews.error.load.received", "Failed to load received reviews"),
                            ex.getMessage()));
                    return null;
                });

        // Load given reviews
        reviewService.getGivenReviews(currentUser.getId())
                .thenAccept(reviews -> Platform.runLater(() -> {
                    givenReviews.clear();
                    reviews.forEach(reviewDTO -> {
                        // Convert DTO to ViewModel if needed
                        // givenReviews.add(viewModelMapper.toViewModel(reviewDTO));
                    });
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("reviews.error.title", "Error"),
                            localeService.getMessage("reviews.error.load.given", "Failed to load given reviews"),
                            ex.getMessage()));
                    return null;
                });
    }

    private void showReviewDetails(ReviewViewModel review) {
        if (review == null) {
            hideReviewDetails();
            return;
        }

        detailReviewerLabel.setText(review.getReviewerName());
        detailScoreLabel.setText(review.getFormattedScore());
        detailDateLabel.setText(review.getFormattedDate());
        detailCommentArea.setText(review.getComment() != null ? review.getComment() : "");

        // Update detail stars
        detailStarsBox.getChildren().clear();
        double score = review.getScore();
        int fullStars = (int) Math.floor(score);
        boolean hasHalfStar = (score - fullStars) >= 0.5;

        for (int i = 0; i < fullStars; i++) {
            Label star = new Label("★");
            star.getStyleClass().add("detail-star-filled");
            detailStarsBox.getChildren().add(star);
        }

        if (hasHalfStar) {
            Label halfStar = new Label("☆");
            halfStar.getStyleClass().add("detail-star-half");
            detailStarsBox.getChildren().add(halfStar);
        }

        for (int i = fullStars + (hasHalfStar ? 1 : 0); i < 5; i++) {
            Label emptyStar = new Label("☆");
            emptyStar.getStyleClass().add("detail-star-empty");
            detailStarsBox.getChildren().add(emptyStar);
        }

        reviewDetailsSection.setVisible(true);
    }

    private void hideReviewDetails() {
        reviewDetailsSection.setVisible(false);
    }

    @FXML
    private void handleRefresh() {
        updateRatingSummary();
        loadReviews();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}