package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * DTO representing a user's follower relationship
 */
/**
 * 
 */
public class FollowerDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private Long id;
    /**
     * 
     */
    private Long followerId;
    /**
     * 
     */
    private Long followedId;
    /**
     * 
     */
    private UserDTO follower;
    /**
     * 
     */
    private UserDTO followed;
    /**
     * 
     */
    private LocalDateTime createdAt;

    // Default constructor
    /**
     * 
     */
    public FollowerDTO() {
    }

    // Constructor
    /**
     * @param followerId
     * @param followedId
     */
    public FollowerDTO(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    /**
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return
     */
    public Long getFollowerId() {
        return followerId;
    }

    /**
     * @param followerId
     */
    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    /**
     * @return
     */
    public Long getFollowedId() {
        return followedId;
    }

    /**
     * @param followedId
     */
    public void setFollowedId(Long followedId) {
        this.followedId = followedId;
    }

    /**
     * @return
     */
    public UserDTO getFollower() {
        return follower;
    }

    /**
     * @param follower
     */
    public void setFollower(UserDTO follower) {
        this.follower = follower;
    }

    /**
     * @return
     */
    public UserDTO getFollowed() {
        return followed;
    }

    /**
     * @param followed
     */
    public void setFollowed(UserDTO followed) {
        this.followed = followed;
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
}