package com.uninaswap.server.entity;

import com.uninaswap.common.enums.ListingReportReason;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a listing report (user reporting a listing)
 */
@Entity
@Table(name = "listing_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "reporting_user_id", "reported_listing_id" })
})
public class ListingReportEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporting_user_id", nullable = false)
    private UserEntity reportingUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_listing_id", nullable = false)
    private ListingEntity reportedListing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingReportReason reason;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // For admin tracking
    @Column(nullable = false)
    private boolean reviewed = false;

    @Column
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private UserEntity reviewedByAdmin;

    @Column(length = 500)
    private String adminNotes;

    // Default constructor
    public ListingReportEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public ListingReportEntity(UserEntity reportingUser, ListingEntity reportedListing, ListingReportReason reason,
            String description) {
        this();
        this.reportingUser = reportingUser;
        this.reportedListing = reportedListing;
        this.reason = reason;
        this.description = description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public UserEntity getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(UserEntity reportingUser) {
        this.reportingUser = reportingUser;
    }

    public ListingEntity getReportedListing() {
        return reportedListing;
    }

    public void setReportedListing(ListingEntity reportedListing) {
        this.reportedListing = reportedListing;
    }

    public ListingReportReason getReason() {
        return reason;
    }

    public void setReason(ListingReportReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public UserEntity getReviewedByAdmin() {
        return reviewedByAdmin;
    }

    public void setReviewedByAdmin(UserEntity reviewedByAdmin) {
        this.reviewedByAdmin = reviewedByAdmin;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}