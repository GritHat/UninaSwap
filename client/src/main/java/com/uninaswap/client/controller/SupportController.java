package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * 
 */
public class SupportController {
    
    /**
     * 
     */
    @FXML private TextField subjectField;
    /**
     * 
     */
    @FXML private TextArea messageField;
    /**
     * 
     */
    @FXML private Button sendButton;
   
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
    public void handleSendMessage(ActionEvent event) {
        if (validateForm()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Richiesta inviata");
            alert.setHeaderText("La tua richiesta è stata inviata");
            alert.setContentText("Ti risponderemo al più presto all'indirizzo email associato al tuo account.");
            alert.showAndWait();
            subjectField.clear();
            messageField.clear();
        }
    }
    
    /**
     * @return
     */
    private boolean validateForm() {
        String subject = subjectField.getText().trim();
        String message = messageField.getText().trim();
        
        if (subject.isEmpty()) {
            showError("Oggetto richiesto", "Inserisci l'oggetto della richiesta.");
            return false;
        }
        
        if (message.isEmpty()) {
            showError("Messaggio richiesto", "Inserisci il testo del messaggio.");
            return false;
        }
        
        if (message.length() < 20) {
            showError("Messaggio troppo breve", "Il messaggio deve contenere almeno 20 caratteri.");
            return false;
        }
        
        return true;
    }
    
    /**
     * @param title
     * @param message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
