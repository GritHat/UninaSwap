package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Terms and Conditions popup view.
 */
public class TermsAndConditionsController implements Initializable, Refreshable {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private ImageView alertIcon;
    
    @FXML
    private Button primaryButton;
    
    @FXML
    private Button secondaryButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    
    // Dialog configuration
    private String dialogType = "terms"; // Default type
    private boolean isPrimaryButtonVisible = true;
    private boolean isSecondaryButtonVisible = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("terms.debug.initialized", "Terms and Conditions controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update dialog content based on type
        updateDialogContent();
        
        // Update button text based on current configuration
        updateButtonText();
        
        System.out.println(localeService.getMessage("terms.debug.ui.refreshed", "Terms and Conditions UI refreshed"));
    }

    private void updateDialogContent() {
        // Set default title and message based on dialog type
        switch (dialogType) {
            case "terms" -> {
                if (titleLabel != null) {
                    titleLabel.setText(localeService.getMessage("terms.dialog.title", "Terms and Conditions"));
                }
                if (messageLabel != null) {
                    messageLabel.setText(localeService.getMessage("terms.dialog.message", 
                        "Please read and accept our Terms and Conditions to continue using UninaSwap."));
                }
            }
            case "privacy" -> {
                if (titleLabel != null) {
                    titleLabel.setText(localeService.getMessage("privacy.dialog.title", "Privacy Policy"));
                }
                if (messageLabel != null) {
                    messageLabel.setText(localeService.getMessage("privacy.dialog.message", 
                        "Please review our Privacy Policy to understand how we handle your data."));
                }
            }
            case "warning" -> {
                if (titleLabel != null) {
                    titleLabel.setText(localeService.getMessage("warning.dialog.title", "Warning"));
                }
                if (messageLabel != null) {
                    messageLabel.setText(localeService.getMessage("warning.dialog.message", 
                        "Please be aware of the following important information."));
                }
            }
            case "confirmation" -> {
                if (titleLabel != null) {
                    titleLabel.setText(localeService.getMessage("confirmation.dialog.title", "Confirmation Required"));
                }
                if (messageLabel != null) {
                    messageLabel.setText(localeService.getMessage("confirmation.dialog.message", 
                        "Please confirm your action to proceed."));
                }
            }
            default -> {
                if (titleLabel != null) {
                    titleLabel.setText(localeService.getMessage("dialog.default.title", "Information"));
                }
                if (messageLabel != null) {
                    messageLabel.setText(localeService.getMessage("dialog.default.message", 
                        "Please review the following information."));
                }
            }
        }
    }

    private void updateButtonText() {
        // Update primary button text
        if (primaryButton != null) {
            String primaryText = switch (dialogType) {
                case "terms" -> localeService.getMessage("terms.button.accept", "Accept");
                case "privacy" -> localeService.getMessage("privacy.button.acknowledge", "Acknowledge");
                case "warning" -> localeService.getMessage("warning.button.understood", "Understood");
                case "confirmation" -> localeService.getMessage("confirmation.button.confirm", "Confirm");
                default -> localeService.getMessage("button.ok", "OK");
            };
            primaryButton.setText(primaryText);
            primaryButton.setVisible(isPrimaryButtonVisible);
        }

        // Update secondary button text
        if (secondaryButton != null) {
            String secondaryText = switch (dialogType) {
                case "terms" -> localeService.getMessage("terms.button.decline", "Decline");
                case "privacy" -> localeService.getMessage("privacy.button.close", "Close");
                case "warning" -> localeService.getMessage("warning.button.close", "Close");
                case "confirmation" -> localeService.getMessage("confirmation.button.cancel", "Cancel");
                default -> localeService.getMessage("button.cancel", "Cancel");
            };
            secondaryButton.setText(secondaryText);
            secondaryButton.setVisible(isSecondaryButtonVisible);
        }
    }
    
    /**
     * Handles the primary action (usually OK/Accept).
     * 
     * @param event The action event
     */
    @FXML
    private void handlePrimaryAction(ActionEvent event) {
        System.out.println(localeService.getMessage("terms.debug.primary.action", 
            "Primary action triggered for dialog type: {0}").replace("{0}", dialogType));
        
        // Publish event based on dialog type
        switch (dialogType) {
            case "terms" -> {
                EventBusService.getInstance().publishEvent(EventTypes.TERMS_ACCEPTED, null);
                System.out.println(localeService.getMessage("terms.debug.terms.accepted", "Terms and Conditions accepted"));
            }
            case "privacy" -> {
                EventBusService.getInstance().publishEvent(EventTypes.PRIVACY_ACKNOWLEDGED, null);
                System.out.println(localeService.getMessage("terms.debug.privacy.acknowledged", "Privacy Policy acknowledged"));
            }
            case "warning" -> {
                EventBusService.getInstance().publishEvent(EventTypes.WARNING_ACKNOWLEDGED, null);
                System.out.println(localeService.getMessage("terms.debug.warning.acknowledged", "Warning acknowledged"));
            }
            case "confirmation" -> {
                EventBusService.getInstance().publishEvent(EventTypes.CONFIRMATION_CONFIRMED, null);
                System.out.println(localeService.getMessage("terms.debug.confirmation.confirmed", "Confirmation confirmed"));
            }
            default -> {
                EventBusService.getInstance().publishEvent(EventTypes.DIALOG_ACCEPTED, dialogType);
                System.out.println(localeService.getMessage("terms.debug.dialog.accepted", "Dialog accepted: {0}")
                    .replace("{0}", dialogType));
            }
        }
        
        closeDialog();
    }
    
    /**
     * Handles the secondary action (usually Cancel/Decline).
     * 
     * @param event The action event
     */
    @FXML
    private void handleSecondaryAction(ActionEvent event) {
        System.out.println(localeService.getMessage("terms.debug.secondary.action", 
            "Secondary action triggered for dialog type: {0}").replace("{0}", dialogType));
        
        // Publish event based on dialog type
        switch (dialogType) {
            case "terms" -> {
                EventBusService.getInstance().publishEvent(EventTypes.TERMS_DECLINED, null);
                System.out.println(localeService.getMessage("terms.debug.terms.declined", "Terms and Conditions declined"));
            }
            case "privacy" -> {
                EventBusService.getInstance().publishEvent(EventTypes.PRIVACY_CLOSED, null);
                System.out.println(localeService.getMessage("terms.debug.privacy.closed", "Privacy Policy dialog closed"));
            }
            case "warning" -> {
                EventBusService.getInstance().publishEvent(EventTypes.WARNING_CLOSED, null);
                System.out.println(localeService.getMessage("terms.debug.warning.closed", "Warning dialog closed"));
            }
            case "confirmation" -> {
                EventBusService.getInstance().publishEvent(EventTypes.CONFIRMATION_CANCELLED, null);
                System.out.println(localeService.getMessage("terms.debug.confirmation.cancelled", "Confirmation cancelled"));
            }
            default -> {
                EventBusService.getInstance().publishEvent(EventTypes.DIALOG_CANCELLED, dialogType);
                System.out.println(localeService.getMessage("terms.debug.dialog.cancelled", "Dialog cancelled: {0}")
                    .replace("{0}", dialogType));
            }
        }
        
        closeDialog();
    }
    
    /**
     * Sets the message to display in the dialog.
     * 
     * @param message The message to display
     */
    public void setMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            System.out.println(localeService.getMessage("terms.debug.message.set", "Dialog message set: {0}")
                .replace("{0}", message != null ? message.substring(0, Math.min(50, message.length())) + "..." : "null"));
        }
    }
    
    /**
     * Sets the title of the dialog.
     * 
     * @param title The title to display
     */
    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
            System.out.println(localeService.getMessage("terms.debug.title.set", "Dialog title set: {0}")
                .replace("{0}", title != null ? title : "null"));
        }
    }
    
    /**
     * Configures the primary button.
     * 
     * @param text The button text
     * @param visible Whether the button should be visible
     */
    public void configurePrimaryButton(String text, boolean visible) {
        isPrimaryButtonVisible = visible;
        if (primaryButton != null) {
            if (text != null) {
                primaryButton.setText(text);
            }
            primaryButton.setVisible(visible);
            System.out.println(localeService.getMessage("terms.debug.primary.button.configured", 
                "Primary button configured: text={0}, visible={1}")
                    .replace("{0}", text != null ? text : "default")
                    .replace("{1}", String.valueOf(visible)));
        }
    }
    
    /**
     * Configures the secondary button.
     * 
     * @param text The button text
     * @param visible Whether the button should be visible
     */
    public void configureSecondaryButton(String text, boolean visible) {
        isSecondaryButtonVisible = visible;
        if (secondaryButton != null) {
            if (text != null) {
                secondaryButton.setText(text);
            }
            secondaryButton.setVisible(visible);
            System.out.println(localeService.getMessage("terms.debug.secondary.button.configured", 
                "Secondary button configured: text={0}, visible={1}")
                    .replace("{0}", text != null ? text : "default")
                    .replace("{1}", String.valueOf(visible)));
        }
    }

    /**
     * Sets the dialog type which determines default content and behavior.
     * 
     * @param type The dialog type (terms, privacy, warning, confirmation, etc.)
     */
    public void setDialogType(String type) {
        this.dialogType = type != null ? type : "default";
        System.out.println(localeService.getMessage("terms.debug.type.set", "Dialog type set to: {0}")
            .replace("{0}", this.dialogType));
        
        // Refresh UI to update content based on new type
        if (titleLabel != null) { // Check if UI is initialized
            refreshUI();
        }
    }

    /**
     * Sets the dialog for terms and conditions acceptance.
     */
    public void configureForTermsAcceptance() {
        setDialogType("terms");
        configurePrimaryButton(null, true);
        configureSecondaryButton(null, true);
        System.out.println(localeService.getMessage("terms.debug.configured.terms", "Dialog configured for Terms and Conditions acceptance"));
    }

    /**
     * Sets the dialog for privacy policy acknowledgment.
     */
    public void configureForPrivacyPolicy() {
        setDialogType("privacy");
        configurePrimaryButton(null, true);
        configureSecondaryButton(null, true);
        System.out.println(localeService.getMessage("terms.debug.configured.privacy", "Dialog configured for Privacy Policy"));
    }

    /**
     * Sets the dialog for warning display.
     */
    public void configureForWarning() {
        setDialogType("warning");
        configurePrimaryButton(null, true);
        configureSecondaryButton(null, false);
        System.out.println(localeService.getMessage("terms.debug.configured.warning", "Dialog configured for Warning display"));
    }

    /**
     * Sets the dialog for confirmation.
     */
    public void configureForConfirmation() {
        setDialogType("confirmation");
        configurePrimaryButton(null, true);
        configureSecondaryButton(null, true);
        System.out.println(localeService.getMessage("terms.debug.configured.confirmation", "Dialog configured for Confirmation"));
    }

    /**
     * Sets custom content for the dialog.
     * 
     * @param title Custom title
     * @param message Custom message
     * @param primaryButtonText Custom primary button text
     * @param secondaryButtonText Custom secondary button text (null to hide)
     */
    public void setCustomContent(String title, String message, String primaryButtonText, String secondaryButtonText) {
        setDialogType("custom");
        setTitle(title);
        setMessage(message);
        configurePrimaryButton(primaryButtonText, true);
        configureSecondaryButton(secondaryButtonText, secondaryButtonText != null);
        
        System.out.println(localeService.getMessage("terms.debug.custom.content.set", 
            "Custom dialog content set with title: {0}").replace("{0}", title != null ? title : "null"));
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            Stage stage = (Stage) primaryButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
                System.out.println(localeService.getMessage("terms.debug.dialog.closed", "Dialog closed successfully"));
            } else {
                System.err.println(localeService.getMessage("terms.debug.close.error.no.stage", "Cannot close dialog: no stage found"));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("terms.debug.close.error", "Error closing dialog: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    /**
     * Gets the current dialog type.
     * 
     * @return The current dialog type
     */
    public String getDialogType() {
        return dialogType;
    }

    /**
     * Checks if the primary button is visible.
     * 
     * @return true if primary button is visible
     */
    public boolean isPrimaryButtonVisible() {
        return isPrimaryButtonVisible;
    }

    /**
     * Checks if the secondary button is visible.
     * 
     * @return true if secondary button is visible
     */
    public boolean isSecondaryButtonVisible() {
        return isSecondaryButtonVisible;
    }

    /**
     * Gets the current title text.
     * 
     * @return The current title text
     */
    public String getCurrentTitle() {
        return titleLabel != null ? titleLabel.getText() : null;
    }

    /**
     * Gets the current message text.
     * 
     * @return The current message text
     */
    public String getCurrentMessage() {
        return messageLabel != null ? messageLabel.getText() : null;
    }

    /**
     * Sets the icon for the dialog.
     * 
     * @param iconPath Path to the icon image
     */
    public void setIcon(String iconPath) {
        if (alertIcon != null && iconPath != null) {
            try {
                alertIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath)));
                System.out.println(localeService.getMessage("terms.debug.icon.set", "Dialog icon set to: {0}")
                    .replace("{0}", iconPath));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("terms.debug.icon.error", "Error setting dialog icon: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    /**
     * Shows or hides the icon.
     * 
     * @param visible Whether the icon should be visible
     */
    public void setIconVisible(boolean visible) {
        if (alertIcon != null) {
            alertIcon.setVisible(visible);
            System.out.println(localeService.getMessage("terms.debug.icon.visibility.set", "Dialog icon visibility set to: {0}")
                .replace("{0}", String.valueOf(visible)));
        }
    }
}
