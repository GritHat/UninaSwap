package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.UserReportReason;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 */
public class UserReportViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> reportingUser = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> reportedUser = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserReportReason> reason = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final StringProperty description = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final BooleanProperty reviewed = new SimpleBooleanProperty();
    /**
     * 
     */
    private final StringProperty adminNotes = new SimpleStringProperty();

    // Default constructor
    /**
     * 
     */
    public UserReportViewModel() {
    }

    // Constructor with required fields
    /**
     * @param reportingUser
     * @param reportedUser
     * @param reason
     * @param description
     */
    public UserReportViewModel(UserViewModel reportingUser, UserViewModel reportedUser, UserReportReason reason,
            String description) {
        setReportingUser(reportingUser);
        setReportedUser(reportedUser);
        setReason(reason);
        setDescription(description);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
        setReviewed(false);
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
    public ObjectProperty<UserViewModel> reportingUserProperty() {
        return reportingUser;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> reportedUserProperty() {
        return reportedUser;
    }

    /**
     * @return
     */
    public ObjectProperty<UserReportReason> reasonProperty() {
        return reason;
    }

    /**
     * @return
     */
    public StringProperty descriptionProperty() {
        return description;
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

    /**
     * @return
     */
    public BooleanProperty reviewedProperty() {
        return reviewed;
    }

    /**
     * @return
     */
    public StringProperty adminNotesProperty() {
        return adminNotes;
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
    public UserViewModel getReportingUser() {
        return reportingUser.get();
    }

    /**
     * @param reportingUser
     */
    public void setReportingUser(UserViewModel reportingUser) {
        this.reportingUser.set(reportingUser);
    }

    /**
     * @return
     */
    public UserViewModel getReportedUser() {
        return reportedUser.get();
    }

    /**
     * @param reportedUser
     */
    public void setReportedUser(UserViewModel reportedUser) {
        this.reportedUser.set(reportedUser);
    }

    /**
     * @return
     */
    public UserReportReason getReason() {
        return reason.get();
    }

    /**
     * @param reason
     */
    public void setReason(UserReportReason reason) {
        this.reason.set(reason);
    }

    /**
     * @return
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description.set(description);
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

    /**
     * @return
     */
    public boolean isReviewed() {
        return reviewed.get();
    }

    /**
     * @param reviewed
     */
    public void setReviewed(boolean reviewed) {
        this.reviewed.set(reviewed);
    }

    /**
     * @return
     */
    public String getAdminNotes() {
        return adminNotes.get();
    }

    /**
     * @param adminNotes
     */
    public void setAdminNotes(String adminNotes) {
        this.adminNotes.set(adminNotes);
    }

    // Utility methods
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
    public String getReportingUserName() {
        return reportingUser.get() != null ? reportingUser.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public String getReportedUserName() {
        return reportedUser.get() != null ? reportedUser.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public boolean hasDescription() {
        return description.get() != null && !description.get().trim().isEmpty();
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

        UserReportViewModel that = (UserReportViewModel) obj;
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
        return String.format("UserReportViewModel{id='%s', reason=%s, reportingUser='%s', reportedUser='%s'}",
                getId(), getReason(), getReportingUserName(), getReportedUserName());
    }
}