package com.uninaswap.server.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageService {
    
    private final String uploadDir;
    
    public ImageService() {
        // Create uploads directory in project root
        this.uploadDir = "uploads";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Saves a Base64 encoded image to disk
     * @param base64Image Base64 encoded image data
     * @param format Image format (jpg, png, etc)
     * @return The unique image ID that can be used to retrieve the image
     */
    public String saveImage(String base64Image, String format) throws IOException {
        // Generate a unique ID for the image
        String imageId = UUID.randomUUID().toString();
        
        // Decode the base64 string
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        
        // Create the file path
        String filename = imageId + "." + format;
        String filePath = uploadDir + File.separator + filename;
        
        // Write the file to disk
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(imageBytes);
        }
        
        return imageId;
    }
    
    /**
     * Retrieves an image from disk as Base64
     * @param imageId The ID of the image to retrieve
     * @param format Image format
     * @return Base64 encoded image data
     */
    public String getImage(String imageId, String format) throws IOException {
        String filename = imageId + "." + format;
        Path path = Paths.get(uploadDir, filename);
        
        if (!Files.exists(path)) {
            throw new IOException("Image not found: " + imageId);
        }
        
        byte[] imageBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Deletes an image from disk
     * @param imageId The ID of the image to delete
     * @param format Image format
     * @return true if deletion was successful
     */
    public boolean deleteImage(String imageId, String format) {
        String filename = imageId + "." + format;
        Path path = Paths.get(uploadDir, filename);
        
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}