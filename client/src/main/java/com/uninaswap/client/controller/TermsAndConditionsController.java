package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the Terms and Conditions view.
 */
public class TermsAndConditionsController {
    
    @FXML
    private Button closeButton;
    
    @FXML
    public void initialize() {
        // Initialization code if needed
    }
    
    /**
     * Handles the close button action and closes the window.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        // Get the stage from the close button and close it
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
