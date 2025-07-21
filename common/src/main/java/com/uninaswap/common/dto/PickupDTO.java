package com.uninaswap.common.dto;

import com.uninaswap.common.enums.PickupStatus;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PickupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String offerId;
    private OfferDTO offer;
    private List<LocalDate> availableDates = new ArrayList<>();
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private String location;
    private String details;
    private PickupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO createdBy;
    private UserDTO updatedBy;

    // Default constructor
    public PickupDTO() {
    }

    // Constructor for creating new pickup
    public PickupDTO(String offerId, List<LocalDate> availableDates, LocalTime startTime, LocalTime endTime,
            String location, String details, Long createdByUserId) {
        this.offerId = offerId;
        this.availableDates = availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.details = details;
        this.status = PickupStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public OfferDTO getOffer() {
        return offer;
    }

    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }

    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>();
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public LocalTime getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(LocalTime selectedTime) {
        this.selectedTime = selectedTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public PickupStatus getStatus() {
        return status;
    }

    public void setStatus(PickupStatus status) {
        this.status = status;
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

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Utility methods
    public LocalDateTime getSelectedDateTime() {
        if (selectedDate != null && selectedTime != null) {
            return selectedDate.atTime(selectedTime);
        }
        return null;
    }

    public boolean isTimeSlotValid(LocalTime time) {
        return time != null && !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean isDateAvailable(LocalDate date) {
        return availableDates.contains(date);
    }

    public boolean hasValidSelection() {
        return selectedDate != null && selectedTime != null &&
                isDateAvailable(selectedDate) && isTimeSlotValid(selectedTime);
    }

    public String getTimeRangeDisplay() {
        if (startTime != null && endTime != null) {
            return startTime.toString() + " - " + endTime.toString();
        }
        return "";
    }
}