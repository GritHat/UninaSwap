package com.uninaswap.common.message;

import com.uninaswap.common.dto.UserDTO;

/**
 * 
 */
public class AuthMessage extends Message {
    /**
     * 
     */
    public enum Type {
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        REGISTER_REQUEST,
        REGISTER_RESPONSE,
        AUTH_ERROR_RESPONSE
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
    private String password;
    /**
     * 
     */
    private String email;
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
    private UserDTO user;
    /**
     * 
     */
    private String token;

    /**
     * 
     */
    public AuthMessage() {
        super();
        setMessageType("auth");
    }

    
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
    public UserDTO getUser() {
        return user;
    }

    /**
     * @param user
     */
    public void setUser(UserDTO user) {
        this.user = user;
    }

    /**
     *
     */
    public String getToken() {
        return token;
    }

    /**
     *
     */
    public void setToken(String token) {
        this.token = token;
    }
}