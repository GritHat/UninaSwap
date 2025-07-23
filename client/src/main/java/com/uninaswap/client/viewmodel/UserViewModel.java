package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * 
 */
public class UserViewModel {
    /**
     * 
     */
    private final LongProperty id = new SimpleLongProperty();
    /**
     * 
     */
    private final StringProperty username = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty email = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty firstName = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty lastName = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty profileImagePath = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> lastLoginAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);
    /**
     * 
     */
    private final StringProperty phoneNumber = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty address = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty city = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty country = new SimpleStringProperty();
    /**
     * 
     */
    private final DoubleProperty rating = new SimpleDoubleProperty(0.0);
    /**
     * 
     */
    private final IntegerProperty reviewCount = new SimpleIntegerProperty(0);
    /**
     * 
     */
    private final StringProperty bio = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty stateProvince = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty zipPostalCode = new SimpleStringProperty();

    
    /**
     * 
     */
    public UserViewModel() {
    }

    /**
     * @param id
     * @param username
     * @param email
     */
    public UserViewModel(Long id, String username, String email) {
        setId(id);
        setUsername(username);
        setEmail(email);
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
    public StringProperty usernameProperty() {
        return username;
    }

    /**
     * @return
     */
    public StringProperty emailProperty() {
        return email;
    }

    /**
     * @return
     */
    public StringProperty firstNameProperty() {
        return firstName;
    }

    /**
     * @return
     */
    public StringProperty lastNameProperty() {
        return lastName;
    }

    /**
     * @return
     */
    public StringProperty profileImagePathProperty() {
        return profileImagePath;
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
    public ObjectProperty<LocalDateTime> lastLoginAtProperty() {
        return lastLoginAt;
    }

    /**
     * @return
     */
    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    /**
     * @return
     */
    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    /**
     * @return
     */
    public StringProperty addressProperty() {
        return address;
    }

    /**
     * @return
     */
    public StringProperty cityProperty() {
        return city;
    }

    /**
     * @return
     */
    public StringProperty countryProperty() {
        return country;
    }

    /**
     * @return
     */
    public DoubleProperty ratingProperty() {
        return rating;
    }

    /**
     * @return
     */
    public IntegerProperty reviewCountProperty() {
        return reviewCount;
    }

    /**
     * @return
     */
    public StringProperty bioProperty() {
        return bio;
    }

    /**
     * @return
     */
    public StringProperty stateProvinceProperty() {
        return stateProvince;
    }

    /**
     * @return
     */
    public StringProperty zipPostalCodeProperty() {
        return zipPostalCode;
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
        this.id.set(id != null ? id : 0L);
    }

    /**
     * @return
     */
    public String getUsername() {
        return username.get();
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username.set(username);
    }

    /**
     * @return
     */
    public String getEmail() {
        return email.get();
    }

    /**
     * @param email
     */
    public void setEmail(String email) {
        this.email.set(email);
    }

    /**
     * @return
     */
    public String getFirstName() {
        return firstName.get();
    }

    /**
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    /**
     * @return
     */
    public String getLastName() {
        return lastName.get();
    }

    /**
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    /**
     * @return
     */
    public String getProfileImagePath() {
        return profileImagePath.get();
    }

    /**
     * @param profileImagePath
     */
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath.set(profileImagePath);
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
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt.get();
    }

    /**
     * @param lastLoginAt
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt.set(lastLoginAt);
    }

    /**
     * @return
     */
    public boolean isActive() {
        return isActive.get();
    }

    /**
     * @param active
     */
    public void setActive(boolean active) {
        this.isActive.set(active);
    }

    /**
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    /**
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    /**
     * @return
     */
    public String getAddress() {
        return address.get();
    }

    /**
     * @param address
     */
    public void setAddress(String address) {
        this.address.set(address);
    }

    /**
     * @return
     */
    public String getCity() {
        return city.get();
    }

    /**
     * @param city
     */
    public void setCity(String city) {
        this.city.set(city);
    }

    /**
     * @return
     */
    public String getCountry() {
        return country.get();
    }

    /**
     * @param country
     */
    public void setCountry(String country) {
        this.country.set(country);
    }

    /**
     * @return
     */
    public double getRating() {
        return rating.get();
    }

    /**
     * @param rating
     */
    public void setRating(double rating) {
        this.rating.set(rating);
    }

    /**
     * @return
     */
    public int getReviewCount() {
        return reviewCount.get();
    }

    /**
     * @param reviewCount
     */
    public void setReviewCount(int reviewCount) {
        this.reviewCount.set(reviewCount);
    }

    /**
     * @return
     */
    public String getBio() {
        return bio.get();
    }

    /**
     * @param bio
     */
    public void setBio(String bio) {
        this.bio.set(bio);
    }

    /**
     * @return
     */
    public String getStateProvince() {
        return stateProvince.get();
    }

    /**
     * @param stateProvince
     */
    public void setStateProvince(String stateProvince) {
        this.stateProvince.set(stateProvince);
    }

    /**
     * @return
     */
    public String getZipPostalCode() {
        return zipPostalCode.get();
    }

    /**
     * @param zipPostalCode
     */
    public void setZipPostalCode(String zipPostalCode) {
        this.zipPostalCode.set(zipPostalCode);
    }

    
    /**
     * @return
     */
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

    /**
     * @return
     */
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName != null && !fullName.equals(getUsername()) ? fullName : getUsername();
    }

    /**
     * @return
     */
    public boolean hasProfileImage() {
        String imagePath = getProfileImagePath();
        return imagePath != null && !imagePath.trim().isEmpty() && !imagePath.equals("default");
    }

    /**
     * @return
     */
    public String getFormattedRating() {
        if (getReviewCount() > 0) {
            return String.format("⭐ %.1f (%d)", getRating(), getReviewCount());
        } else {
            return "Nessuna recensione";
        }
    }

    /**
     * @return
     */
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

    /**
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        UserViewModel that = (UserViewModel) obj;
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
        return "UserViewModel{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
