package com.uninaswap.common.dto;

import com.uninaswap.common.enums.UserReportReason;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 */
public class UserReportDTO implements Serializable {
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
    private UserDTO reportingUser;
    /**
     * 
     */
    private UserDTO reportedUser;
    /**
     * 
     */
    private UserReportReason reason;
    /**
     * 
     */
    private String description;
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
    private boolean reviewed;
    /**
     * 
     */
    private LocalDateTime reviewedAt;
    /**
     * 
     */
    private UserDTO reviewedByAdmin;
    /**
     * 
     */
    private String adminNotes;

    // Default constructor
    /**
     * 
     */
    public UserReportDTO() {
    }

    // Constructor with required fields
    /**
     * @param reportingUser
     * @param reportedUser
     * @param reason
     * @param description
     */
    public UserReportDTO(UserDTO reportingUser, UserDTO reportedUser, UserReportReason reason, String description) {
        this.reportingUser = reportingUser;
        this.reportedUser = reportedUser;
        this.reason = reason;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
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
    public UserDTO getReportingUser() {
        return reportingUser;
    }

    /**
     * @param reportingUser
     */
    public void setReportingUser(UserDTO reportingUser) {
        this.reportingUser = reportingUser;
    }

    /**
     * @return
     */
    public UserDTO getReportedUser() {
        return reportedUser;
    }

    /**
     * @param reportedUser
     */
    public void setReportedUser(UserDTO reportedUser) {
        this.reportedUser = reportedUser;
    }

    /**
     * @return
     */
    public UserReportReason getReason() {
        return reason;
    }

    /**
     * @param reason
     */
    public void setReason(UserReportReason reason) {
        this.reason = reason;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * @return
     */
    public boolean isReviewed() {
        return reviewed;
    }

    /**
     * @param reviewed
     */
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    /**
     * @return
     */
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    /**
     * @param reviewedAt
     */
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    /**
     * @return
     */
    public UserDTO getReviewedByAdmin() {
        return reviewedByAdmin;
    }

    /**
     * @param reviewedByAdmin
     */
    public void setReviewedByAdmin(UserDTO reviewedByAdmin) {
        this.reviewedByAdmin = reviewedByAdmin;
    }

    /**
     * @return
     */
    public String getAdminNotes() {
        return adminNotes;
    }

    /**
     * @param adminNotes
     */
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}