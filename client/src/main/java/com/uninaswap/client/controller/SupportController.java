package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SupportController {
    
    @FXML
    public void initialize() {
        // Inizializzazione della vista di supporto
    }
    
    @FXML
    public void handleSendMessage(ActionEvent event) {
        // Implementazione per l'invio del messaggio di supporto
        // In una versione reale, qui ci sarebbe la logica per inviare il messaggio al server
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Messaggio Inviato");
        alert.setHeaderText("Il tuo messaggio è stato inviato con successo");
        alert.setContentText("Riceverai una risposta al tuo indirizzo email entro 24-48 ore lavorative.");
        alert.showAndWait();
    }
    
    @FXML
    public void openBeginnerGuide(ActionEvent event) {
        // Apre la guida per principianti (potrebbe aprire un PDF o una nuova vista)
        openResourceNotAvailableDialog("Guida per principianti");
    }
    
    @FXML
    public void openVideoTutorials(ActionEvent event) {
        // Apre i video tutorial (potrebbe aprire un browser con YouTube)
        openResourceNotAvailableDialog("Video tutorial");
    }
    
    @FXML
    public void openSellingTips(ActionEvent event) {
        // Apre i suggerimenti per vendere
        openResourceNotAvailableDialog("Suggerimenti per vendere");
    }
    
    @FXML
    public void openSecurityGuide(ActionEvent event) {
        // Apre la guida sulla sicurezza
        openResourceNotAvailableDialog("Guida sulla sicurezza");
    }
    
    private void openResourceNotAvailableDialog(String resourceName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Risorsa non disponibile");
        alert.setHeaderText(resourceName);
        alert.setContentText("Questa risorsa sarà disponibile nella prossima versione dell'applicazione.");
        alert.showAndWait();
    }
}
