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

/**
 * 
 */
public class PickupViewModel {
    /**
     * 
     */
    private StringProperty offerId = new SimpleStringProperty();
    /**
     * 
     */
    private LongProperty id = new SimpleLongProperty();
    /**
     * 
     */
    private ObjectProperty<OfferViewModel> offer = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObservableList<LocalDate> availableDates = FXCollections.observableArrayList();
    /**
     * 
     */
    private ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<LocalTime> endTime = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<LocalTime> selectedTime = new SimpleObjectProperty<>();
    /**
     * 
     */
    private StringProperty location = new SimpleStringProperty();
    /**
     * 
     */
    private StringProperty details = new SimpleStringProperty();
    /**
     * 
     */
    private ObjectProperty<PickupStatus> status = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<UserDTO> createdBy = new SimpleObjectProperty<>();
    /**
     * 
     */
    private ObjectProperty<UserDTO> updatedBy = new SimpleObjectProperty<>();

    
    /**
     * 
     */
    public PickupViewModel() {
    }

    
    /**
     * @param offerId
     * @param offer
     * @param availableDates
     * @param startTime
     * @param endTime
     * @param location
     * @param details
     * @param createdByUserId
     */
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

    
    /**
     * @return
     */
    public StringProperty offerIdProperty() {
        return offerId;
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
    public ObjectProperty<OfferViewModel> offerProperty() {
        return offer;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalTime> endTimeProperty() {
        return endTime;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDate> selectedDateProperty() {
        return selectedDate;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalTime> selectedTimeProperty() {
        return selectedTime;
    }

    /**
     * @return
     */
    public StringProperty locationProperty() {
        return location;
    }

    /**
     * @return
     */
    public StringProperty detailsProperty() {
        return details;
    }

    /**
     * @return
     */
    public ObjectProperty<PickupStatus> statusProperty() {
        return status;
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
    public ObjectProperty<UserDTO> createdByProperty() {
        return createdBy;
    }

    /**
     * @return
     */
    public ObjectProperty<UserDTO> updatedByProperty() {
        return updatedBy;
    }

    
    /**
     * @return
     */
    public String getOfferId() {
        return offerId.get();
    }

    /**
     * @param offerId
     */
    public void setOfferId(String offerId) {
        this.offerId.set(offerId);
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
    public OfferViewModel getOffer() {
        return offer.get();
    }

    /**
     * @param offer
     */
    public void setOffer(OfferViewModel offer) {
        this.offer.set(offer);
    }

    /**
     * @return
     */
    public ObservableList<LocalDate> getAvailableDates() {
        return availableDates;
    }

    /**
     * @param availableDates
     */
    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates.setAll(availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>());
    }

    /**
     * @return
     */
    public LocalTime getStartTime() {
        return startTime.get();
    }

    /**
     * @param startTime
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime.set(startTime);
    }

    /**
     * @return
     */
    public LocalTime getEndTime() {
        return endTime.get();
    }

    /**
     * @param endTime
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime.set(endTime);
    }

    /**
     * @return
     */
    public LocalDate getSelectedDate() {
        return selectedDate.get();
    }

    /**
     * @param selectedDate
     */
    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate.set(selectedDate);
    }

    /**
     * @return
     */
    public LocalTime getSelectedTime() {
        return selectedTime.get();
    }

    /**
     * @param selectedTime
     */
    public void setSelectedTime(LocalTime selectedTime) {
        this.selectedTime.set(selectedTime);
    }

    /**
     * @return
     */
    public String getLocation() {
        return location.get();
    }

    /**
     * @param location
     */
    public void setLocation(String location) {
        this.location.set(location);
    }

    /**
     * @return
     */
    public String getDetails() {
        return details.get();
    }

    /**
     * @param details
     */
    public void setDetails(String details) {
        this.details.set(details);
    }

    /**
     * @return
     */
    public PickupStatus getStatus() {
        return status.get();
    }

    /**
     * @param status
     */
    public void setStatus(PickupStatus status) {
        this.status.set(status);
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
    public UserDTO getCreatedBy() {
        return createdBy.get();
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy.set(createdBy);
    }

    /**
     * @return
     */
    public UserDTO getUpdatedBy() {
        return updatedBy.get();
    }

    /**
     * @param updatedBy
     */
    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy.set(updatedBy);
    }
}
