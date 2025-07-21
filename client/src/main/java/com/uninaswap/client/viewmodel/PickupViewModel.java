package com.uninaswap.client.viewmodel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.uninaswap.common.dto.OfferDTO;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.enums.PickupStatus;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PickupViewModel {
    private StringProperty offerId = new SimpleStringProperty();
    private LongProperty id = new SimpleLongProperty();
    private ObjectProperty<OfferViewModel> offer = new SimpleObjectProperty<>();
    private ObservableList<LocalDate> availableDates = FXCollections.observableArrayList();
    private ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>();
    private ObjectProperty<LocalTime> endTime = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>();
    private ObjectProperty<LocalTime> selectedTime = new SimpleObjectProperty<>();
    private StringProperty location = new SimpleStringProperty();
    private StringProperty details = new SimpleStringProperty();
    private ObjectProperty<PickupStatus> status = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private ObjectProperty<UserDTO> createdBy = new SimpleObjectProperty<>();
    private ObjectProperty<UserDTO> updatedBy = new SimpleObjectProperty<>();

    // Default constructor
    public PickupViewModel() {
    }

    // Constructor for creating new pickup
    public PickupViewModel(String offerId,
            OfferViewModel offer,
            List<LocalDate> availableDates, 
            LocalTime startTime, 
            LocalTime endTime, 
            String location, 
            String details, 
            Long createdByUserId) {
        setOfferId(offerId);
        setOffer(offer);
        setAvailableDates(availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>());
        setStartTime(startTime);
        setEndTime(endTime);
        setLocation(location);
        setDetails(details);
        setStatus(PickupStatus.PENDING);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    // Property getters
    public StringProperty offerIdProperty() {
        return offerId;
    }

    public LongProperty idProperty() {
        return id;
    }

    public ObjectProperty<OfferViewModel> offerProperty() {
        return offer;
    }

    public ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    public ObjectProperty<LocalTime> endTimeProperty() {
        return endTime;
    }

    public ObjectProperty<LocalDate> selectedDateProperty() {
        return selectedDate;
    }

    public ObjectProperty<LocalTime> selectedTimeProperty() {
        return selectedTime;
    }

    public StringProperty locationProperty() {
        return location;
    }

    public StringProperty detailsProperty() {
        return details;
    }

    public ObjectProperty<PickupStatus> statusProperty() {
        return status;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public ObjectProperty<UserDTO> createdByProperty() {
        return createdBy;
    }

    public ObjectProperty<UserDTO> updatedByProperty() {
        return updatedBy;
    }

    // Getters and setters
    public String getOfferId() {
        return offerId.get();
    }

    public void setOfferId(String offerId) {
        this.offerId.set(offerId);
    }

    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public OfferViewModel getOffer() {
        return offer.get();
    }

    public void setOffer(OfferViewModel offer) {
        this.offer.set(offer);
    }

    public ObservableList<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates.setAll(availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>());
    }

    public LocalTime getStartTime() {
        return startTime.get();
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime.set(startTime);
    }

    public LocalTime getEndTime() {
        return endTime.get();
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime.set(endTime);
    }

    public LocalDate getSelectedDate() {
        return selectedDate.get();
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate.set(selectedDate);
    }

    public LocalTime getSelectedTime() {
        return selectedTime.get();
    }

    public void setSelectedTime(LocalTime selectedTime) {
        this.selectedTime.set(selectedTime);
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getDetails() {
        return details.get();
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public PickupStatus getStatus() {
        return status.get();
    }

    public void setStatus(PickupStatus status) {
        this.status.set(status);
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

    public UserDTO getCreatedBy() {
        return createdBy.get();
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy.set(createdBy);
    }

    public UserDTO getUpdatedBy() {
        return updatedBy.get();
    }

    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy.set(updatedBy);
    }
}
