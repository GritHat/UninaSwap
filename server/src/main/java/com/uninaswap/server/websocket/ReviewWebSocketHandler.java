package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.ReviewDTO;
import com.uninaswap.common.message.ReviewMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.ReviewService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Optional;

@Component
public class ReviewWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReviewWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;
    private final SessionService sessionService;

    @Autowired
    public ReviewWebSocketHandler(ObjectMapper objectMapper, ReviewService reviewService,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.reviewService = reviewService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received review message: {}", message.getPayload());

        try {
            ReviewMessage reviewMessage = objectMapper.readValue(message.getPayload(), ReviewMessage.class);
            ReviewMessage response = new ReviewMessage();

            try {
                
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }

                switch (reviewMessage.getType()) {
                    case CREATE_REVIEW_REQUEST:
                        handleCreateReview(reviewMessage, response, currentUser);
                        break;

                    case UPDATE_REVIEW_REQUEST:
                        handleUpdateReview(reviewMessage, response, currentUser);
                        break;

                    case GET_REVIEW_REQUEST:
                        handleGetReview(reviewMessage, response, currentUser);
                        break;

                    case GET_USER_RECEIVED_REVIEWS_REQUEST:
                        handleGetReceivedReviews(reviewMessage, response, currentUser);
                        break;

                    case GET_USER_GIVEN_REVIEWS_REQUEST:
                        handleGetGivenReviews(reviewMessage, response, currentUser);
                        break;

                    case GET_USER_RATING_SUMMARY_REQUEST:
                        handleGetRatingSummary(reviewMessage, response, currentUser);
                        break;

                    case GET_OFFER_REVIEW_REQUEST:
                        handleGetOfferReview(reviewMessage, response, currentUser);
                        break;

                    case DELETE_REVIEW_REQUEST:
                        handleDeleteReview(reviewMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown review message type: " + reviewMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for review operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing review request: " + e.getMessage());
                logger.error("Error processing review message", e);
            }

            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing review message", e);
            ReviewMessage errorResponse = new ReviewMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleCreateReview(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            ReviewDTO newReview = request.getReview();
            ReviewDTO createdReview = reviewService.createReview(newReview, currentUser.getId());

            response.setType(ReviewMessage.Type.CREATE_REVIEW_RESPONSE);
            response.setReview(createdReview);
            response.setSuccess(true);

            logger.info("Created review {} for offer {} by user {}",
                    createdReview.getId(), createdReview.getOfferId(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.CREATE_REVIEW_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to create review: " + e.getMessage());
            logger.error("Failed to create review: {}", e.getMessage());
        }
    }

    private void handleUpdateReview(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            String reviewId = request.getReviewId();
            ReviewDTO reviewData = request.getReview();

            ReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewData, currentUser.getId());

            response.setType(ReviewMessage.Type.UPDATE_REVIEW_RESPONSE);
            response.setReview(updatedReview);
            response.setSuccess(true);

            logger.info("Updated review {} by user {}", reviewId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.UPDATE_REVIEW_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to update review: " + e.getMessage());
            logger.error("Failed to update review {}: {}", request.getReviewId(), e.getMessage());
        }
    }

    private void handleGetReview(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            String reviewId = request.getReviewId();
            ReviewDTO review = reviewService.getReviewById(reviewId);

            response.setType(ReviewMessage.Type.GET_REVIEW_RESPONSE);
            response.setReview(review);
            response.setSuccess(true);

            logger.info("Retrieved review {} for user {}", reviewId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.GET_REVIEW_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get review: " + e.getMessage());
            logger.error("Failed to get review {}: {}", request.getReviewId(), e.getMessage());
        }
    }

    private void handleGetReceivedReviews(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            Long userId = request.getUserId() != null ? request.getUserId() : currentUser.getId();

            if (request.getSize() > 0) {
                
                Page<ReviewDTO> reviewPage = reviewService.getReceivedReviews(userId, request.getPage(),
                        request.getSize());
                response.setReviews(reviewPage.getContent());
                response.setTotalElements(reviewPage.getTotalElements());
                response.setTotalPages(reviewPage.getTotalPages());
                response.setPage(request.getPage());
                response.setSize(request.getSize());
            } else {
                
                List<ReviewDTO> reviews = reviewService.getReceivedReviews(userId);
                response.setReviews(reviews);
            }

            response.setType(ReviewMessage.Type.GET_USER_RECEIVED_REVIEWS_RESPONSE);
            response.setUserId(userId);
            response.setSuccess(true);

            logger.info("Retrieved {} received reviews for user {}", response.getReviews().size(), userId);
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.GET_USER_RECEIVED_REVIEWS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get received reviews: " + e.getMessage());
            logger.error("Failed to get received reviews: {}", e.getMessage());
        }
    }

    private void handleGetGivenReviews(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            Long userId = request.getUserId() != null ? request.getUserId() : currentUser.getId();

            if (request.getSize() > 0) {
                
                Page<ReviewDTO> reviewPage = reviewService.getGivenReviews(userId, request.getPage(),
                        request.getSize());
                response.setReviews(reviewPage.getContent());
                response.setTotalElements(reviewPage.getTotalElements());
                response.setTotalPages(reviewPage.getTotalPages());
                response.setPage(request.getPage());
                response.setSize(request.getSize());
            } else {
                
                List<ReviewDTO> reviews = reviewService.getGivenReviews(userId);
                response.setReviews(reviews);
            }

            response.setType(ReviewMessage.Type.GET_USER_GIVEN_REVIEWS_RESPONSE);
            response.setUserId(userId);
            response.setSuccess(true);

            logger.info("Retrieved {} given reviews for user {}", response.getReviews().size(), userId);
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.GET_USER_GIVEN_REVIEWS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get given reviews: " + e.getMessage());
            logger.error("Failed to get given reviews: {}", e.getMessage());
        }
    }

    private void handleGetRatingSummary(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            Long userId = request.getUserId() != null ? request.getUserId() : currentUser.getId();

            ReviewService.UserRatingSummary summary = reviewService.getUserRatingSummary(userId);

            response.setType(ReviewMessage.Type.GET_USER_RATING_SUMMARY_RESPONSE);
            response.setUserId(userId);
            response.setAverageRating(summary.getAverageRating());
            response.setTotalReviews(summary.getTotalReviews());
            response.setSuccess(true);

            logger.info("Retrieved rating summary for user {}: avg={}, count={}",
                    userId, summary.getAverageRating(), summary.getTotalReviews());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.GET_USER_RATING_SUMMARY_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get rating summary: " + e.getMessage());
            logger.error("Failed to get rating summary: {}", e.getMessage());
        }
    }

    private void handleGetOfferReview(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            String offerId = request.getOfferId();
            Optional<ReviewDTO> review = reviewService.getReviewForOffer(offerId);

            response.setType(ReviewMessage.Type.GET_OFFER_REVIEW_RESPONSE);
            response.setOfferId(offerId);
            response.setReview(review.orElse(null));
            response.setSuccess(true);

            logger.info("Retrieved review for offer {} by user {}", offerId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.GET_OFFER_REVIEW_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get offer review: " + e.getMessage());
            logger.error("Failed to get review for offer {}: {}", request.getOfferId(), e.getMessage());
        }
    }

    private void handleDeleteReview(ReviewMessage request, ReviewMessage response, UserEntity currentUser) {
        try {
            String reviewId = request.getReviewId();
            reviewService.deleteReview(reviewId, currentUser.getId());

            response.setType(ReviewMessage.Type.DELETE_REVIEW_RESPONSE);
            response.setSuccess(true);

            logger.info("Deleted review {} by user {}", reviewId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReviewMessage.Type.DELETE_REVIEW_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to delete review: " + e.getMessage());
            logger.error("Failed to delete review {}: {}", request.getReviewId(), e.getMessage());
        }
    }
}