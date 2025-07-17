package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.viewmodel.ReviewViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.ReviewDTO;
import com.uninaswap.common.message.ReviewMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReviewService {
    private static ReviewService instance;
    private final WebSocketClient webSocketClient = WebSocketManager.getClient();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    private CompletableFuture<?> futureToComplete;
    private Consumer<ReviewMessage> messageCallback;

    // Observable lists for UI binding
    private final ObservableList<ReviewViewModel> userReceivedReviews = FXCollections.observableArrayList();
    private final ObservableList<ReviewViewModel> userGivenReviews = FXCollections.observableArrayList();

    private ReviewService() {
        // Register message handler
        this.webSocketClient.registerMessageHandler(ReviewMessage.class, this::handleReviewMessage);
    }

    public static synchronized ReviewService getInstance() {
        if (instance == null) {
            instance = new ReviewService();
        }
        return instance;
    }

    /**
     * Create a new review
     */
    public CompletableFuture<ReviewViewModel> createReview(ReviewViewModel reviewViewModel) {
        CompletableFuture<ReviewViewModel> future = new CompletableFuture<>();

        // Convert ViewModel to DTO for service communication
        ReviewDTO reviewDTO = viewModelMapper.toDTO(reviewViewModel);

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.CREATE_REVIEW_REQUEST);
        message.setReview(reviewDTO);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Update an existing review
     */
    public CompletableFuture<ReviewViewModel> updateReview(String reviewId, ReviewViewModel reviewViewModel) {
        CompletableFuture<ReviewViewModel> future = new CompletableFuture<>();

        ReviewDTO reviewDTO = viewModelMapper.toDTO(reviewViewModel);

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.UPDATE_REVIEW_REQUEST);
        message.setReviewId(reviewId);
        message.setReview(reviewDTO);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get reviews received by a user
     */
    public CompletableFuture<List<ReviewDTO>> getReceivedReviews(Long userId) {
        CompletableFuture<List<ReviewDTO>> future = new CompletableFuture<>();

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.GET_USER_RECEIVED_REVIEWS_REQUEST);
        message.setUserId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get reviews given by a user
     */
    public CompletableFuture<List<ReviewDTO>> getGivenReviews(Long userId) {
        CompletableFuture<List<ReviewDTO>> future = new CompletableFuture<>();

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.GET_USER_GIVEN_REVIEWS_REQUEST);
        message.setUserId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get user rating summary
     */
    public CompletableFuture<UserRatingSummary> getUserRatingSummary(Long userId) {
        CompletableFuture<UserRatingSummary> future = new CompletableFuture<>();

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.GET_USER_RATING_SUMMARY_REQUEST);
        message.setUserId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get review for a specific offer
     */
    public CompletableFuture<ReviewViewModel> getOfferReview(String offerId) {
        CompletableFuture<ReviewViewModel> future = new CompletableFuture<>();

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.GET_OFFER_REVIEW_REQUEST);
        message.setOfferId(offerId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Delete a review
     */
    public CompletableFuture<Boolean> deleteReview(String reviewId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ReviewMessage message = new ReviewMessage();
        message.setType(ReviewMessage.Type.DELETE_REVIEW_REQUEST);
        message.setReviewId(reviewId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    // Handle incoming messages
    @SuppressWarnings("unchecked")
    private void handleReviewMessage(ReviewMessage message) {
        if (message.getType() == null) {
            System.err.println("Received review message with null type: " + message.getErrorMessage());
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case CREATE_REVIEW_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        ReviewViewModel reviewViewModel = viewModelMapper.toViewModel(message.getReview());
                        userGivenReviews.add(reviewViewModel);
                        if (futureToComplete != null) {
                            ((CompletableFuture<ReviewViewModel>) futureToComplete).complete(reviewViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to create review: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case UPDATE_REVIEW_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        ReviewViewModel updatedReview = viewModelMapper.toViewModel(message.getReview());
                        updateReviewInList(userGivenReviews, updatedReview);
                        updateReviewInList(userReceivedReviews, updatedReview);
                        if (futureToComplete != null) {
                            ((CompletableFuture<ReviewViewModel>) futureToComplete).complete(updatedReview);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to update review: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_USER_RECEIVED_REVIEWS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<ReviewDTO> reviews = message.getReviews() != null ? message.getReviews()
                                : new ArrayList<>();
                        List<ReviewViewModel> reviewViewModels = reviews.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userReceivedReviews.setAll(reviewViewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ReviewDTO>>) futureToComplete).complete(reviews);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get received reviews: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_USER_GIVEN_REVIEWS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<ReviewDTO> reviews = message.getReviews() != null ? message.getReviews()
                                : new ArrayList<>();
                        List<ReviewViewModel> reviewViewModels = reviews.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userGivenReviews.setAll(reviewViewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ReviewDTO>>) futureToComplete).complete(reviews);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get given reviews: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_USER_RATING_SUMMARY_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        UserRatingSummary summary = new UserRatingSummary(
                                message.getAverageRating(),
                                message.getTotalReviews());
                        if (futureToComplete != null) {
                            ((CompletableFuture<UserRatingSummary>) futureToComplete).complete(summary);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get rating summary: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_OFFER_REVIEW_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        ReviewViewModel reviewViewModel = message.getReview() != null
                                ? viewModelMapper.toViewModel(message.getReview())
                                : null;
                        if (futureToComplete != null) {
                            ((CompletableFuture<ReviewViewModel>) futureToComplete).complete(reviewViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get offer review: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case DELETE_REVIEW_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to delete review: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unknown review message type: " + message.getType());
                break;
        }

        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    private void updateReviewInList(ObservableList<ReviewViewModel> list, ReviewViewModel updatedReview) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedReview.getId())) {
                list.set(i, updatedReview);
                break;
            }
        }
    }

    // Getters for observable lists
    public ObservableList<ReviewViewModel> getUserReceivedReviewsList() {
        return userReceivedReviews;
    }

    public ObservableList<ReviewViewModel> getUserGivenReviewsList() {
        return userGivenReviews;
    }

    public void clearData() {
        userReceivedReviews.clear();
        userGivenReviews.clear();
    }

    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<ReviewMessage> callback) {
        this.messageCallback = callback;
    }

    // Inner class for rating summary
    public static class UserRatingSummary {
        private final Double averageRating;
        private final Integer totalReviews;

        public UserRatingSummary(Double averageRating, Integer totalReviews) {
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.totalReviews = totalReviews != null ? totalReviews : 0;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public Integer getTotalReviews() {
            return totalReviews;
        }

        public String getFormattedRating() {
            if (totalReviews > 0) {
                return String.format("⭐ %.1f (%d reviews)", averageRating, totalReviews);
            } else {
                return "No reviews yet";
            }
        }
    }
}