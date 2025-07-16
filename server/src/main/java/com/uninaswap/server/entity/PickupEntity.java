package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.uninaswap.common.enums.PickupStatus;

/**
 * Entity representing a pickup arrangement for an accepted offer
 */
@Entity
@Table(name = "pickups")
public class PickupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false, unique = true)
    private OfferEntity offer;

    // Available dates (JSON array in database)
    @Column(name = "available_dates", nullable = false, columnDefinition = "JSON")
    private String availableDatesJson;

    // Time range for all dates
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // Selected date and time (when status becomes ACCEPTED)
    @Column(name = "selected_date")
    private LocalDate selectedDate;

    @Column(name = "selected_time")
    private LocalTime selectedTime;

    @Column(nullable = false, length = 500)
    private String location;

    @Column(length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickupStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;

    // Transient field for Java operations
    @Transient
    private List<LocalDate> availableDates = new ArrayList<>();

    // Default constructor
    public PickupEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PickupStatus.PENDING;
    }

    // Constructor
    public PickupEntity(OfferEntity offer, List<LocalDate> availableDates, LocalTime startTime, LocalTime endTime,
            String location, String details, UserEntity createdBy) {
        this();
        this.offer = offer;
        this.availableDates = availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.details = details;
        this.createdBy = createdBy;
        updateAvailableDatesJson();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OfferEntity getOffer() {
        return offer;
    }

    public void setOffer(OfferEntity offer) {
        this.offer = offer;
    }

    public List<LocalDate> getAvailableDates() {
        if (availableDates.isEmpty() && availableDatesJson != null) {
            parseAvailableDatesJson();
        }
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates != null ? new ArrayList<>(availableDates) : new ArrayList<>();
        updateAvailableDatesJson();
    }

    public String getAvailableDatesJson() {
        return availableDatesJson;
    }

    public void setAvailableDatesJson(String availableDatesJson) {
        this.availableDatesJson = availableDatesJson;
        parseAvailableDatesJson();
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
        this.updatedAt = LocalDateTime.now();
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

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public UserEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserEntity updatedBy) {
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

    // JSON conversion methods
    private void updateAvailableDatesJson() {
        if (availableDates != null && !availableDates.isEmpty()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < availableDates.size(); i++) {
                json.append("\"").append(availableDates.get(i).toString()).append("\"");
                if (i < availableDates.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            this.availableDatesJson = json.toString();
        } else {
            this.availableDatesJson = "[]";
        }
    }

    private void parseAvailableDatesJson() {
        if (availableDatesJson != null && !availableDatesJson.trim().isEmpty() && !availableDatesJson.equals("[]")) {
            try {
                availableDates.clear();
                String cleanJson = availableDatesJson.replace("[", "").replace("]", "").replace("\"", "");
                String[] dateStrings = cleanJson.split(",");
                for (String dateString : dateStrings) {
                    if (!dateString.trim().isEmpty()) {
                        availableDates.add(LocalDate.parse(dateString.trim()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing available dates JSON: " + e.getMessage());
                availableDates = new ArrayList<>();
            }
        } else {
            availableDates = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateAvailableDatesJson();
    }

    @PrePersist
    protected void onPersist() {
        updateAvailableDatesJson();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PickupEntity))
            return false;
        PickupEntity that = (PickupEntity) o;
        return offer != null && offer.getId().equals(that.offer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}