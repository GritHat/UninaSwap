package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.application.Platform;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;

import java.util.Locale;
import java.util.Map;

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
    private final EventBusService eventBus = EventBusService.getInstance();

    // Mappatura tra codici visualizzati nel ComboBox e Locale corrispondenti
    private static final Map<String, Locale> LANGUAGE_MAP = Map.of(
        "ITA", Locale.ITALIAN,
        "ENG", Locale.ENGLISH
    );
    
    // Mappatura inversa per convertire da Locale a codice lingua
    private static final Map<Locale, String> LOCALE_TO_CODE = Map.of(
        Locale.ITALIAN, "ITA",
        Locale.ENGLISH, "ENG"
    );

    @FXML
    public void initialize() {
        // Popola il ComboBox
        languageSelector.setItems(FXCollections.observableArrayList("ITA", "ENG"));
        
        // Imposta il valore iniziale in base alla lingua corrente
        String currentCode = LOCALE_TO_CODE.getOrDefault(
            localeService.getCurrentLocale(), 
            "ENG" // Fallback a inglese se la lingua corrente non è nella mappa
        );
        languageSelector.setValue(currentCode);
        
        // Listener per il cambio di lingua dal ComboBox
        languageSelector.setOnAction(_ -> changeLanguage(languageSelector.getValue()));
        
        // Sottoscrizione all'evento di cambio lingua (per rimanere sincronizzati)
        eventBus.subscribe(EventTypes.LOCALE_CHANGED, data -> {
            if (data instanceof Map) {
                Map<String, Object> eventData = (Map<String, Object>) data;
                Locale newLocale = (Locale) eventData.get("newLocale");
                Platform.runLater(() -> {
                    String code = LOCALE_TO_CODE.getOrDefault(newLocale, "ENG");
                    if (!code.equals(languageSelector.getValue())) {
                        languageSelector.setValue(code);
                    }
                    updateConnectionStatus(connectionStatusLabel.getText().equals(localeService.getMessage("status.online")));
                });
            }
        });
        
        connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));
        updateConnectionStatus(true);
    }

    private void changeLanguage(String languageCode) {
        Locale locale = LANGUAGE_MAP.getOrDefault(languageCode, Locale.ENGLISH);
        localeService.setLocale(locale);
    }

    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionStatusLabel.setText(localeService.getMessage("dashboard.status.online"));
            connectionStatusLabel.getStyleClass().setAll("status-text-success");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/icons/online.png")));
        } else {
            connectionStatusLabel.setText(localeService.getMessage("dashboard.status.offline"));
            connectionStatusLabel.getStyleClass().setAll("status-text-warning");
            statusIcon.setImage(new Image(getClass().getResourceAsStream("/images/icons/offline.png")));
        }
    }
}
