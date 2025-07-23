package com.uninaswap.common.message;

import com.uninaswap.common.dto.ReviewDTO;

import java.util.List;

/**
 * 
 */
public class ReviewMessage extends Message {

    /**
     * 
     */
    public enum Type {
        // Requests
        CREATE_REVIEW_REQUEST,
        UPDATE_REVIEW_REQUEST,
        GET_REVIEW_REQUEST,
        GET_USER_REVIEWS_REQUEST,
        GET_USER_GIVEN_REVIEWS_REQUEST,
        GET_USER_RECEIVED_REVIEWS_REQUEST,
        GET_USER_RATING_SUMMARY_REQUEST,
        DELETE_REVIEW_REQUEST,
        GET_OFFER_REVIEW_REQUEST,

        // Responses
        CREATE_REVIEW_RESPONSE,
        UPDATE_REVIEW_RESPONSE,
        GET_REVIEW_RESPONSE,
        GET_USER_REVIEWS_RESPONSE,
        GET_USER_GIVEN_REVIEWS_RESPONSE,
        GET_USER_RECEIVED_REVIEWS_RESPONSE,
        GET_USER_RATING_SUMMARY_RESPONSE,
        DELETE_REVIEW_RESPONSE,
        GET_OFFER_REVIEW_RESPONSE,
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private String reviewId;
    /**
     * 
     */
    private String offerId;
    /**
     * 
     */
    private Long userId;
    /**
     * 
     */
    private ReviewDTO review;
    /**
     * 
     */
    private List<ReviewDTO> reviews;
    /**
     * 
     */
    private Double averageRating;
    /**
     * 
     */
    private Integer totalReviews;

    // For pagination
    /**
     * 
     */
    private int page = 0;
    /**
     * 
     */
    private int size = 20;
    /**
     * 
     */
    private long totalElements = 0;
    /**
     * 
     */
    private int totalPages = 0;

    // Default constructor
    /**
     * 
     */
    public ReviewMessage() {
        super();
        setMessageType("review");
    }

    // Getters and setters
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public String getReviewId() {
        return reviewId;
    }

    /**
     * @param reviewId
     */
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * @return
     */
    public String getOfferId() {
        return offerId;
    }

    /**
     * @param offerId
     */
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    /**
     * @return
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return
     */
    public ReviewDTO getReview() {
        return review;
    }

    /**
     * @param review
     */
    public void setReview(ReviewDTO review) {
        this.review = review;
    }

    /**
     * @return
     */
    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    /**
     * @param reviews
     */
    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    /**
     * @return
     */
    public Double getAverageRating() {
        return averageRating;
    }

    /**
     * @param averageRating
     */
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * @return
     */
    public Integer getTotalReviews() {
        return totalReviews;
    }

    /**
     * @param totalReviews
     */
    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}