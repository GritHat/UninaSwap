package com.uninaswap.common.message;

public class ProfileUpdateMessage extends Message {

    public ProfileUpdateMessage() {
        super();
        setMessageType("profile");
    }

    public enum Type {
        UPDATE_REQUEST,
        UPDATE_RESPONSE
    }

    private Type type;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImagePath;
    private String message;

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}