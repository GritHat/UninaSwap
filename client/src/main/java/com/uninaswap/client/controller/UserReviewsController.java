package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.Refreshable;
import com.uninaswap.client.service.ReviewService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
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

public class UserReviewsController implements Refreshable {

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

    // Received Reviews Tab
    @FXML
    private Tab receivedReviewsTab;

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

    // Given Reviews Tab
    @FXML
    private Tab givenReviewsTab;

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

    // Review Details Section
    @FXML
    private VBox reviewDetailsSection;

    @FXML
    private Label reviewDetailsLabel;

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

    // Additional UI elements for localization
    @FXML
    private Label overallRatingLabel;

    @FXML
    private Label reviewerStaticLabel;

    @FXML
    private Label ratingStaticLabel;

    @FXML
    private Label dateStaticLabel;

    @FXML
    private Label commentStaticLabel;

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
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        setupLabels();
        setupTables();
        setupSelectionHandlers();
        hideReviewDetails();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("reviews.debug.initialized", "UserReviews controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update all static labels
        setupLabels();
        
        // Update tab headers
        updateTabHeaders();
        
        // Update table column headers
        updateTableHeaders();
        
        // Update detail section labels
        updateDetailSectionLabels();
        
        // Update table placeholders
        updateTablePlaceholders();
        
        // Update user info if user is set
        if (currentUser != null) {
            updateUserInfo();
        }
        
        System.out.println(localeService.getMessage("reviews.debug.ui.refreshed", "UserReviews UI refreshed"));
    }

    private void setupLabels() {
        if (titleLabel != null) {
            titleLabel.setText(localeService.getMessage("reviews.title", "User Reviews"));
        }
        if (refreshButton != null) {
            refreshButton.setText(localeService.getMessage("reviews.button.refresh", "Refresh"));
        }
        if (closeButton != null) {
            closeButton.setText(localeService.getMessage("reviews.button.close", "Close"));
        }
        if (overallRatingLabel != null) {
            overallRatingLabel.setText(localeService.getMessage("reviews.overall.rating", "Overall Rating"));
        }
        if (reviewDetailsLabel != null) {
            reviewDetailsLabel.setText(localeService.getMessage("reviews.details.title", "Review Details"));
        }
    }

    private void updateTabHeaders() {
        if (receivedReviewsTab != null) {
            receivedReviewsTab.setText(localeService.getMessage("reviews.tab.received", "Received Reviews"));
        }
        if (givenReviewsTab != null) {
            givenReviewsTab.setText(localeService.getMessage("reviews.tab.given", "Given Reviews"));
        }
    }

    private void updateTableHeaders() {
        // Received reviews table headers
        if (receivedReviewerColumn != null) {
            receivedReviewerColumn.setText(localeService.getMessage("reviews.column.reviewer", "Reviewer"));
        }
        if (receivedScoreColumn != null) {
            receivedScoreColumn.setText(localeService.getMessage("reviews.column.rating", "Rating"));
        }
        if (receivedCommentColumn != null) {
            receivedCommentColumn.setText(localeService.getMessage("reviews.column.comment", "Comment"));
        }
        if (receivedDateColumn != null) {
            receivedDateColumn.setText(localeService.getMessage("reviews.column.date", "Date"));
        }

        // Given reviews table headers
        if (givenReviewedUserColumn != null) {
            givenReviewedUserColumn.setText(localeService.getMessage("reviews.column.reviewed.user", "Reviewed User"));
        }
        if (givenScoreColumn != null) {
            givenScoreColumn.setText(localeService.getMessage("reviews.column.rating", "Rating"));
        }
        if (givenCommentColumn != null) {
            givenCommentColumn.setText(localeService.getMessage("reviews.column.comment", "Comment"));
        }
        if (givenDateColumn != null) {
            givenDateColumn.setText(localeService.getMessage("reviews.column.date", "Date"));
        }
    }

    private void updateDetailSectionLabels() {
        if (reviewerStaticLabel != null) {
            reviewerStaticLabel.setText(localeService.getMessage("reviews.detail.reviewer", "Reviewer:"));
        }
        if (ratingStaticLabel != null) {
            ratingStaticLabel.setText(localeService.getMessage("reviews.detail.rating", "Rating:"));
        }
        if (dateStaticLabel != null) {
            dateStaticLabel.setText(localeService.getMessage("reviews.detail.date", "Date:"));
        }
        if (commentStaticLabel != null) {
            commentStaticLabel.setText(localeService.getMessage("reviews.detail.comment", "Comment:"));
        }
    }

    private void updateTablePlaceholders() {
        if (receivedReviewsTable != null) {
            Label receivedPlaceholder = new Label(localeService.getMessage("reviews.placeholder.received", "No reviews received yet"));
            receivedPlaceholder.getStyleClass().add("table-placeholder");
            receivedReviewsTable.setPlaceholder(receivedPlaceholder);
        }
        
        if (givenReviewsTable != null) {
            Label givenPlaceholder = new Label(localeService.getMessage("reviews.placeholder.given", "No reviews given yet"));
            givenPlaceholder.getStyleClass().add("table-placeholder");
            givenReviewsTable.setPlaceholder(givenPlaceholder);
        }
    }

    private void setupTables() {
        // Received reviews table
        receivedReviewerColumn.setCellValueFactory(cellData -> {
            String reviewerName = cellData.getValue().getReviewerName();
            return new SimpleStringProperty(reviewerName != null ? reviewerName : 
                localeService.getMessage("reviews.unknown.reviewer", "Unknown Reviewer"));
        });

        receivedScoreColumn.setCellValueFactory(cellData -> {
            String formattedScore = cellData.getValue().getFormattedScore();
            return new SimpleStringProperty(formattedScore != null ? formattedScore : 
                localeService.getMessage("reviews.no.rating", "No Rating"));
        });

        receivedCommentColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            if (comment != null && !comment.trim().isEmpty()) {
                if (comment.length() > 50) {
                    comment = comment.substring(0, 50) + "...";
                }
                return new SimpleStringProperty(comment);
            }
            return new SimpleStringProperty(localeService.getMessage("reviews.no.comment", "No comment"));
        });

        receivedDateColumn.setCellValueFactory(cellData -> {
            String formattedDate = cellData.getValue().getFormattedDate();
            return new SimpleStringProperty(formattedDate != null ? formattedDate : 
                localeService.getMessage("reviews.unknown.date", "Unknown"));
        });

        // Given reviews table
        givenReviewedUserColumn.setCellValueFactory(cellData -> {
            String reviewedUserName = cellData.getValue().getReviewedUserName();
            return new SimpleStringProperty(reviewedUserName != null ? reviewedUserName : 
                localeService.getMessage("reviews.unknown.user", "Unknown User"));
        });

        givenScoreColumn.setCellValueFactory(cellData -> {
            String formattedScore = cellData.getValue().getFormattedScore();
            return new SimpleStringProperty(formattedScore != null ? formattedScore : 
                localeService.getMessage("reviews.no.rating", "No Rating"));
        });

        givenCommentColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            if (comment != null && !comment.trim().isEmpty()) {
                if (comment.length() > 50) {
                    comment = comment.substring(0, 50) + "...";
                }
                return new SimpleStringProperty(comment);
            }
            return new SimpleStringProperty(localeService.getMessage("reviews.no.comment", "No comment"));
        });

        givenDateColumn.setCellValueFactory(cellData -> {
            String formattedDate = cellData.getValue().getFormattedDate();
            return new SimpleStringProperty(formattedDate != null ? formattedDate : 
                localeService.getMessage("reviews.unknown.date", "Unknown"));
        });

        // Bind tables to observable lists
        receivedReviewsTable.setItems(receivedReviews);
        givenReviewsTable.setItems(givenReviews);
        
        // Set initial placeholders
        updateTablePlaceholders();
    }

    private void setupSelectionHandlers() {
        receivedReviewsTable.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSelection) -> {
                    if (newSelection != null) {
                        showReviewDetails(newSelection);
                        givenReviewsTable.getSelectionModel().clearSelection();
                        System.out.println(localeService.getMessage("reviews.debug.received.selected", "Received review selected"));
                    }
                });

        givenReviewsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                showReviewDetails(newSelection);
                receivedReviewsTable.getSelectionModel().clearSelection();
                System.out.println(localeService.getMessage("reviews.debug.given.selected", "Given review selected"));
            }
        });
    }

    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                loadReviews();
                System.out.println(localeService.getMessage("reviews.debug.user.set", "User set for reviews view: {0}")
                    .replace("{0}", user.getDisplayName() != null ? user.getDisplayName() : "unknown"));
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null && userNameLabel != null) {
            String displayName = currentUser.getDisplayName();
            userNameLabel.setText(displayName != null ? displayName : 
                localeService.getMessage("reviews.unknown.user", "Unknown User"));
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

                        if (averageRatingLabel != null) {
                            averageRatingLabel.setText(avgRating > 0 ? String.format("%.1f", avgRating) : 
                                localeService.getMessage("reviews.no.rating.short", "N/A"));
                        }
                        
                        if (totalReviewsLabel != null) {
                            totalReviewsLabel.setText(localeService.getMessage("reviews.total.count", "{0} reviews")
                                .replace("{0}", String.valueOf(totalReviews)));
                        }

                        updateRatingVisuals(avgRating);
                        
                        System.out.println(localeService.getMessage("reviews.debug.summary.updated", 
                            "Rating summary updated: {0} avg, {1} total")
                                .replace("{0}", String.format("%.1f", avgRating))
                                .replace("{1}", String.valueOf(totalReviews)));
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (averageRatingLabel != null) {
                                averageRatingLabel.setText(localeService.getMessage("reviews.no.rating.short", "N/A"));
                            }
                            if (totalReviewsLabel != null) {
                                totalReviewsLabel.setText(localeService.getMessage("reviews.no.reviews", "No reviews"));
                            }
                            updateRatingVisuals(0.0);
                            
                            System.err.println(localeService.getMessage("reviews.debug.summary.error", 
                                "Error loading rating summary: {0}")
                                    .replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        }
    }

    private void updateRatingVisuals(double rating) {
        // Update star display
        if (ratingStarsBox != null) {
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
        }

        // Update progress bar
        if (ratingBar != null) {
            ratingBar.setProgress(rating / 5.0);
        }
    }

    private void loadReviews() {
        if (currentUser == null) {
            System.out.println(localeService.getMessage("reviews.debug.no.user", "No user set for loading reviews"));
            return;
        }

        System.out.println(localeService.getMessage("reviews.debug.loading", "Loading reviews for user"));

        // Load received reviews
        reviewService.getReceivedReviews(currentUser.getId())
                .thenAccept(reviews -> Platform.runLater(() -> {
                    receivedReviews.clear();
                    reviews.forEach(reviewDTO -> {
                        // TODO: Convert DTO to ViewModel when ViewModelMapper is available
                        // receivedReviews.add(viewModelMapper.toViewModel(reviewDTO));
                    });
                    
                    System.out.println(localeService.getMessage("reviews.debug.received.loaded", 
                        "Received reviews loaded: {0} reviews")
                            .replace("{0}", String.valueOf(reviews.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("reviews.debug.received.error", 
                            "Error loading received reviews: {0}")
                                .replace("{0}", ex.getMessage()));
                        
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("reviews.error.title", "Error"),
                                localeService.getMessage("reviews.error.load.received", "Failed to load received reviews"),
                                localeService.getMessage("reviews.error.load.received.content", 
                                    "Could not load the reviews you have received: {0}")
                                        .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });

        // Load given reviews
        reviewService.getGivenReviews(currentUser.getId())
                .thenAccept(reviews -> Platform.runLater(() -> {
                    givenReviews.clear();
                    reviews.forEach(reviewDTO -> {
                        // TODO: Convert DTO to ViewModel when ViewModelMapper is available
                        // givenReviews.add(viewModelMapper.toViewModel(reviewDTO));
                    });
                    
                    System.out.println(localeService.getMessage("reviews.debug.given.loaded", 
                        "Given reviews loaded: {0} reviews")
                            .replace("{0}", String.valueOf(reviews.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("reviews.debug.given.error", 
                            "Error loading given reviews: {0}")
                                .replace("{0}", ex.getMessage()));
                        
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("reviews.error.title", "Error"),
                                localeService.getMessage("reviews.error.load.given", "Failed to load given reviews"),
                                localeService.getMessage("reviews.error.load.given.content", 
                                    "Could not load the reviews you have given: {0}")
                                        .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    private void showReviewDetails(ReviewViewModel review) {
        if (review == null) {
            hideReviewDetails();
            return;
        }

        if (detailReviewerLabel != null) {
            String reviewerName = review.getReviewerName();
            detailReviewerLabel.setText(reviewerName != null ? reviewerName : 
                localeService.getMessage("reviews.unknown.reviewer", "Unknown Reviewer"));
        }
        
        if (detailScoreLabel != null) {
            String formattedScore = review.getFormattedScore();
            detailScoreLabel.setText(formattedScore != null ? formattedScore : 
                localeService.getMessage("reviews.no.rating", "No Rating"));
        }
        
        if (detailDateLabel != null) {
            String formattedDate = review.getFormattedDate();
            detailDateLabel.setText(formattedDate != null ? formattedDate : 
                localeService.getMessage("reviews.unknown.date", "Unknown"));
        }
        
        if (detailCommentArea != null) {
            String comment = review.getComment();
            detailCommentArea.setText(comment != null && !comment.trim().isEmpty() ? comment : 
                localeService.getMessage("reviews.no.comment", "No comment"));
        }

        // Update detail stars
        if (detailStarsBox != null) {
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
        }

        if (reviewDetailsSection != null) {
            reviewDetailsSection.setVisible(true);
        }
        
        System.out.println(localeService.getMessage("reviews.debug.details.shown", "Review details shown"));
    }

    private void hideReviewDetails() {
        if (reviewDetailsSection != null) {
            reviewDetailsSection.setVisible(false);
        }
        System.out.println(localeService.getMessage("reviews.debug.details.hidden", "Review details hidden"));
    }

    @FXML
    private void handleRefresh() {
        System.out.println(localeService.getMessage("reviews.debug.refresh.requested", "Reviews refresh requested"));
        
        updateRatingSummary();
        loadReviews();
        
        // Show brief feedback to user
        if (refreshButton != null) {
            String originalText = refreshButton.getText();
            refreshButton.setText(localeService.getMessage("reviews.button.refreshing", "Refreshing..."));
            refreshButton.setDisable(true);
            
            // Reset button after a short delay
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            refreshButton.setText(originalText);
                            refreshButton.setDisable(false);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }

    @FXML
    private void handleClose() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
                System.out.println(localeService.getMessage("reviews.debug.closed", "UserReviews dialog closed"));
            } else {
                System.err.println(localeService.getMessage("reviews.debug.close.error", "Cannot close dialog: no stage found"));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("reviews.debug.close.exception", "Error closing dialog: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    
    /**
     * Check if reviews are loaded
     */
    public boolean hasReviews() {
        return !receivedReviews.isEmpty() || !givenReviews.isEmpty();
    }

    /**
     * Get received reviews count
     */
    public int getReceivedReviewsCount() {
        return receivedReviews.size();
    }

    /**
     * Get given reviews count
     */
    public int getGivenReviewsCount() {
        return givenReviews.size();
    }

    /**
     * Refresh data (can be called externally)
     */
    public void refreshData() {
        updateRatingSummary();
        loadReviews();
        System.out.println(localeService.getMessage("reviews.debug.external.refresh", "External refresh triggered"));
    }

    /**
     * Get the current user
     */
    public UserViewModel getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear all data
     */
    public void clearData() {
        currentUser = null;
        receivedReviews.clear();
        givenReviews.clear();
        hideReviewDetails();
        if (userNameLabel != null) {
            userNameLabel.setText("");
        }
        if (averageRatingLabel != null) {
            averageRatingLabel.setText("--");
        }
        if (totalReviewsLabel != null) {
            totalReviewsLabel.setText("--");
        }
        updateRatingVisuals(0.0);
        System.out.println(localeService.getMessage("reviews.debug.data.cleared", "UserReviews data cleared"));
    }

    /**
     * Check if user is set
     */
    public boolean hasUser() {
        return currentUser != null;
    }

    /**
     * Get user display name
     */
    public String getUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : 
            localeService.getMessage("reviews.no.user", "No User");
    }

    /**
     * Force refresh of the tables display
     */
    public void refreshTables() {
        if (receivedReviewsTable != null) {
            receivedReviewsTable.refresh();
        }
        if (givenReviewsTable != null) {
            givenReviewsTable.refresh();
        }
        updateTablePlaceholders();
        System.out.println(localeService.getMessage("reviews.debug.tables.refreshed", "Tables display refreshed"));
    }

    /**
     * Show details for a specific review
     */
    public void showReview(ReviewViewModel review) {
        if (review != null) {
            showReviewDetails(review);
            System.out.println(localeService.getMessage("reviews.debug.external.show", "External review display requested"));
        }
    }

    /**
     * Clear review selection and hide details
     */
    public void clearSelection() {
        if (receivedReviewsTable != null) {
            receivedReviewsTable.getSelectionModel().clearSelection();
        }
        if (givenReviewsTable != null) {
            givenReviewsTable.getSelectionModel().clearSelection();
        }
        hideReviewDetails();
        System.out.println(localeService.getMessage("reviews.debug.selection.cleared", "Review selection cleared"));
    }

    /**
     * Get current average rating
     */
    public double getCurrentAverageRating() {
        return currentUser != null ? 0.0 : 0.0; // TODO: Implement when rating data is available
    }

    /**
     * Get total reviews count
     */
    public int getTotalReviewsCount() {
        return getReceivedReviewsCount() + getGivenReviewsCount();
    }
}