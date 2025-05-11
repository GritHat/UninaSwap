package com.uninaswap.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

import com.uninaswap.server.service.ImageService;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Generate a unique ID for the image
            String imageId = UUID.randomUUID().toString();
            
            // Get file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            
            // Save the file
            imageService.saveImageFile(imageId, extension, file.getBytes());
            
            // Update the user's profile image path
            /*UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setProfileImagePath(imageId + "." + extension);
            userRepository.save(user);*/
            
            return ResponseEntity.ok(imageId + "." + extension);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
        }
    }
}