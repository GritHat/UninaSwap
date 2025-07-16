package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private UserDTO reviewer;
    private UserDTO reviewedUser;
    private String offerId;
    private Double score; // 0.0 to 5.0
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ReviewDTO() {
    }

    // Constructor with required fields
    public ReviewDTO(UserDTO reviewer, UserDTO reviewedUser, String offerId, Double score, String comment) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.offerId = offerId;
        this.score = score;
        this.comment = comment;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserDTO getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserDTO reviewer) {
        this.reviewer = reviewer;
    }

    public UserDTO getReviewedUser() {
        return reviewedUser;
    }

    public void setReviewedUser(UserDTO reviewedUser) {
        this.reviewedUser = reviewedUser;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("ReviewDTO{id='%s', score=%.1f, reviewer='%s', reviewedUser='%s'}",
                id, score,
                reviewer != null ? reviewer.getUsername() : null,
                reviewedUser != null ? reviewedUser.getUsername() : null);
    }
}