package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import com.uninaswap.client.service.NavigationService;

public class SearchController {
    
    @FXML private TextField searchField;
    
    private final NavigationService navigationService = NavigationService.getInstance();
    
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
    public void showProfile(MouseEvent event) {
        try {
            navigationService.loadProfileView();
        } catch (Exception e) {
            System.err.println("Errore nella navigazione al profilo: " + e.getMessage());
        }
    }
    
    @FXML
    public void showSettings(MouseEvent event) {
        try {
            navigationService.navigateToSettings();
        } catch (Exception e) {
            System.err.println("Errore nella navigazione alle impostazioni: " + e.getMessage());
        }
    }
    
    @FXML
    public void openSupport(MouseEvent event) {
        try {
            navigationService.navigateToSupport();
        } catch (Exception e) {
            System.err.println("Errore nella navigazione al supporto: " + e.getMessage());
        }
    }
    
    @FXML
    public void logout(MouseEvent event) {
        try {
            navigationService.logout();
        } catch (Exception e) {
            System.err.println("Errore durante il logout: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleChangePassword(ActionEvent event) {
        // Implementa il cambio password
    }
    
    @FXML
    public void handleNotificationSettings(ActionEvent event) {
        // Implementa impostazioni notifiche
    }
}
