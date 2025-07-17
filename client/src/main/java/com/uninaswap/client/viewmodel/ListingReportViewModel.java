package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.ListingReportReason;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ListingReportViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final ObjectProperty<UserViewModel> reportingUser = new SimpleObjectProperty<>();
    private final ObjectProperty<ListingViewModel> reportedListing = new SimpleObjectProperty<>();
    private final ObjectProperty<ListingReportReason> reason = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final BooleanProperty reviewed = new SimpleBooleanProperty();
    private final StringProperty adminNotes = new SimpleStringProperty();

    // Default constructor
    public ListingReportViewModel() {
    }

    // Constructor with required fields
    public ListingReportViewModel(UserViewModel reportingUser, ListingViewModel reportedListing,
            ListingReportReason reason, String description) {
        setReportingUser(reportingUser);
        setReportedListing(reportedListing);
        setReason(reason);
        setDescription(description);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
        setReviewed(false);
    }

    // Property getters
    public StringProperty idProperty() {
        return id;
    }

    public ObjectProperty<UserViewModel> reportingUserProperty() {
        return reportingUser;
    }

    public ObjectProperty<ListingViewModel> reportedListingProperty() {
        return reportedListing;
    }

    public ObjectProperty<ListingReportReason> reasonProperty() {
        return reason;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public BooleanProperty reviewedProperty() {
        return reviewed;
    }

    public StringProperty adminNotesProperty() {
        return adminNotes;
    }

    // Getters and setters
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public UserViewModel getReportingUser() {
        return reportingUser.get();
    }

    public void setReportingUser(UserViewModel reportingUser) {
        this.reportingUser.set(reportingUser);
    }

    public ListingViewModel getReportedListing() {
        return reportedListing.get();
    }

    public void setReportedListing(ListingViewModel reportedListing) {
        this.reportedListing.set(reportedListing);
    }

    public ListingReportReason getReason() {
        return reason.get();
    }

    public void setReason(ListingReportReason reason) {
        this.reason.set(reason);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
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

    public boolean isReviewed() {
        return reviewed.get();
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed.set(reviewed);
    }

    public String getAdminNotes() {
        return adminNotes.get();
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes.set(adminNotes);
    }

    // Utility methods
    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    public String getReportingUserName() {
        return reportingUser.get() != null ? reportingUser.get().getDisplayName() : "";
    }

    public String getReportedListingTitle() {
        return reportedListing.get() != null ? reportedListing.get().getTitle() : "";
    }

    public boolean hasDescription() {
        return description.get() != null && !description.get().trim().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ListingReportViewModel that = (ListingReportViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("ListingReportViewModel{id='%s', reason=%s, reportingUser='%s', reportedListing='%s'}",
                getId(), getReason(), getReportingUserName(), getReportedListingTitle());
    }
}