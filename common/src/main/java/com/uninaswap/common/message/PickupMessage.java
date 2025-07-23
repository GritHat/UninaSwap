package com.uninaswap.common.message;

import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.PickupStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 
 */
public class PickupMessage extends Message {

    /**
     * 
     */
    public enum Type {
        // Requests
        CREATE_PICKUP_REQUEST,
        UPDATE_PICKUP_REQUEST,
        UPDATE_PICKUP_STATUS_REQUEST,
        GET_PICKUP_REQUEST,
        GET_PICKUP_BY_OFFER_REQUEST,
        GET_USER_PICKUPS_REQUEST,
        GET_UPCOMING_PICKUPS_REQUEST,
        GET_PAST_PICKUPS_REQUEST,
        GET_PICKUPS_BY_STATUS_REQUEST,
        DELETE_PICKUP_REQUEST,
        CANCEL_PICKUP_REQUEST,
        ACCEPT_PICKUP_REQUEST,
        REJECT_PICKUP_REQUEST,
        PROPOSE_PICKUP_TIMES_REQUEST,
        SELECT_PICKUP_TIME_REQUEST,

        // Responses
        CREATE_PICKUP_RESPONSE,
        UPDATE_PICKUP_RESPONSE,
        UPDATE_PICKUP_STATUS_RESPONSE,
        GET_PICKUP_RESPONSE,
        GET_PICKUP_BY_OFFER_RESPONSE,
        GET_USER_PICKUPS_RESPONSE,
        GET_UPCOMING_PICKUPS_RESPONSE,
        GET_PAST_PICKUPS_RESPONSE,
        GET_PICKUPS_BY_STATUS_RESPONSE,
        DELETE_PICKUP_RESPONSE,
        CANCEL_PICKUP_RESPONSE,
        ACCEPT_PICKUP_RESPONSE,
        REJECT_PICKUP_RESPONSE,
        PROPOSE_PICKUP_TIMES_RESPONSE,
        SELECT_PICKUP_TIME_RESPONSE, 
        CANCEL_PICKUP_ARRANGEMENT_REQUEST,
        CANCEL_PICKUP_ARRANGEMENT_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private Long pickupId;
    /**
     * 
     */
    private String offerId;
    /**
     * 
     */
    private Long userId;
    /**
     * 
     */
    private PickupStatus status;
    /**
     * 
     */
    private PickupDTO pickup;
    /**
     * 
     */
    private List<PickupDTO> pickups;
    /**
     * 
     */
    private int page = 0;
    /**
     * 
     */
    private int size = 20;
    /**
     * 
     */
    private long totalElements = 0;
    /**
     * 
     */
    private int totalPages = 0;
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
    private List<LocalDate> availableDates;
    /**
     * 
     */
    private LocalTime startTime;
    /**
     * 
     */
    private LocalTime endTime;

    // Default constructor
    /**
     * 
     */
    public PickupMessage() {
        super();
        setMessageType("pickup");
    }

    // Getters and setters
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public Long getPickupId() {
        return pickupId;
    }

    /**
     * @param pickupId
     */
    public void setPickupId(Long pickupId) {
        this.pickupId = pickupId;
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
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
    public PickupDTO getPickup() {
        return pickup;
    }

    /**
     * @param pickup
     */
    public void setPickup(PickupDTO pickup) {
        this.pickup = pickup;
    }

    /**
     * @return
     */
    public List<PickupDTO> getPickups() {
        return pickups;
    }

    /**
     * @param pickups
     */
    public void setPickups(List<PickupDTO> pickups) {
        this.pickups = pickups;
    }

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
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
    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    /**
     * @param availableDates
     */
    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
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
}