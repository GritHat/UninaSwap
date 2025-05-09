package com.uninaswap.server.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.uninaswap.common.model.User;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.UserRepository;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public boolean register(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername()) ||
            userRepository.existsByEmail(user.getEmail())) {
            return false;
        }
        
        // Create new user with encrypted password
        UserEntity entity = new UserEntity();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(entity);
        return true;
    }
    
    public boolean authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }
}