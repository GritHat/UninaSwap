package com.uninaswap.server.service;

import com.uninaswap.common.dto.ReviewDTO;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.server.entity.OfferEntity;
import com.uninaswap.server.entity.ReviewEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.mapper.ReviewMapper;
import com.uninaswap.server.repository.OfferRepository;
import com.uninaswap.server.repository.ReviewRepository;
import com.uninaswap.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    /**
     * Create a new review
     */
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO, Long reviewerId) {
        logger.info("Creating review for offer {} by user {}", reviewDTO.getOfferId(), reviewerId);

        // Validate reviewer
        UserEntity reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));

        // Validate offer
        OfferEntity offer = offerRepository.findById(reviewDTO.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + reviewDTO.getOfferId()));

        // Validate offer is completed
        if (offer.getStatus() != OfferStatus.COMPLETED) {
            throw new IllegalArgumentException("Can only review completed offers");
        }

        // Determine who should be reviewed
        UserEntity reviewedUser;
        if (offer.getUser().getId().equals(reviewerId)) {
            // Reviewer is the one who made the offer, so review the listing creator
            reviewedUser = offer.getListing().getCreator();
        } else if (offer.getListing().getCreator().getId().equals(reviewerId)) {
            // Reviewer is the listing creator, so review the offer maker
            reviewedUser = offer.getUser();
        } else {
            throw new IllegalArgumentException("User is not involved in this offer");
        }

        // Check if review already exists
        Optional<ReviewEntity> existingReview = reviewRepository.findByOfferIdAndReviewerId(
                reviewDTO.getOfferId(), reviewerId);
        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("Review already exists for this offer");
        }

        // Validate score
        if (reviewDTO.getScore() < 0.0 || reviewDTO.getScore() > 5.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 5.0");
        }

        // Create review entity
        ReviewEntity review = new ReviewEntity();
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);
        review.setOffer(offer);
        review.setScore(reviewDTO.getScore());
        review.setComment(reviewDTO.getComment());

        ReviewEntity savedReview = reviewRepository.save(review);

        logger.info("Successfully created review {} for user {} by user {}",
                savedReview.getId(), reviewedUser.getUsername(), reviewer.getUsername());

        return reviewMapper.toDto(savedReview);
    }

    /**
     * Update an existing review
     */
    @Transactional
    public ReviewDTO updateReview(String reviewId, ReviewDTO reviewDTO, Long userId) {
        logger.info("Updating review {} by user {}", reviewId, userId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        // Verify user owns the review
        if (!review.getReviewer().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this review");
        }

        // Validate score
        if (reviewDTO.getScore() < 0.0 || reviewDTO.getScore() > 5.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 5.0");
        }

        // Update fields
        review.setScore(reviewDTO.getScore());
        review.setComment(reviewDTO.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        ReviewEntity updatedReview = reviewRepository.save(review);

        logger.info("Successfully updated review {}", reviewId);
        return reviewMapper.toDto(updatedReview);
    }

    /**
     * Get review by ID
     */
    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(String reviewId) {
        logger.info("Getting review by ID: {}", reviewId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        return reviewMapper.toDto(review);
    }

    /**
     * Get all reviews received by a user
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReceivedReviews(Long userId) {
        logger.info("Getting received reviews for user: {}", userId);

        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Get all reviews given by a user
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getGivenReviews(Long userId) {
        logger.info("Getting given reviews for user: {}", userId);

        List<ReviewEntity> reviews = reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Get user rating summary
     */
    @Transactional(readOnly = true)
    public UserRatingSummary getUserRatingSummary(Long userId) {
        logger.info("Getting rating summary for user: {}", userId);

        Double averageRating = reviewRepository.getAverageRatingForUser(userId).orElse(0.0);
        Long totalReviews = reviewRepository.getTotalReviewsForUser(userId);

        return new UserRatingSummary(averageRating, totalReviews.intValue());
    }

    /**
     * Get review for a specific offer
     */
    @Transactional(readOnly = true)
    public Optional<ReviewDTO> getReviewForOffer(String offerId) {
        logger.info("Getting review for offer: {}", offerId);

        Optional<ReviewEntity> review = reviewRepository.findByOfferId(offerId);
        return review.map(reviewMapper::toDto);
    }

    /**
     * Check if user can review for a specific offer
     */
    @Transactional(readOnly = true)
    public boolean canUserReviewOffer(String offerId, Long userId) {
        logger.info("Checking if user {} can review offer {}", userId, offerId);

        // Check if offer exists and is completed
        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent() || offerOpt.get().getStatus() != OfferStatus.COMPLETED) {
            return false;
        }

        OfferEntity offer = offerOpt.get();

        // Check if user is involved in the offer
        boolean isInvolved = offer.getUser().getId().equals(userId) ||
                offer.getListing().getCreator().getId().equals(userId);
        if (!isInvolved) {
            return false;
        }

        // Check if review already exists
        Optional<ReviewEntity> existingReview = reviewRepository.findByOfferIdAndReviewerId(offerId, userId);
        return !existingReview.isPresent();
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(String reviewId, Long userId) {
        logger.info("Deleting review {} by user {}", reviewId, userId);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        // Verify user owns the review
        if (!review.getReviewer().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this review");
        }

        reviewRepository.delete(review);
        logger.info("Successfully deleted review {}", reviewId);
    }

    /**
     * Get received reviews with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReceivedReviews(Long userId, int page, int size) {
        logger.info("Getting received reviews for user {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewEntity> reviewPage = reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId, pageable);

        return reviewPage.map(reviewMapper::toDto);
    }

    /**
     * Get given reviews with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getGivenReviews(Long userId, int page, int size) {
        logger.info("Getting given reviews for user {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewEntity> reviewPage = reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId, pageable);

        return reviewPage.map(reviewMapper::toDto);
    }

    /**
     * Inner class for rating summary
     */
    public static class UserRatingSummary {
        private final Double averageRating;
        private final Integer totalReviews;

        public UserRatingSummary(Double averageRating, Integer totalReviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public Integer getTotalReviews() {
            return totalReviews;
        }
    }
}