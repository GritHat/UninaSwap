package com.uninaswap.common.dto;

import com.uninaswap.common.enums.PickupStatus;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class PickupDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private Long id;
    /**
     * 
     */
    private String offerId;
    /**
     * 
     */
    private OfferDTO offer;
    /**
     * 
     */
    private List<LocalDate> availableDates = new ArrayList<>();
    /**
     * 
     */
    private LocalTime startTime;
    /**
     * 
     */
    private LocalTime endTime;
    /**
     * 
     */
    private LocalDate selectedDate;
    /**
     * 
     */
    private LocalTime selectedTime;
    /**
     * 
     */
    private String location;
    /**
     * 
     */
    private String details;
    /**
     * 
     */
    private PickupStatus status;
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
    private UserDTO createdBy;
    /**
     * 
     */
    private UserDTO updatedBy;

    // Default constructor
    /**
     * 
     */
    public PickupDTO() {
    }

    // Constructor for creating new pickup
    /**
     * @param offerId
     * @param availableDates
     * @param startTime
     * @param endTime
     * @param location
     * @param details
     * @param createdByUserId
     */
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
    /**
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return
     */
    public String getOfferId() {
        return offerId;
    }

    /**
     * @param offerId
     */
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    /**
     * @return
     */
    public OfferDTO getOffer() {
        return offer;
    }

    /**
     * @param offer
     */
    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }

    /**
     * @return
     */
    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    /**
     * @param availableDates
     */
    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>();
    }

    /**
     * @return
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * @return
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * @return
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * @param selectedDate
     */
    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    /**
     * @return
     */
    public LocalTime getSelectedTime() {
        return selectedTime;
    }

    /**
     * @param selectedTime
     */
    public void setSelectedTime(LocalTime selectedTime) {
        this.selectedTime = selectedTime;
    }

    /**
     * @return
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return
     */
    public String getDetails() {
        return details;
    }

    /**
     * @param details
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return
     */
    public PickupStatus getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(PickupStatus status) {
        this.status = status;
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
    public UserDTO getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     */
    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return
     */
    public UserDTO getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy
     */
    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Utility methods
    /**
     * @return
     */
    public LocalDateTime getSelectedDateTime() {
        if (selectedDate != null && selectedTime != null) {
            return selectedDate.atTime(selectedTime);
        }
        return null;
    }

    /**
     * @param time
     * @return
     */
    public boolean isTimeSlotValid(LocalTime time) {
        return time != null && !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * @param date
     * @return
     */
    public boolean isDateAvailable(LocalDate date) {
        return availableDates.contains(date);
    }

    /**
     * @return
     */
    public boolean hasValidSelection() {
        return selectedDate != null && selectedTime != null &&
                isDateAvailable(selectedDate) && isTimeSlotValid(selectedTime);
    }

    /**
     * @return
     */
    public String getTimeRangeDisplay() {
        if (startTime != null && endTime != null) {
            return startTime.toString() + " - " + endTime.toString();
        }
        return "";
    }
}