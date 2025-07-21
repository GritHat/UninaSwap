package com.uninaswap.common.message;

import com.uninaswap.common.dto.ReviewDTO;

import java.util.List;

public class ReviewMessage extends Message {

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

    private Type type;
    private String reviewId;
    private String offerId;
    private Long userId;
    private ReviewDTO review;
    private List<ReviewDTO> reviews;
    private Double averageRating;
    private Integer totalReviews;

    // For pagination
    private int page = 0;
    private int size = 20;
    private long totalElements = 0;
    private int totalPages = 0;

    // Default constructor
    public ReviewMessage() {
        super();
        setMessageType("review");
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReviewDTO getReview() {
        return review;
    }

    public void setReview(ReviewDTO review) {
        this.review = review;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}