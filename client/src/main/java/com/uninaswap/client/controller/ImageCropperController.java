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

/**
 * 
 */
public class ImageCropperController {

    /**
     * 
     */
    @FXML private ImageView sourceImageView;
    /**
     * 
     */
    @FXML private StackPane cropperContainer;
    /**
     * 
     */
    @FXML private StackPane cropOverlay;
    /**
     * 
     */
    @FXML private Region cropCircle;
    /**
     * 
     */
    @FXML private Slider zoomSlider;
    
    /**
     * 
     */
    private double dragStartX;
    /**
     * 
     */
    private double dragStartY;
    /**
     * 
     */
    private double imageStartX;
    /**
     * 
     */
    private double imageStartY;
    /**
     * 
     */
    private Consumer<Image> cropCallback;
    /**
     * 
     */
    private double circleDiameter;
    
    /**
     * 
     */
    @FXML
    public void initialize() {
        cropperContainer.setOnMousePressed(this::handleMousePressed);
        cropperContainer.setOnMouseDragged(this::handleMouseDragged);
        sourceImageView.setMouseTransparent(false);
        cropOverlay.setMouseTransparent(true);
        cropCircle.setMouseTransparent(true);
        zoomSlider.valueProperty().addListener((_, _, newVal) -> {
            double scale = newVal.doubleValue();
            sourceImageView.setScaleX(scale);
            sourceImageView.setScaleY(scale);
        });
        cropperContainer.widthProperty().addListener((_, _, _) -> {
            updateCropCircleSize();
            centerCropOverlay();
        });
        cropperContainer.heightProperty().addListener((_, _, _) -> {
            updateCropCircleSize();
            centerCropOverlay();
        });
        cropOverlay.setMouseTransparent(true);
        cropOverlay.setPickOnBounds(false);
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
        cropperContainer.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setMinWidth(500);
                stage.setMinHeight(450);
                stage.setMaxWidth(800);
                stage.setMaxHeight(700);
                Platform.runLater(this::centerCropOverlay);
            }
        });
    }
    
    /**
     * 
     */
    private void updateCropCircleSize() {
        double size = 200.0;
        cropCircle.setPrefSize(size, size);
        cropCircle.setMinSize(size, size);
        cropCircle.setMaxSize(size, size);
        circleDiameter = size;
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
        StackPane.setAlignment(cropCircle, javafx.geometry.Pos.CENTER);
    }
    
    /**
     * Ensures the crop overlay remains centered in the viewport
     * regardless of image size
     */
    /**
     * 
     */
    private void centerCropOverlay() {
        cropOverlay.setTranslateX(0);
        cropOverlay.setTranslateY(0);
        StackPane.setAlignment(cropOverlay, javafx.geometry.Pos.CENTER);
    }
    
    /**
     * @param image
     */
    public void setImage(Image image) {
        sourceImageView.setImage(image);
        double maxInitialDimension = 800;
        double scale = 1.0;
        if (image.getWidth() > maxInitialDimension || image.getHeight() > maxInitialDimension) {
            double widthScale = maxInitialDimension / image.getWidth();
            double heightScale = maxInitialDimension / image.getHeight();
            scale = Math.min(widthScale, heightScale);
        }
        double containerWidth = cropperContainer.getWidth() > 0 ? 
                               cropperContainer.getWidth() : 500; 
        double containerHeight = cropperContainer.getHeight() > 0 ? 
                               cropperContainer.getHeight() : 350;
                               
        double containerRatio = containerWidth / containerHeight;
        double imageRatio = image.getWidth() / image.getHeight();
        
        if (imageRatio > containerRatio) {
            double fitWidth = Math.min(containerWidth, image.getWidth() * scale);
            sourceImageView.setFitWidth(fitWidth);
            sourceImageView.setFitHeight(0);
        } else {
            double fitHeight = Math.min(containerHeight, image.getHeight() * scale);
            sourceImageView.setFitHeight(fitHeight);
            sourceImageView.setFitWidth(0);
        }
        sourceImageView.setTranslateX(0);
        sourceImageView.setTranslateY(0);
        sourceImageView.setScaleX(1.0);
        sourceImageView.setScaleY(1.0);
        zoomSlider.setValue(1.0);
        Platform.runLater(() -> {
            centerCropOverlay();
            updateCropCircleSize();
            if (image.getWidth() > 1000 || image.getHeight() > 1000) {
                centerImageInViewport();
            }
        });
    }
    
    /**
     * Centers the image in the viewport when it's too large
     */
    /**
     * 
     */
    private void centerImageInViewport() {
        StackPane.setAlignment(sourceImageView, javafx.geometry.Pos.CENTER);
        double offsetX = (cropperContainer.getWidth() - sourceImageView.getBoundsInLocal().getWidth()) / 2;
        double offsetY = (cropperContainer.getHeight() - sourceImageView.getBoundsInLocal().getHeight()) / 2;
        if (Math.abs(offsetX) > 1 || Math.abs(offsetY) > 1) {
            sourceImageView.setTranslateX(0);
            sourceImageView.setTranslateY(0);
        }
    }
    
    /**
     * @param event
     */
    private void handleMousePressed(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        imageStartX = sourceImageView.getTranslateX();
        imageStartY = sourceImageView.getTranslateY();
    }
    
    /**
     * @param event
     */
    private void handleMouseDragged(MouseEvent event) {
        double offsetX = event.getSceneX() - dragStartX;
        double offsetY = event.getSceneY() - dragStartY;
        sourceImageView.setTranslateX(imageStartX + offsetX);
        sourceImageView.setTranslateY(imageStartY + offsetY);
        event.consume();
    }
    
    /**
     * 
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) sourceImageView.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 
     */
    @FXML
    private void handleApply() {
        if (cropCallback != null) {
            Image croppedImage = createCroppedImage();
            cropCallback.accept(croppedImage);
        }
        Stage stage = (Stage) sourceImageView.getScene().getWindow();
        stage.close();
    }
    
    /**
     * @return
     */
    private Image createCroppedImage() {
        Image sourceImage = sourceImageView.getImage();
        double displayedImageWidth = sourceImageView.getBoundsInLocal().getWidth();
        double displayedImageHeight = sourceImageView.getBoundsInLocal().getHeight();
        double zoomScale = sourceImageView.getScaleX();
        double originalImageWidth = sourceImage.getWidth();
        double originalImageHeight = sourceImage.getHeight();
        double fitScaleX = displayedImageWidth / originalImageWidth;
        double fitScaleY = displayedImageHeight / originalImageHeight;
        double combinedScaleX = fitScaleX * zoomScale;
        double combinedScaleY = fitScaleY * zoomScale;
        double cropRadius = circleDiameter / 2;
        double cropCenterX = cropOverlay.localToScene(cropOverlay.getWidth() / 2, 0).getX();
        double cropCenterY = cropOverlay.localToScene(0, cropOverlay.getHeight() / 2).getY();
        double imageViewX = sourceImageView.localToScene(0, 0).getX();
        double imageViewY = sourceImageView.localToScene(0, 0).getY();
        double relativeCenterX = cropCenterX - imageViewX;
        double relativeCenterY = cropCenterY - imageViewY;
        double originalCenterX = relativeCenterX / combinedScaleX;
        double originalCenterY = relativeCenterY / combinedScaleY;
        double originalCropRadiusX = cropRadius / combinedScaleX;
        double originalCropRadiusY = cropRadius / combinedScaleY;
        double cropStartX = originalCenterX - originalCropRadiusX;
        double cropStartY = originalCenterY - originalCropRadiusY;
        double cropSize = originalCropRadiusX * 2;
        cropStartX = Math.max(0, cropStartX);
        cropStartY = Math.max(0, cropStartY);
        int outputSize = (int) circleDiameter;
        WritableImage result = new WritableImage(outputSize, outputSize);
        PixelReader reader = sourceImage.getPixelReader();
        PixelWriter writer = result.getPixelWriter();
        boolean outOfBounds = false;
        for (int y = 0; y < outputSize; y++) {
            for (int x = 0; x < outputSize; x++) {
                double distanceX = x - outputSize / 2.0;
                double distanceY = y - outputSize / 2.0;
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                if (distance <= outputSize / 2.0) {
                    double sourceX = cropStartX + (x * cropSize / outputSize);
                    double sourceY = cropStartY + (y * cropSize / outputSize);
                    if (sourceX >= 0 && sourceX < originalImageWidth && 
                        sourceY >= 0 && sourceY < originalImageHeight) {
                        Color color = reader.getColor((int) sourceX, (int) sourceY);
                        writer.setColor(x, y, color);
                    } else {
                        writer.setColor(x, y, Color.TRANSPARENT);
                        outOfBounds = true;
                    }
                } else {
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        if (outOfBounds) {
            System.out.println("Warning: Some pixels were out of bounds during cropping");
        }
        
        return result;
    }
    
    /**
     * @param callback
     */
    public void setCropCallback(Consumer<Image> callback) {
        this.cropCallback = callback;
    }
}