package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import com.uninaswap.client.service.LocaleService;

public class FooterController {
    @FXML
    private HBox footer;
    @FXML
    private Text connectionStatusLabel;
    @FXML
    private ImageView statusIcon;
    @FXML
    private ComboBox<String> languageSelector;

    private final LocaleService localeService = LocaleService.getInstance();

    @FXML
    public void initialize() {
        languageSelector.getItems().addAll("ITA", "ENG");
        languageSelector.setValue("ENG");
        languageSelector.setOnAction(_ -> changeLanguage(languageSelector.getValue()));
        
        connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));
        updateConnectionStatus(true);
    }

    private void changeLanguage(String language) {
        System.out.println("Changing language to: " + language);
        java.util.Locale locale;
        if ("ITA".equalsIgnoreCase(language)) {
            locale = java.util.Locale.of("it", "IT");
        } else {
            locale = java.util.Locale.ENGLISH;
        }
        localeService.setLocale(locale);
    }

    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionStatusLabel.setText(localeService.getMessage("status.online"));
            connectionStatusLabel.getStyleClass().setAll("status-text-success");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/icons/online.png")));
        } else {
            connectionStatusLabel.setText(localeService.getMessage("status.offline"));
            connectionStatusLabel.getStyleClass().setAll("status-text-warning");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/icons/offline.png")));
        }
    }
}
