package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SearchBarController implements Initializable {
    
    @FXML
    private HBox searchBar;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializzazione del controller
    }
    
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText();
        
        // Esegui la ricerca quando pronto
        if (searchQuery != null && !searchQuery.isEmpty()) {
            System.out.println("Ricerca per: " + searchQuery);
            // TODO: Implementare la logica di ricerca
            
            // Esempio: puoi inviare un evento di ricerca tramite EventBus
            // EventBusService.getInstance().publish(EventTypes.SEARCH_REQUESTED, searchQuery);
        }
    }
}