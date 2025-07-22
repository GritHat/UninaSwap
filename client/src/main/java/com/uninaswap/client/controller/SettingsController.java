package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.util.AlertHelper;

import java.util.Locale;

public class SettingsController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    
    @FXML private CheckBox emailNotificationsCheckBox;
    @FXML private CheckBox appNotificationsCheckBox;
    @FXML private CheckBox offerNotificationsCheckBox;
    @FXML private CheckBox messageNotificationsCheckBox;
    
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider fontSizeSlider;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    
    @FXML
    public void initialize() {
        if (userSessionService.isLoggedIn()) {
            emailField.setText(userSessionService.getUser().getEmail());
        }
        
        languageComboBox.getItems().addAll("Italiano", "English");
        String currentLanguage = localeService.getCurrentLocale().getDisplayLanguage();
        languageComboBox.setValue(currentLanguage);
        themeComboBox.getItems().addAll("Chiaro", "Scuro", "Sistema");
        themeComboBox.setValue("Chiaro");
        emailNotificationsCheckBox.setSelected(true);
        appNotificationsCheckBox.setSelected(true);
        offerNotificationsCheckBox.setSelected(true);
        messageNotificationsCheckBox.setSelected(true);
    }
    
    @FXML
    public void handleChangePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showWarningAlert(
                "Campi mancanti",
                "Tutti i campi sono obbligatori",
                "Per cambiare la password, compila tutti i campi."
            );
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showWarningAlert(
                "Password non corrispondenti",
                "Le nuove password non corrispondono",
                "Assicurati che la nuova password e la conferma siano uguali."
            );
            return;
        }
        AlertHelper.showInformationAlert(
            "Password aggiornata",
            "La tua password è stata aggiornata con successo",
            "Usa la nuova password al prossimo accesso."
        );
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    @FXML
    public void handleSave(ActionEvent event) {
        String selectedLanguage = languageComboBox.getValue();
        Locale locale;
        if (selectedLanguage.equals("English")) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.ITALIAN;
        }
        localeService.setLocale(locale);
        AlertHelper.showInformationAlert(
            "Impostazioni salvate",
            "Le tue impostazioni sono state salvate",
            "Le nuove impostazioni sono state applicate."
        );
    }
    
    @FXML
    public void handleCancel(ActionEvent event) {
    }
}
