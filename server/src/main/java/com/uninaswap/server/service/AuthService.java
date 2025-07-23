package com.uninaswap.server.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(UserDTO user) {
        
        if (userRepository.existsByUsername(user.getUsername()) ||
                userRepository.existsByEmail(user.getEmail())) {
            return false;
        }

        
        UserEntity entity = new UserEntity();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setLastLoginAt(now);

        
        entity.setActive(false);

        
        if (user.getFirstName() != null) {
            entity.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            entity.setLastName(user.getLastName());
        }
        if (user.getBio() != null) {
            entity.setBio(user.getBio());
        }
        if (user.getProfileImagePath() != null) {
            entity.setProfileImagePath(user.getProfileImagePath());
        }
        if (user.getPhoneNumber() != null) {
            entity.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getAddress() != null) {
            entity.setAddress(user.getAddress());
        }
        if (user.getCity() != null) {
            entity.setCity(user.getCity());
        }
        if (user.getCountry() != null) {
            entity.setCountry(user.getCountry());
        }

        userRepository.save(entity);
        return true;
    }

    /**
     * Authenticate a user and return the user entity if successful
     * 
     * @param username The username to authenticate
     * @param password The password to verify
     * @return Optional containing the user if authentication is successful, empty
     *         otherwise
     */
    public Optional<UserEntity> authenticateAndGetUser(String username, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                user.updateLastLogin();
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
}