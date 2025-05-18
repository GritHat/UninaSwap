package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    public void initialize() {
        System.out.println("Inizializzazione HomeController");
    }
    
    @FXML
    public void apri_profilo() {
        System.out.println("Apertura profilo");
        // Implementare la navigazione alla pagina del profilo
    }
    
    // Altri metodi di gestione eventi della home page
}
