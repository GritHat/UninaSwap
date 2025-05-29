package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import com.uninaswap.client.service.LocaleService;

public class FooterController {
    @FXML private HBox footer;
    @FXML private Text statusText;
    @FXML private ImageView statusIcon;
    @FXML private ComboBox<String> languageSelector;
    
    private final LocaleService localeService = LocaleService.getInstance();
    
    @FXML
    public void initialize() {
        // Initialize language options
        languageSelector.getItems().addAll("ITA", "ENG");
        languageSelector.setValue("ENG"); // Default language
        
        // Set language change listener
        languageSelector.setOnAction(event -> {
            changeLanguage(languageSelector.getValue());
        });
        
        // Initialize connection status
        updateConnectionStatus(true);
    }
    
    private void changeLanguage(String language) {
        System.out.println("Changing language to: " + language);
        localeService.setLocale(language.toLowerCase());
    }
    
    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            statusText.setText("Online");
            statusText.getStyleClass().setAll("status-text-success");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/online.png")));
        } else {
            statusText.setText("Offline");
            statusText.getStyleClass().setAll("status-text-warning");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/offline.png")));
        }
    }
}
