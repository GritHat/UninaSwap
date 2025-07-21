package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.ValidationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.ValidationService.ValidationResult;
import com.uninaswap.common.message.AuthMessage;

public class RegisterController implements Refreshable {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button registerButton;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private Hyperlink termsLink;
    @FXML
    private Hyperlink loginLink;
    @FXML
    private Text registerFormTitle;
    @FXML
    private Text applicationTitle;
    @FXML
    private Text applicationTagline;
    @FXML
    private Text loginText;
    @FXML
    private Text copyrightText;

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
        buttonRegister(null); // Initialize button state
        registerMessageHandler();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("register.debug.initialized", "Register controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update form field prompts
        if (firstNameField != null) {
            firstNameField.setPromptText(localeService.getMessage("register.firstname.prompt", "First Name"));
        }
        if (lastNameField != null) {
            lastNameField.setPromptText(localeService.getMessage("register.lastname.prompt", "Last Name"));
        }
        if (usernameField != null) {
            usernameField.setPromptText(localeService.getMessage("register.username.prompt", "Username"));
        }
        if (emailField != null) {
            emailField.setPromptText(localeService.getMessage("register.email.prompt", "Email Address"));
        }
        if (passwordField != null) {
            passwordField.setPromptText(localeService.getMessage("register.password.prompt", "Password"));
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText(localeService.getMessage("register.confirm.password.prompt", "Confirm Password"));
        }

        // Update static text elements
        if (registerFormTitle != null) {
            registerFormTitle.setText(localeService.getMessage("register.title", "Create Account"));
        }
        if (applicationTitle != null) {
            applicationTitle.setText(localeService.getMessage("application.title", "UninaSwap"));
        }
        if (applicationTagline != null) {
            applicationTagline.setText(localeService.getMessage("application.register.tagline", 
                    "Join the largest university exchange community"));
        }
        if (copyrightText != null) {
            copyrightText.setText(localeService.getMessage("copyright", "© 2024 UninaSwap"));
        }

        // Update button and link text
        if (registerButton != null) {
            registerButton.setText(localeService.getMessage("button.register", "Register"));
        }
        if (termsCheckBox != null) {
            termsCheckBox.setText(localeService.getMessage("register.terms.accept", "I accept the"));
        }
        if (termsLink != null) {
            termsLink.setText(localeService.getMessage("register.terms.link", "Terms and Conditions"));
        }
        if (loginText != null) {
            loginText.setText(localeService.getMessage("register.have.account", "Already have an account?"));
        }
        if (loginLink != null) {
            loginLink.setText(localeService.getMessage("button.login", "Login"));
        }

        // Clear any existing message
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.getStyleClass().clear();
        }
    }

    @FXML
    private void buttonRegister(ActionEvent event) {
        registerButton.setDisable(true);
        termsCheckBox.selectedProperty().addListener((_, _, newValue) -> {
            registerButton.setDisable(!newValue);
            System.out.println(localeService.getMessage("register.debug.terms.changed", "Terms checkbox changed: {0}")
                .replace("{0}", String.valueOf(newValue)));
        });
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        registerButton.setDisable(true);

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        System.out.println(localeService.getMessage("register.debug.attempt", "Registration attempt for user: {0}")
            .replace("{0}", username));

        // Validate input
        ValidationResult validationResult = validationService.validateRegistration(
                username, email, password, confirmPassword);

        if (!validationResult.isValid()) {
            showMessage(validationResult.getMessageKey(), "message-error");
            registerButton.setDisable(false);
            System.err.println(localeService.getMessage("register.debug.validation.failed", "Registration validation failed: {0}")
                .replace("{0}", validationResult.getMessageKey()));
            return;
        }

        try {
            authService.register(firstName, lastName, username, email, password);
            showMessage("register.info.registering", "message-info");
            System.out.println(localeService.getMessage("register.debug.request.sent", "Registration request sent to server"));
        } catch (Exception e) {
            showMessage("register.error.connection", "message-error");
            registerButton.setDisable(false);
            System.err.println(localeService.getMessage("register.debug.connection.error", "Registration connection error: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void showLogin(ActionEvent event) {
        try {
            navigationService.navigateToLogin(usernameField);
            System.out.println(localeService.getMessage("register.debug.navigate.login", "Navigating to login view"));
        } catch (Exception e) {
            showMessage("navigation.error.load.login", "message-error");
            System.err.println(localeService.getMessage("register.debug.navigation.error", "Error navigating to login: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.REGISTER_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("register.success", "message-success");
                    System.out.println(localeService.getMessage("register.debug.success", "Registration successful"));
                    
                    // Optionally navigate to login after successful registration
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(2000); // Show success message for 2 seconds
                            navigationService.navigateToLogin(usernameField);
                        } catch (Exception e) {
                            System.err.println(localeService.getMessage("register.debug.auto.navigate.error", 
                                "Error auto-navigating to login: {0}").replace("{0}", e.getMessage()));
                        }
                    });
                } else {
                    // Use server's message or fallback
                    String errorMessage = (response.getMessage() != null && !response.getMessage().isEmpty())
                            ? response.getMessage()
                            : localeService.getMessage("register.error.failed", "Registration failed");
                    messageLabel.setText(errorMessage);
                    messageLabel.getStyleClass().clear();
                    messageLabel.getStyleClass().add("message-error");
                    registerButton.setDisable(false);
                    System.err.println(localeService.getMessage("register.debug.server.error", 
                        "Registration failed with server message: {0}").replace("{0}", errorMessage));
                }
            }
        });
    }

    /**
     * Helper method to display messages
     */
    private void showMessage(String messageKey, String styleClass) {
        String message = localeService.getMessage(messageKey, messageKey);
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add(styleClass);
        System.out.println(localeService.getMessage("register.debug.message.shown", "Message shown: {0} (style: {1})")
            .replace("{0}", message)
            .replace("{1}", styleClass));
    }

    /**
     * Registers this controller's message handler with the AuthenticationService.
     * Called by NavigationService when this view is loaded.
     */
    public void registerMessageHandler() {
        authService.setAuthResponseHandler(this::handleAuthResponse);
        System.out.println(localeService.getMessage("register.debug.handler.registered", "Auth response handler registered"));
    }

    @FXML
    public void openTermsAndConditions(MouseEvent event) {
        try {
            // Pass the source of the event to the NavigationService
            navigationService.openTermsAndConditions((javafx.scene.Node) event.getSource());
            System.out.println(localeService.getMessage("register.debug.terms.opened", "Terms and Conditions dialog opened"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("register.debug.terms.error", "Error opening Terms and Conditions: {0}")
                .replace("{0}", e.getMessage()));
            e.printStackTrace();
            showMessage("navigation.error.terms", "message-error");
        }
    }
}