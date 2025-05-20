package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.Locale;
import java.util.Map;

public class LanguageController {
    
    @FXML
    private ComboBox<Locale> languageComboBox;
    
    private final LocaleService localeService = LocaleService.getInstance();
    
    // Map of supported languages
    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
        "English", Locale.ENGLISH,
        "Italiano", Locale.ITALIAN
    );
    
    @FXML
    public void initialize() {
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
}