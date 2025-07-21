package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import com.uninaswap.client.service.LocaleService;
import javafx.util.StringConverter;
import java.lang.classfile.Label;
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
    @FXML
    private ComboBox<Locale> languageComboBox;
    @FXML
    private Label languageLabel;

    private final LocaleService localeService = LocaleService.getInstance();


    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
        "English", Locale.ENGLISH,
        "Italiano", Locale.ITALIAN
    );

    @FXML
    public void initialize() {    
        connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));
        updateConnectionStatus(true);

        // Populate ComboBox with language options
        languageComboBox.setItems(FXCollections.observableArrayList(SUPPORTED_LANGUAGES.values()));
        
        // Set a custom string converter to display language names
        languageComboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                return locale.getDisplayLanguage(locale);
            }
            
            @Override
            public Locale fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        // Set current locale
        languageComboBox.setValue(localeService.getCurrentLocale());
        
        // Add listener to change language when selection changes
        languageComboBox.valueProperty().addListener((_, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                localeService.setLocale(newValue);
            }
        });
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
