package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class UserViewModel {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty profileImagePath = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lastLoginAt = new SimpleObjectProperty<>();
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty(0.0);
    private final IntegerProperty reviewCount = new SimpleIntegerProperty(0);
    private final StringProperty bio = new SimpleStringProperty();

    // Constructors
    public UserViewModel() {
    }

    public UserViewModel(Long id, String username, String email) {
        setId(id);
        setUsername(username);
        setEmail(email);
    }

    // Property getters
    public LongProperty idProperty() {
        return id;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty profileImagePathProperty() {
        return profileImagePath;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> lastLoginAtProperty() {
        return lastLoginAt;
    }

    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty cityProperty() {
        return city;
    }

    public StringProperty countryProperty() {
        return country;
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    public IntegerProperty reviewCountProperty() {
        return reviewCount;
    }

    public StringProperty bioProperty() {
        return bio;
    }

    // Getters and setters
    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id != null ? id : 0L);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getProfileImagePath() {
        return profileImagePath.get();
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath.set(profileImagePath);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt.get();
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt.set(lastLoginAt);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean active) {
        this.isActive.set(active);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public String getCountry() {
        return country.get();
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    public double getRating() {
        return rating.get();
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }

    public int getReviewCount() {
        return reviewCount.get();
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount.set(reviewCount);
    }

    public String getBio() {
        return bio.get();
    }

    public void setBio(String bio) {
        this.bio.set(bio);
    }

    // Utility methods
    public String getFullName() {
        String first = getFirstName();
        String last = getLastName();

        if (first != null && last != null) {
            return first + " " + last;
        } else if (first != null) {
            return first;
        } else if (last != null) {
            return last;
        } else {
            return getUsername();
        }
    }

    public String getDisplayName() {
        String fullName = getFullName();
        return fullName != null && !fullName.equals(getUsername()) ? fullName : getUsername();
    }

    public boolean hasProfileImage() {
        String imagePath = getProfileImagePath();
        return imagePath != null && !imagePath.trim().isEmpty() && !imagePath.equals("default");
    }

    public String getFormattedRating() {
        if (getReviewCount() > 0) {
            return String.format("⭐ %.1f (%d)", getRating(), getReviewCount());
        } else {
            return "Nessuna recensione";
        }
    }

    public String getLocationString() {
        StringBuilder location = new StringBuilder();

        if (getCity() != null && !getCity().trim().isEmpty()) {
            location.append(getCity());
        }

        if (getCountry() != null && !getCountry().trim().isEmpty()) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(getCountry());
        }

        return location.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        UserViewModel that = (UserViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserViewModel{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
