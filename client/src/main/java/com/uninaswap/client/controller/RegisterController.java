package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.ValidationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ValidationService.ValidationResult;
import com.uninaswap.common.message.AuthMessage;

import java.io.IOException;
import java.util.Arrays;

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField surnameField;
    
    @FXML
    private ComboBox<String> departmentComboBox;
    
    @FXML
    private TextField matricolaField;
    
    @FXML
    private CheckBox termsCheckBox;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Hyperlink loginLink;
    
    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final ValidationService validationService;
    private final LocaleService localeService;
    
    public RegisterController() {
        this.navigationService = NavigationService.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.validationService = ValidationService.getInstance();
        this.localeService = LocaleService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // Set message handler
        registerMessageHandler();
        
        // Inizializza il ComboBox con i dipartimenti
        departmentComboBox.getItems().addAll(Arrays.asList(
            "Ingegneria",
            "Economia",
            "Giurisprudenza",
            "Medicina",
            "Lettere",
            "Scienze",
            "Architettura"
        ));
    }
    
    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate input
        ValidationResult validationResult = validationService.validateRegistration(
            username, email, password, confirmPassword);
        
        if (!validationResult.isValid()) {
            showMessage(validationResult.getMessageKey(), "message-error");
            return;
        }
        
        try {
            authService.register(username, email, password);
            showMessage("register.info.registering", "message-info");
        } catch (Exception e) {
            showMessage("register.error.connection", "message-error");
        }
        
        System.out.println("Registrazione tentata con email: " + emailField.getText());
        
        // Qui aggiungerai la logica di registrazione
        // Per ora, ritorniamo alla pagina di login
        if (validateForm()) {
            try {
                // Torna alla schermata di login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/new/fxml_new/LoginView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("UninaSwap - Login");
                stage.show();
            } catch (IOException e) {
                System.err.println("Errore durante il caricamento della schermata di login: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void showLogin(ActionEvent event) {
        try {
            navigationService.navigateToLogin(usernameField);
        } catch (Exception e) {
            showMessage("navigation.error.load.login", "message-error");
        }
    }
    
    @FXML
    public void handleLoginLink(ActionEvent event) {
        System.out.println("Redirect alla pagina di login");
        
        try {
            // Torna alla schermata di login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/new/fxml_new/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("UninaSwap - Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della schermata di login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.REGISTER_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("register.success", "message-success");
                } else {
                    // Use server's message or fallback
                    String errorMessage = (response.getMessage() != null && !response.getMessage().isEmpty()) 
                        ? response.getMessage() 
                        : localeService.getMessage("register.error.failed");
                    messageLabel.setText(errorMessage);
                    messageLabel.getStyleClass().clear();
                    messageLabel.getStyleClass().add("message-error");
                }
            }
        });
    }
    
    /**
     * Helper method to display messages
     */
    private void showMessage(String messageKey, String styleClass) {
        messageLabel.setText(localeService.getMessage(messageKey));
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add(styleClass);
    }
    
    /**
     * Registers this controller's message handler with the AuthenticationService.
     * Called by NavigationService when this view is loaded.
     */
    public void registerMessageHandler() {
        authService.setAuthResponseHandler(this::handleAuthResponse);
    }
    
    private boolean validateForm() {
        // Qui aggiungerai la logica di validazione del form
        // Per ora, controlliamo solo che i campi obbligatori siano compilati
        return nameField.getText() != null && !nameField.getText().isEmpty() &&
               surnameField.getText() != null && !surnameField.getText().isEmpty() &&
               emailField.getText() != null && !emailField.getText().isEmpty() &&
               departmentComboBox.getValue() != null &&
               matricolaField.getText() != null && !matricolaField.getText().isEmpty() &&
               passwordField.getText() != null && !passwordField.getText().isEmpty() &&
               confirmPasswordField.getText() != null && !confirmPasswordField.getText().isEmpty() &&
               passwordField.getText().equals(confirmPasswordField.getText()) &&
               termsCheckBox.isSelected();
    }
}