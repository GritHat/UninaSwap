package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
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

/**
 * 
 */
public class ReviewService {
    /**
     * 
     */
    private static ReviewService instance;
    /**
     * 
     */
    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    /**
     * 
     */
    private CompletableFuture<?> futureToComplete;
    /**
     * 
     */
    private Consumer<ReviewMessage> messageCallback;

    /**
     * 
     */
    private final ObservableList<ReviewViewModel> userReceivedReviews = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<ReviewViewModel> userGivenReviews = FXCollections.observableArrayList();

    /**
     * 
     */
    private ReviewService() {
        this.webSocketClient.registerMessageHandler(ReviewMessage.class, this::handleReviewMessage);
    }

    /**
     * @return
     */
    public static synchronized ReviewService getInstance() {
        if (instance == null) {
            instance = new ReviewService();
        }
        return instance;
    }

    /**
     * Create a new review
     * 
     * @param reviewViewModel The ViewModel containing review details
     * @return CompletableFuture with the created ReviewViewModel
     *         or an exception if the review creation fails
     */
    /**
     * @param reviewViewModel
     * @return
     */
    public CompletableFuture<ReviewViewModel> createReview(ReviewViewModel reviewViewModel) {
        CompletableFuture<ReviewViewModel> future = new CompletableFuture<>();
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
     * 
     * @param reviewId The ID of the review to update
     * @param reviewViewModel The ViewModel containing updated review details
     * @return CompletableFuture with the updated ReviewViewModel
     *         or an exception if the update fails
     */
    /**
     * @param reviewId
     * @param reviewViewModel
     * @return
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
     * 
     * @param userId The ID of the user whose received reviews to fetch
     * @return CompletableFuture with a list of ReviewDTOs
     *         or an exception if the request fails
     */
    /**
     * @param userId
     * @return
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
     * 
     * @param userId The ID of the user whose given reviews to fetch
     * @return CompletableFuture with a list of ReviewDTOs
     *         or an exception if the request fails
     */
    /**
     * @param userId
     * @return
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
     * 
     * @param userId The ID of the user to get the rating summary for
     * @return CompletableFuture with UserRatingSummary
     *         or an exception if the request fails
     */
    /**
     * @param userId
     * @return
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
     * 
     * @param offerId The ID of the offer to get the review for
     * @return CompletableFuture with the ReviewViewModel
     *         or an exception if the request fails
     */
    /**
     * @param offerId
     * @return
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
     * 
     * @param reviewId The ID of the review to delete
     * @return CompletableFuture that completes with true if deletion was successful
     *         or an exception if the deletion fails
     */
    /**
     * @param reviewId
     * @return
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

    /**
     * @param message
     */
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
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    /**
     * @param list
     * @param updatedReview
     */
    private void updateReviewInList(ObservableList<ReviewViewModel> list, ReviewViewModel updatedReview) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedReview.getId())) {
                list.set(i, updatedReview);
                break;
            }
        }
    }

    /**
     * @return
     */
    public ObservableList<ReviewViewModel> getUserReceivedReviewsList() {
        return userReceivedReviews;
    }

    /**
     * @return
     */
    public ObservableList<ReviewViewModel> getUserGivenReviewsList() {
        return userGivenReviews;
    }

    /**
     * 
     */
    public void clearData() {
        userReceivedReviews.clear();
        userGivenReviews.clear();
    }

    /**
     * @param callback
     */
    public void setMessageCallback(Consumer<ReviewMessage> callback) {
        this.messageCallback = callback;
    }

    /**
     * 
     */
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