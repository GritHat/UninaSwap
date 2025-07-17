package com.uninaswap.common.dto;

import com.uninaswap.common.enums.ListingReportReason;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ListingReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private UserDTO reportingUser;
    private ListingDTO reportedListing;
    private ListingReportReason reason;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean reviewed;
    private LocalDateTime reviewedAt;
    private UserDTO reviewedByAdmin;
    private String adminNotes;

    // Default constructor
    public ListingReportDTO() {
    }

    // Constructor with required fields
    public ListingReportDTO(UserDTO reportingUser, ListingDTO reportedListing, ListingReportReason reason,
            String description) {
        this.reportingUser = reportingUser;
        this.reportedListing = reportedListing;
        this.reason = reason;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserDTO getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(UserDTO reportingUser) {
        this.reportingUser = reportingUser;
    }

    public ListingDTO getReportedListing() {
        return reportedListing;
    }

    public void setReportedListing(ListingDTO reportedListing) {
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

    public UserDTO getReviewedByAdmin() {
        return reviewedByAdmin;
    }

    public void setReviewedByAdmin(UserDTO reviewedByAdmin) {
        this.reviewedByAdmin = reviewedByAdmin;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}