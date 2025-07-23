package com.uninaswap.client.util;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;

/**
 * Utility class with helper methods for file operations
 */
/**
 * 
 */
public class FileHelper {
    
    /**
     * Read a file into a byte array
     * @param file The file to read
     * @return The file contents as a byte array
     * @throws IOException If the file cannot be read
     */
    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
    
    /**
     * Write a byte array to a file
     * @param data The data to write
     * @param filePath The path to write the file to
     * @throws IOException If the file cannot be written
     */
    /**
     * @param data
     * @param filePath
     * @throws IOException
     */
    public static void writeFile(byte[] data, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, data);
    }
    
    /**
     * Convert a file to a Base64 encoded string
     * @param file The file to encode
     * @return The Base64 encoded string
     * @throws IOException If the file cannot be read
     */
    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static String fileToBase64(File file) throws IOException {
        byte[] fileContent = readFile(file);
        return Base64.getEncoder().encodeToString(fileContent);
    }
    
    /**
     * Convert a Base64 encoded string to a file
     * @param base64 The Base64 encoded string
     * @param filePath The path to save the file to
     * @throws IOException If the file cannot be written
     */
    /**
     * @param base64
     * @param filePath
     * @throws IOException
     */
    public static void base64ToFile(String base64, String filePath) throws IOException {
        byte[] data = Base64.getDecoder().decode(base64);
        writeFile(data, filePath);
    }
    
    /**
     * Create a JavaFX Image from a Base64 encoded string
     * @param base64 The Base64 encoded image data
     * @return A JavaFX Image
     */
    /**
     * @param base64
     * @return
     */
    public static Image createImageFromBase64(String base64) {
        try {
            byte[] imageData = Base64.getDecoder().decode(base64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bis);
            
            if (bufferedImage == null) {
                throw new IOException("Could not decode image");
            }
            return new Image(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            System.err.println("Error creating image from Base64: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Ensure a directory exists, creating it if necessary
     * @param directoryPath The directory path to ensure
     * @throws IOException If the directory cannot be created
     */
    /**
     * @param directoryPath
     * @throws IOException
     */
    public static void ensureDirectoryExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
    
    /**
     * Get the file extension from a file name
     * @param fileName The file name
     * @return The file extension (without the dot)
     */
    /**
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Check if a file is an image based on its extension
     * @param fileName The file name to check
     * @return true if the file is an image, false otherwise
     */
    /**
     * @param fileName
     * @return
     */
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equals("png") || extension.equals("jpg") || 
               extension.equals("jpeg") || extension.equals("gif") || 
               extension.equals("bmp");
    }
    
    /**
     * Create a temporary file with a given prefix and suffix
     * @param prefix The file name prefix
     * @param suffix The file name suffix
     * @return The created temporary file
     * @throws IOException If the file cannot be created
     */
    /**
     * @param prefix
     * @param suffix
     * @return
     * @throws IOException
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix);
    }
    
    /**
     * Save an image to the app's cache directory
     * @param base64 The Base64 encoded image data
     * @param fileName The desired file name
     * @return The path to the saved file
     * @throws IOException If the file cannot be saved
     */
    /**
     * @param base64
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String saveImageToCache(String base64, String fileName) throws IOException {
        String cacheDir = System.getProperty("user.home") + "/.uninaswap/cache/images";
        ensureDirectoryExists(cacheDir);
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            extension = "png";
        }
        String filePath = cacheDir + "/" + UUID.randomUUID().toString() + "." + extension;
        base64ToFile(base64, filePath);
        return filePath;
    }
}