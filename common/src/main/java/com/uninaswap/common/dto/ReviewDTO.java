package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 */
public class ReviewDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String id;
    /**
     * 
     */
    private UserDTO reviewer;
    /**
     * 
     */
    private UserDTO reviewedUser;
    /**
     * 
     */
    private String offerId;
    /**
     * 
     */
    private Double score; 
    /**
     * 
     */
    private String comment;
    /**
     * 
     */
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime updatedAt;

    
    /**
     * 
     */
    public ReviewDTO() {
    }

    
    /**
     * @param reviewer
     * @param reviewedUser
     * @param offerId
     * @param score
     * @param comment
     */
    public ReviewDTO(UserDTO reviewer, UserDTO reviewedUser, String offerId, Double score, String comment) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.offerId = offerId;
        this.score = score;
        this.comment = comment;
    }

    
    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     */
    public UserDTO getReviewer() {
        return reviewer;
    }

    /**
     * @param reviewer
     */
    public void setReviewer(UserDTO reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * @return
     */
    public UserDTO getReviewedUser() {
        return reviewedUser;
    }

    /**
     * @param reviewedUser
     */
    public void setReviewedUser(UserDTO reviewedUser) {
        this.reviewedUser = reviewedUser;
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
    public Double getScore() {
        return score;
    }

    /**
     * @param score
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * @return
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return String.format("ReviewDTO{id='%s', score=%.1f, reviewer='%s', reviewedUser='%s'}",
                id, score,
                reviewer != null ? reviewer.getUsername() : null,
                reviewedUser != null ? reviewedUser.getUsername() : null);
    }
}