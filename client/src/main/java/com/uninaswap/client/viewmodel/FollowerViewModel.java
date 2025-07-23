package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 */
public class FollowerViewModel {
    /**
     * 
     */
    private final LongProperty id = new SimpleLongProperty();
    /**
     * 
     */
    private final LongProperty followerId = new SimpleLongProperty();
    /**
     * 
     */
    private final LongProperty followedId = new SimpleLongProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> follower = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> followed = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    
    /**
     * 
     */
    public FollowerViewModel() {
    }

    
    /**
     * @param followerId
     * @param followedId
     */
    public FollowerViewModel(Long followerId, Long followedId) {
        setFollowerId(followerId);
        setFollowedId(followedId);
        setCreatedAt(LocalDateTime.now());
    }

    
    /**
     * @return
     */
    public LongProperty idProperty() {
        return id;
    }

    /**
     * @return
     */
    public LongProperty followerIdProperty() {
        return followerId;
    }

    /**
     * @return
     */
    public LongProperty followedIdProperty() {
        return followedId;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> followerProperty() {
        return follower;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> followedProperty() {
        return followed;
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
    public Long getId() {
        return id.get();
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id.set(id);
    }

    /**
     * @return
     */
    public Long getFollowerId() {
        return followerId.get();
    }

    /**
     * @param followerId
     */
    public void setFollowerId(Long followerId) {
        this.followerId.set(followerId);
    }

    /**
     * @return
     */
    public Long getFollowedId() {
        return followedId.get();
    }

    /**
     * @param followedId
     */
    public void setFollowedId(Long followedId) {
        this.followedId.set(followedId);
    }

    /**
     * @return
     */
    public UserViewModel getFollower() {
        return follower.get();
    }

    /**
     * @param follower
     */
    public void setFollower(UserViewModel follower) {
        this.follower.set(follower);
    }

    /**
     * @return
     */
    public UserViewModel getFollowed() {
        return followed.get();
    }

    /**
     * @param followed
     */
    public void setFollowed(UserViewModel followed) {
        this.followed.set(followed);
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
    public String getFollowerName() {
        return follower.get() != null ? follower.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public String getFollowedName() {
        return followed.get() != null ? followed.get().getDisplayName() : "";
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

        FollowerViewModel that = (FollowerViewModel) obj;
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
        return String.format("FollowerViewModel{id=%d, followerId=%d, followedId=%d}",
                getId(), getFollowerId(), getFollowedId());
    }
}