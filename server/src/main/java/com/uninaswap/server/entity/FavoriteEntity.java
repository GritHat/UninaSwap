package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a user's favorite listing
 */
@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "listing_id" })
})
public class FavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    
    public FavoriteEntity() {
        this.createdAt = LocalDateTime.now();
    }

    
    public FavoriteEntity(UserEntity user, ListingEntity listing) {
        this();
        this.user = user;
        this.listing = listing;
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
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
        if (!(o instanceof FavoriteEntity))
            return false;
        FavoriteEntity that = (FavoriteEntity) o;
        return user != null && listing != null &&
                user.getId().equals(that.user.getId()) &&
                listing.getId().equals(that.listing.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}