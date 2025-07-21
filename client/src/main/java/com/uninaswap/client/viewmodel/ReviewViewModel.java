package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReviewViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final ObjectProperty<UserViewModel> reviewer = new SimpleObjectProperty<>();
    private final ObjectProperty<UserViewModel> reviewedUser = new SimpleObjectProperty<>();
    private final StringProperty offerId = new SimpleStringProperty();
    private final DoubleProperty score = new SimpleDoubleProperty();
    private final StringProperty comment = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Default constructor
    public ReviewViewModel() {
    }

    // Constructor with required fields
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
    public StringProperty idProperty() {
        return id;
    }

    public ObjectProperty<UserViewModel> reviewerProperty() {
        return reviewer;
    }

    public ObjectProperty<UserViewModel> reviewedUserProperty() {
        return reviewedUser;
    }

    public StringProperty offerIdProperty() {
        return offerId;
    }

    public DoubleProperty scoreProperty() {
        return score;
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    // Getters and setters
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public UserViewModel getReviewer() {
        return reviewer.get();
    }

    public void setReviewer(UserViewModel reviewer) {
        this.reviewer.set(reviewer);
    }

    public UserViewModel getReviewedUser() {
        return reviewedUser.get();
    }

    public void setReviewedUser(UserViewModel reviewedUser) {
        this.reviewedUser.set(reviewedUser);
    }

    public String getOfferId() {
        return offerId.get();
    }

    public void setOfferId(String offerId) {
        this.offerId.set(offerId);
    }

    public Double getScore() {
        return score.get();
    }

    public void setScore(Double score) {
        this.score.set(score != null ? score : 0.0);
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    // Utility methods
    public String getFormattedScore() {
        return String.format("%.1f/5.0", getScore());
    }

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

    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    public String getReviewerName() {
        return reviewer.get() != null ? reviewer.get().getDisplayName() : "";
    }

    public String getReviewedUserName() {
        return reviewedUser.get() != null ? reviewedUser.get().getDisplayName() : "";
    }

    public boolean hasComment() {
        return comment.get() != null && !comment.get().trim().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ReviewViewModel that = (ReviewViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("ReviewViewModel{id='%s', score=%.1f, reviewer='%s', reviewedUser='%s'}",
                getId(), getScore(), getReviewerName(), getReviewedUserName());
    }
}