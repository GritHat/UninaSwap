package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 */
public class UserDTO implements Serializable {
    /**
     * 
     */
    private Long id;
    /**
     * 
     */
    private String username;
    /**
     * 
     */
    private String email;
    /**
     * 
     */
    private String password; // Store hashed password only
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
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime lastLoginAt;
    /**
     * 
     */
    private Boolean isActive;
    /**
     * 
     */
    private String phoneNumber;
    /**
     * 
     */
    private String address;
    /**
     * 
     */
    private String city;
    /**
     * 
     */
    private String country;
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
    public UserDTO() {
    }

    /**
     * @param username
     * @param email
     * @param password
     */
    public UserDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
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
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
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
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * @param lastLoginAt
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
    public String getCity() {
        return city;
    }

    /**
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
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
    public Boolean isActive() {
        return isActive;
    }

    /**
     * @param active
     */
    public void setActive(Boolean active) {
        this.isActive = active;
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
}