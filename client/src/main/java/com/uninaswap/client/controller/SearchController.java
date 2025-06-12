package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.uninaswap.client.service.NavigationService;

public class SearchController {
    
    @FXML private TextField searchField;
    
    @FXML
    public void initialize() {
        // Inizializzazione della vista di ricerca
    }
    
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            System.out.println("Cercando: " + searchQuery);
            // Logica di ricerca da implementare
        }
    }
  
    @FXML
    public void handleChangePassword(ActionEvent event) {
        // Implementa il cambio password
    }

}
