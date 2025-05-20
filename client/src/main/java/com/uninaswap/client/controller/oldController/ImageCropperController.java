package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ImageCropperController {

    @FXML private ImageView sourceImageView;
    @FXML private StackPane cropperContainer;
    @FXML private StackPane cropOverlay;
    @FXML private Region cropCircle;
    @FXML private Slider zoomSlider;
    
    private double dragStartX;
    private double dragStartY;
    private double imageStartX;
    private double imageStartY;
    private Consumer<Image> cropCallback;
    private double circleDiameter;
    
    @FXML
    public void initialize() {
        // Setup drag functionality - apply to the StackPane instead of the image
        // This ensures the drag behavior moves the image but not the overlay
        cropperContainer.setOnMousePressed(this::handleMousePressed);
        cropperContainer.setOnMouseDragged(this::handleMouseDragged);
        
        // Make sourceImageView non-mouseTransparent so clicks on the image work
        sourceImageView.setMouseTransparent(false);
        
        // Make sure the overlay is mouseTransparent (should not receive mouse events)
        cropOverlay.setMouseTransparent(true);
        cropCircle.setMouseTransparent(true);
        
        // Setup zoom functionality
        zoomSlider.valueProperty().addListener((_, _, newVal) -> {
            double scale = newVal.doubleValue();
            sourceImageView.setScaleX(scale);
            sourceImageView.setScaleY(scale);
        });
        
        // Ensure the crop circle size is tracked and stays centered
        cropperContainer.widthProperty().addListener((_, _, _) -> {
            updateCropCircleSize();
            centerCropOverlay();
        });
        
        cropperContainer.heightProperty().addListener((_, _, _) -> {
            updateCropCircleSize();
            centerCropOverlay();
        });
        
        // Set the overlay position to be fixed
        cropOverlay.setMouseTransparent(true);
        cropOverlay.setPickOnBounds(false);
        
        // Force the overlay to stay centered in the StackPane
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
        
        // Ensure the window size is reasonable
        cropperContainer.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Stage stage = (Stage) newScene.getWindow();
                // Set reasonable size limits
                stage.setMinWidth(500);
                stage.setMinHeight(450);
                stage.setMaxWidth(800);
                stage.setMaxHeight(700);
                
                // Center overlay after scene is loaded
                Platform.runLater(this::centerCropOverlay);
            }
        });
    }
    
    private void updateCropCircleSize() {
        // Use a fixed size - don't calculate based on container size
        // This should match the SVG path's circle size
        double size = 200.0;
        
        // Set fixed size properties
        cropCircle.setPrefSize(size, size);
        cropCircle.setMinSize(size, size);
        cropCircle.setMaxSize(size, size);
        circleDiameter = size;
        
        // Ensure the overlay is positioned correctly
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
        StackPane.setAlignment(cropCircle, javafx.geometry.Pos.CENTER);
    }
    
    /**
     * Ensures the crop overlay remains centered in the viewport
     * regardless of image size
     */
    private void centerCropOverlay() {
        // Make sure the crop overlay is always centered in the visible viewport
        cropOverlay.setTranslateX(0);
        cropOverlay.setTranslateY(0);
        
        // Force it to layout in the center
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
    }
    
    public void setImage(Image image) {
        sourceImageView.setImage(image);
        
        // For very large images, we need to set a maximum scale to start with
        double maxInitialDimension = 800; // Max initial dimension to fit in viewport
        double scale = 1.0;
        
        // Calculate if we need to scale down initially
        if (image.getWidth() > maxInitialDimension || image.getHeight() > maxInitialDimension) {
            double widthScale = maxInitialDimension / image.getWidth();
            double heightScale = maxInitialDimension / image.getHeight();
            scale = Math.min(widthScale, heightScale);
        }
        
        // Calculate how to fit the image within the container
        double containerWidth = cropperContainer.getWidth() > 0 ? 
                               cropperContainer.getWidth() : 500; // Default if not yet laid out
        double containerHeight = cropperContainer.getHeight() > 0 ? 
                               cropperContainer.getHeight() : 350; // Default if not yet laid out
                               
        double containerRatio = containerWidth / containerHeight;
        double imageRatio = image.getWidth() / image.getHeight();
        
        if (imageRatio > containerRatio) {
            // Image is wider - fit to width
            double fitWidth = Math.min(containerWidth, image.getWidth() * scale);
            sourceImageView.setFitWidth(fitWidth);
            sourceImageView.setFitHeight(0); // Auto
        } else {
            // Image is taller - fit to height
            double fitHeight = Math.min(containerHeight, image.getHeight() * scale);
            sourceImageView.setFitHeight(fitHeight);
            sourceImageView.setFitWidth(0); // Auto
        }
        
        // Reset position and scale
        sourceImageView.setTranslateX(0);
        sourceImageView.setTranslateY(0);
        sourceImageView.setScaleX(1.0);
        sourceImageView.setScaleY(1.0);
        zoomSlider.setValue(1.0);
        
        // Ensure the overlay is centered after image is loaded
        Platform.runLater(() -> {
            centerCropOverlay();
            updateCropCircleSize();
            
            // Additional centering for very large images
            if (image.getWidth() > 1000 || image.getHeight() > 1000) {
                centerImageInViewport();
            }
        });
    }
    
    /**
     * Centers the image in the viewport when it's too large
     */
    private void centerImageInViewport() {
        // Force the image to be centered in the container
        StackPane.setAlignment(sourceImageView, javafx.geometry.Pos.CENTER);
        
        // Additional centering may be needed to ensure the overlay matches the image position
        double offsetX = (cropperContainer.getWidth() - sourceImageView.getBoundsInLocal().getWidth()) / 2;
        double offsetY = (cropperContainer.getHeight() - sourceImageView.getBoundsInLocal().getHeight()) / 2;
        
        if (Math.abs(offsetX) > 1 || Math.abs(offsetY) > 1) {
            sourceImageView.setTranslateX(0);
            sourceImageView.setTranslateY(0);
        }
    }
    
    private void handleMousePressed(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        imageStartX = sourceImageView.getTranslateX();
        imageStartY = sourceImageView.getTranslateY();
    }
    
    private void handleMouseDragged(MouseEvent event) {
        double offsetX = event.getSceneX() - dragStartX;
        double offsetY = event.getSceneY() - dragStartY;
        
        // Only move the image view, not the container or overlay
        sourceImageView.setTranslateX(imageStartX + offsetX);
        sourceImageView.setTranslateY(imageStartY + offsetY);
        
        // Prevent event bubbling to parent containers
        event.consume();
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) sourceImageView.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleApply() {
        if (cropCallback != null) {
            Image croppedImage = createCroppedImage();
            cropCallback.accept(croppedImage);
        }
        
        // Close the dialog
        Stage stage = (Stage) sourceImageView.getScene().getWindow();
        stage.close();
    }
    
    private Image createCroppedImage() {
        // Get the source image
        Image sourceImage = sourceImageView.getImage();
        
        // Get actual display dimensions and scale
        double displayedImageWidth = sourceImageView.getBoundsInLocal().getWidth();
        double displayedImageHeight = sourceImageView.getBoundsInLocal().getHeight();
        double zoomScale = sourceImageView.getScaleX();
        
        // Get the actual dimensions of the original image
        double originalImageWidth = sourceImage.getWidth();
        double originalImageHeight = sourceImage.getHeight();
        
        // Calculate the combined scale (fit + zoom)
        double fitScaleX = displayedImageWidth / originalImageWidth;
        double fitScaleY = displayedImageHeight / originalImageHeight;
        double combinedScaleX = fitScaleX * zoomScale;
        double combinedScaleY = fitScaleY * zoomScale;
        
        // Get crop circle bounds (the visible overlay)
        double cropRadius = circleDiameter / 2;
        
        // Get the center of the crop circle in the scene
        double cropCenterX = cropOverlay.localToScene(cropOverlay.getWidth() / 2, 0).getX();
        double cropCenterY = cropOverlay.localToScene(0, cropOverlay.getHeight() / 2).getY();
        
        // Get the top-left corner of the image view in scene coordinates
        double imageViewX = sourceImageView.localToScene(0, 0).getX();
        double imageViewY = sourceImageView.localToScene(0, 0).getY();
        
        // Calculate the center of the crop circle relative to the image view
        double relativeCenterX = cropCenterX - imageViewX;
        double relativeCenterY = cropCenterY - imageViewY;
        
        // Convert to coordinates in the original image
        double originalCenterX = relativeCenterX / combinedScaleX;
        double originalCenterY = relativeCenterY / combinedScaleY;
        
        // Calculate the crop radius in the original image
        double originalCropRadiusX = cropRadius / combinedScaleX;
        double originalCropRadiusY = cropRadius / combinedScaleY;
        
        // Calculate the crop boundaries in the original image
        double cropStartX = originalCenterX - originalCropRadiusX;
        double cropStartY = originalCenterY - originalCropRadiusY;
        double cropSize = originalCropRadiusX * 2; // Use X for the size calculation
        
        // Ensure we stay within bounds of the original image
        cropStartX = Math.max(0, cropStartX);
        cropStartY = Math.max(0, cropStartY);
        
        // Create the circular crop with a fixed size (use the overlay diameter)
        int outputSize = (int) circleDiameter;
        WritableImage result = new WritableImage(outputSize, outputSize);
        PixelReader reader = sourceImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();
        
        // Track whether we encountered any out-of-bounds issues
        boolean outOfBounds = false;
        
        // Apply the circular mask while copying pixels
        for (int y = 0; y < outputSize; y++) {
            for (int x = 0; x < outputSize; x++) {
                // Calculate distance from the center of the output image
                double distanceX = x - outputSize / 2.0;
                double distanceY = y - outputSize / 2.0;
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                
                // Only copy pixels within the circle
                if (distance <= outputSize / 2.0) {
                    // Map to coordinates in the original image
                    // This maps from the target circular crop to the source image area
                    double sourceX = cropStartX + (x * cropSize / outputSize);
                    double sourceY = cropStartY + (y * cropSize / outputSize);
                    
                    // Check bounds before reading pixel
                    if (sourceX >= 0 && sourceX < originalImageWidth && 
                        sourceY >= 0 && sourceY < originalImageHeight) {
                        Color color = reader.getColor((int) sourceX, (int) sourceY);
                        writer.setColor(x, y, color);
                    } else {
                        // Out of bounds - fill with transparent
                        writer.setColor(x, y, Color.TRANSPARENT);
                        outOfBounds = true;
                    }
                } else {
                    // Outside the circle - set transparent
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        
        // If we had out-of-bounds issues, log them for debugging
        if (outOfBounds) {
            System.out.println("Warning: Some pixels were out of bounds during cropping");
        }
        
        return result;
    }
    
    public void setCropCallback(Consumer<Image> callback) {
        this.cropCallback = callback;
    }
}