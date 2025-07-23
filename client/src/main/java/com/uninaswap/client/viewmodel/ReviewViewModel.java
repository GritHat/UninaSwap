package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 */
public class ReviewViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> reviewer = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> reviewedUser = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final StringProperty offerId = new SimpleStringProperty();
    /**
     * 
     */
    private final DoubleProperty score = new SimpleDoubleProperty();
    /**
     * 
     */
    private final StringProperty comment = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Default constructor
    /**
     * 
     */
    public ReviewViewModel() {
    }

    // Constructor with required fields
    /**
     * @param reviewer
     * @param reviewedUser
     * @param offerId
     * @param score
     * @param comment
     */
    public ReviewViewModel(UserViewModel reviewer, UserViewModel reviewedUser, String offerId, Double score,
            String comment) {
        setReviewer(reviewer);
        setReviewedUser(reviewedUser);
        setOfferId(offerId);
        setScore(score);
        setComment(comment);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    // Property getters
    /**
     * @return
     */
    public StringProperty idProperty() {
        return id;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> reviewerProperty() {
        return reviewer;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> reviewedUserProperty() {
        return reviewedUser;
    }

    /**
     * @return
     */
    public StringProperty offerIdProperty() {
        return offerId;
    }

    /**
     * @return
     */
    public DoubleProperty scoreProperty() {
        return score;
    }

    /**
     * @return
     */
    public StringProperty commentProperty() {
        return comment;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    // Getters and setters
    /**
     * @return
     */
    public String getId() {
        return id.get();
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id.set(id);
    }

    /**
     * @return
     */
    public UserViewModel getReviewer() {
        return reviewer.get();
    }

    /**
     * @param reviewer
     */
    public void setReviewer(UserViewModel reviewer) {
        this.reviewer.set(reviewer);
    }

    /**
     * @return
     */
    public UserViewModel getReviewedUser() {
        return reviewedUser.get();
    }

    /**
     * @param reviewedUser
     */
    public void setReviewedUser(UserViewModel reviewedUser) {
        this.reviewedUser.set(reviewedUser);
    }

    /**
     * @return
     */
    public String getOfferId() {
        return offerId.get();
    }

    /**
     * @param offerId
     */
    public void setOfferId(String offerId) {
        this.offerId.set(offerId);
    }

    /**
     * @return
     */
    public Double getScore() {
        return score.get();
    }

    /**
     * @param score
     */
    public void setScore(Double score) {
        this.score.set(score != null ? score : 0.0);
    }

    /**
     * @return
     */
    public String getComment() {
        return comment.get();
    }

    /**
     * @param comment
     */
    public void setComment(String comment) {
        this.comment.set(comment);
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    // Utility methods
    /**
     * @return
     */
    public String getFormattedScore() {
        return String.format("%.1f/5.0", getScore());
    }

    /**
     * @return
     */
    public String getStarRating() {
        int fullStars = (int) Math.floor(getScore());
        boolean hasHalfStar = (getScore() - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("☆");
        }
        for (int i = fullStars + (hasHalfStar ? 1 : 0); i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    /**
     * @return
     */
    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    /**
     * @return
     */
    public String getReviewerName() {
        return reviewer.get() != null ? reviewer.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public String getReviewedUserName() {
        return reviewedUser.get() != null ? reviewedUser.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public boolean hasComment() {
        return comment.get() != null && !comment.get().trim().isEmpty();
    }

    /**
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ReviewViewModel that = (ReviewViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    /**
     *
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return String.format("ReviewViewModel{id='%s', score=%.1f, reviewer='%s', reviewedUser='%s'}",
                getId(), getScore(), getReviewerName(), getReviewedUserName());
    }
}