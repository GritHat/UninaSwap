package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FollowerViewModel {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty followerId = new SimpleLongProperty();
    private final LongProperty followedId = new SimpleLongProperty();
    private final ObjectProperty<UserViewModel> follower = new SimpleObjectProperty<>();
    private final ObjectProperty<UserViewModel> followed = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Default constructor
    public FollowerViewModel() {
    }

    // Constructor with required fields
    public FollowerViewModel(Long followerId, Long followedId) {
        setFollowerId(followerId);
        setFollowedId(followedId);
        setCreatedAt(LocalDateTime.now());
    }

    // Property getters
    public LongProperty idProperty() {
        return id;
    }

    public LongProperty followerIdProperty() {
        return followerId;
    }

    public LongProperty followedIdProperty() {
        return followedId;
    }

    public ObjectProperty<UserViewModel> followerProperty() {
        return follower;
    }

    public ObjectProperty<UserViewModel> followedProperty() {
        return followed;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public Long getFollowerId() {
        return followerId.get();
    }

    public void setFollowerId(Long followerId) {
        this.followerId.set(followerId);
    }

    public Long getFollowedId() {
        return followedId.get();
    }

    public void setFollowedId(Long followedId) {
        this.followedId.set(followedId);
    }

    public UserViewModel getFollower() {
        return follower.get();
    }

    public void setFollower(UserViewModel follower) {
        this.follower.set(follower);
    }

    public UserViewModel getFollowed() {
        return followed.get();
    }

    public void setFollowed(UserViewModel followed) {
        this.followed.set(followed);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    // Utility methods
    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    public String getFollowerName() {
        return follower.get() != null ? follower.get().getDisplayName() : "";
    }

    public String getFollowedName() {
        return followed.get() != null ? followed.get().getDisplayName() : "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        FollowerViewModel that = (FollowerViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("FollowerViewModel{id=%d, followerId=%d, followedId=%d}",
                getId(), getFollowerId(), getFollowedId());
    }
}