package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller for the Terms and Conditions popup view.
 */
public class TermsAndConditionsController {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private ImageView alertIcon;
    
    @FXML
    private Button primaryButton;
    
    @FXML
    private Button secondaryButton;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Default initialization logic
    }
    
    /**
     * Handles the primary action (usually OK/Accept).
     * 
     * @param event The action event
     */
    @FXML
    private void handlePrimaryAction(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Handles the secondary action (usually Cancel/Decline).
     * 
     * @param event The action event
     */
    @FXML
    private void handleSecondaryAction(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Sets the message to display in the dialog.
     * 
     * @param message The message to display
     */
    public void setMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }
    
    /**
     * Sets the title of the dialog.
     * 
     * @param title The title to display
     */
    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }
    
    /**
     * Configures the primary button.
     * 
     * @param text The button text
     * @param visible Whether the button should be visible
     */
    public void configurePrimaryButton(String text, boolean visible) {
        if (primaryButton != null) {
            primaryButton.setText(text);
            primaryButton.setVisible(visible);
        }
    }
    
    /**
     * Configures the secondary button.
     * 
     * @param text The button text
     * @param visible Whether the button should be visible
     */
    public void configureSecondaryButton(String text, boolean visible) {
        if (secondaryButton != null) {
            secondaryButton.setText(text);
            secondaryButton.setVisible(visible);
        }
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) primaryButton.getScene().getWindow();
        stage.close();
    }
}
