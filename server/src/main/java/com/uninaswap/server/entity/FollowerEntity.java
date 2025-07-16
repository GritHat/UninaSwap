package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a follower relationship between users
 */
@Entity
@Table(name = "followers", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "follower_id", "followed_id" })
})
public class FollowerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserEntity follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private UserEntity followed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public FollowerEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor
    public FollowerEntity(UserEntity follower, UserEntity followed) {
        this();
        this.follower = follower;
        this.followed = followed;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getFollower() {
        return follower;
    }

    public void setFollower(UserEntity follower) {
        this.follower = follower;
    }

    public UserEntity getFollowed() {
        return followed;
    }

    public void setFollowed(UserEntity followed) {
        this.followed = followed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FollowerEntity))
            return false;
        FollowerEntity that = (FollowerEntity) o;
        return follower != null && followed != null &&
                follower.getId().equals(that.follower.getId()) &&
                followed.getId().equals(that.followed.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}