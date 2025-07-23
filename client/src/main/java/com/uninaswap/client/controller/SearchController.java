package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * 
 */
public class SearchController {
    
    /**
     * 
     */
    @FXML private TextField searchField;
    
    /**
     * 
     */
    @FXML
    public void initialize() {
    }
    
    /**
     * @param event
     */
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            System.out.println("Cercando: " + searchQuery);
        }
    }
  
}
