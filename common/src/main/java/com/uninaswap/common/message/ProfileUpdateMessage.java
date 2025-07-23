package com.uninaswap.common.message;

/**
 * 
 */
public class ProfileUpdateMessage extends Message {

    /**
     * 
     */
    public ProfileUpdateMessage() {
        super();
        setMessageType("profile");
    }

    /**
     * 
     */
    public enum Type {
        UPDATE_REQUEST,
        UPDATE_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private String username;
    /**
     * 
     */
    private String firstName;
    /**
     * 
     */
    private String lastName;
    /**
     * 
     */
    private String bio;
    /**
     * 
     */
    private String profileImagePath;
    /**
     * 
     */
    private String message;
    /**
     * 
     */
    private String zipPostalCode;
    /**
     * 
     */
    private String stateProvince;
    /**
     * 
     */
    private String address;
    /**
     * 
     */
    private String country;
    /**
     * 
     */
    private String city;


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
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return
     */
    public String getBio() {
        return bio;
    }

    /**
     * @param bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * @return
     */
    public String getProfileImagePath() {
        return profileImagePath;
    }

    /**
     * @param profileImagePath
     */
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return
     */
    public String getZipPostalCode() {
        return zipPostalCode;
    }

    /**
     * @param zipPostalCode
     */
    public void setZipPostalCode(String zipPostalCode) {
        this.zipPostalCode = zipPostalCode;
    }

    /**
     * @return
     */
    public String getStateProvince() {
        return stateProvince;
    }

    /**
     * @param stateProvince
     */
    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    /**
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }
}