package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.SupportService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.util.AlertHelper;

import java.net.URL;
import java.util.ResourceBundle;

public class SupportController implements Initializable, Refreshable {
    
    @FXML 
    private TextField subjectField;
    @FXML 
    private TextArea messageField;
    @FXML 
    private Button sendButton;

    // Additional FXML elements for localization
    @FXML
    private Text supportCenterText;
    @FXML
    private Text quickGuideText;
    @FXML
    private Text faqText;
    @FXML
    private Text contactUsText;
    @FXML
    private Text otherContactMethodsText;
    @FXML
    private Text platformDescriptionText;
    @FXML
    private Text contactDescriptionText;

    @FXML
    private Label subjectLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label hoursLabel;
    @FXML
    private Label knowledgeBaseLabel;

    @FXML
    private Hyperlink beginnersGuideLink;
    @FXML
    private Hyperlink sellingGuideLink;
    @FXML
    private Hyperlink auctionsGuideLink;
    @FXML
    private Hyperlink rulesGuideLink;
    @FXML
    private Hyperlink emailAddressLink;
    @FXML
    private Hyperlink knowledgeBaseLink;

    @FXML
    private Text hoursValueText;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final SupportService supportService = SupportService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("support.debug.initialized", "Support controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update main section titles
        if (supportCenterText != null) {
            supportCenterText.setText(localeService.getMessage("support.center", "Support Center"));
        }
        if (quickGuideText != null) {
            quickGuideText.setText(localeService.getMessage("support.quick.guide", "Quick Start Guide"));
        }
        if (faqText != null) {
            faqText.setText(localeService.getMessage("support.faq", "Frequently Asked Questions"));
        }
        if (contactUsText != null) {
            contactUsText.setText(localeService.getMessage("support.contact.us", "Contact Us"));
        }
        if (otherContactMethodsText != null) {
            otherContactMethodsText.setText(localeService.getMessage("support.other.contact.methods", "Other Contact Methods"));
        }

        // Update description texts
        if (platformDescriptionText != null) {
            platformDescriptionText.setText(localeService.getMessage("support.platform.description", 
                "UninaSwap is a platform for students to exchange items, sell products, and participate in auctions. Here you'll find everything you need to get started."));
        }
        if (contactDescriptionText != null) {
            contactDescriptionText.setText(localeService.getMessage("support.contact.description",
                "Can't find the answer you're looking for? Send us a message and we'll get back to you as soon as possible."));
        }

        // Update form labels
        if (subjectLabel != null) {
            subjectLabel.setText(localeService.getMessage("support.contact.subject", "Subject:"));
        }
        if (messageLabel != null) {
            messageLabel.setText(localeService.getMessage("support.contact.message", "Message:"));
        }

        // Update field prompts
        if (subjectField != null) {
            subjectField.setPromptText(localeService.getMessage("support.contact.subject.prompt", "Enter the subject of your request"));
        }
        if (messageField != null) {
            messageField.setPromptText(localeService.getMessage("support.contact.message.prompt", "Describe your issue or question in detail"));
        }

        // Update button text
        if (sendButton != null) {
            sendButton.setText(localeService.getMessage("support.contact.send", "Send Message"));
        }

        // Update guide links
        if (beginnersGuideLink != null) {
            beginnersGuideLink.setText(localeService.getMessage("support.guide.beginners", "Beginner's Guide"));
        }
        if (sellingGuideLink != null) {
            sellingGuideLink.setText(localeService.getMessage("support.guide.selling", "How to Sell Items"));
        }
        if (auctionsGuideLink != null) {
            auctionsGuideLink.setText(localeService.getMessage("support.guide.auctions", "Auction Guide"));
        }
        if (rulesGuideLink != null) {
            rulesGuideLink.setText(localeService.getMessage("support.guide.rules", "Community Rules"));
        }

        // Update contact info labels
        if (emailLabel != null) {
            emailLabel.setText(localeService.getMessage("support.contact.email", "Email Support"));
        }
        if (hoursLabel != null) {
            hoursLabel.setText(localeService.getMessage("support.contact.hours", "Support Hours"));
        }
        if (knowledgeBaseLabel != null) {
            knowledgeBaseLabel.setText(localeService.getMessage("support.full.faq", "Knowledge Base"));
        }

        // Update contact info values
        if (emailAddressLink != null) {
            emailAddressLink.setText(localeService.getMessage("support.contact.email.address", "support@uninaswap.com"));
        }
        if (hoursValueText != null) {
            hoursValueText.setText(localeService.getMessage("support.contact.hours.value", "Mon-Fri 9:00-18:00"));
        }
        if (knowledgeBaseLink != null) {
            knowledgeBaseLink.setText(localeService.getMessage("support.knowledge.base.link", "View Full Documentation"));
        }
    }
   
    @FXML
    public void handleSendMessage(ActionEvent event) {
        System.out.println(localeService.getMessage("support.debug.send.attempt", "Support message send attempt"));
        
        if (validateForm()) {
            try {
                String subject = subjectField.getText().trim();
                String message = messageField.getText().trim();
                String userEmail = sessionService.isLoggedIn() ? 
                    sessionService.getUser().getEmail() : 
                    localeService.getMessage("support.anonymous.user", "Anonymous User");

                // Send support request through service
                supportService.sendSupportRequest(subject, message, userEmail)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            AlertHelper.showInformationAlert(
                                localeService.getMessage("support.request.sent.title", "Request Sent"),
                                localeService.getMessage("support.request.sent.header", "Your request has been sent"),
                                localeService.getMessage("support.request.sent.content", 
                                    "We'll respond as soon as possible to the email address associated with your account.")
                            );
                            
                            // Clear the form
                            subjectField.clear();
                            messageField.clear();
                            
                            System.out.println(localeService.getMessage("support.debug.send.success", 
                                "Support request sent successfully"));
                        } else {
                            AlertHelper.showErrorAlert(
                                localeService.getMessage("support.request.failed.title", "Send Failed"),
                                localeService.getMessage("support.request.failed.header", "Could not send request"),
                                localeService.getMessage("support.request.failed.content", 
                                    "Please try again later or contact us directly via email.")
                            );
                            System.err.println(localeService.getMessage("support.debug.send.failed", 
                                "Support request failed: {0}").replace("{0}", response.getErrorMessage()));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            AlertHelper.showErrorAlert(
                                localeService.getMessage("support.request.error.title", "Connection Error"),
                                localeService.getMessage("support.request.error.header", "Unable to send request"),
                                localeService.getMessage("support.request.error.content", 
                                    "Please check your connection and try again.")
                            );
                            System.err.println(localeService.getMessage("support.debug.send.exception", 
                                "Support request exception: {0}").replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
                    
            } catch (Exception e) {
                AlertHelper.showErrorAlert(
                    localeService.getMessage("support.request.error.title", "Connection Error"),
                    localeService.getMessage("support.request.error.header", "Unable to send request"),
                    localeService.getMessage("support.request.error.content", 
                        "Please check your connection and try again.")
                );
                System.err.println(localeService.getMessage("support.debug.send.error", 
                    "Error sending support request: {0}").replace("{0}", e.getMessage()));
            }
        }
    }
    
    private boolean validateForm() {
        String subject = subjectField.getText().trim();
        String message = messageField.getText().trim();
        
        if (subject.isEmpty()) {
            showValidationError(
                localeService.getMessage("support.validation.subject.required.title", "Subject Required"),
                localeService.getMessage("support.validation.subject.required.message", "Please enter the subject of your request.")
            );
            System.err.println(localeService.getMessage("support.debug.validation.subject.empty", 
                "Validation failed: subject is empty"));
            return false;
        }
        
        if (subject.length() < 5) {
            showValidationError(
                localeService.getMessage("support.validation.subject.length.title", "Subject Too Short"),
                localeService.getMessage("support.validation.subject.length.message", "Subject must be at least 5 characters long.")
            );
            System.err.println(localeService.getMessage("support.debug.validation.subject.short", 
                "Validation failed: subject too short"));
            return false;
        }
        
        if (message.isEmpty()) {
            showValidationError(
                localeService.getMessage("support.validation.message.required.title", "Message Required"),
                localeService.getMessage("support.validation.message.required.message", "Please enter the text of your message.")
            );
            System.err.println(localeService.getMessage("support.debug.validation.message.empty", 
                "Validation failed: message is empty"));
            return false;
        }
        
        if (message.length() < 20) {
            showValidationError(
                localeService.getMessage("support.validation.message.length.title", "Message Too Short"),
                localeService.getMessage("support.validation.message.length.message", "Message must contain at least 20 characters.")
            );
            System.err.println(localeService.getMessage("support.debug.validation.message.short", 
                "Validation failed: message too short"));
            return false;
        }

        if (message.length() > 2000) {
            showValidationError(
                localeService.getMessage("support.validation.message.max.length.title", "Message Too Long"),
                localeService.getMessage("support.validation.message.max.length.message", "Message cannot exceed 2000 characters.")
            );
            System.err.println(localeService.getMessage("support.debug.validation.message.long", 
                "Validation failed: message too long"));
            return false;
        }
        
        System.out.println(localeService.getMessage("support.debug.validation.success", 
            "Form validation passed"));
        return true;
    }
    
    private void showValidationError(String title, String message) {
        AlertHelper.showWarningAlert(title, null, message);
    }

    // Event handlers for guide links
    @FXML
    public void handleBeginnersGuide(ActionEvent event) {
        try {
            // Navigate to beginners guide or open external link
            System.out.println(localeService.getMessage("support.debug.guide.beginners", "Opening beginners guide"));
            // Implementation depends on how guides are handled in your app
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.guide.error", 
                "Error opening guide: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void handleSellingGuide(ActionEvent event) {
        try {
            System.out.println(localeService.getMessage("support.debug.guide.selling", "Opening selling guide"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.guide.error", 
                "Error opening guide: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void handleAuctionsGuide(ActionEvent event) {
        try {
            System.out.println(localeService.getMessage("support.debug.guide.auctions", "Opening auctions guide"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.guide.error", 
                "Error opening guide: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void handleRulesGuide(ActionEvent event) {
        try {
            System.out.println(localeService.getMessage("support.debug.guide.rules", "Opening community rules"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.guide.error", 
                "Error opening guide: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void handleEmailLink(ActionEvent event) {
        try {
            // Open email client or copy email to clipboard
            System.out.println(localeService.getMessage("support.debug.email.clicked", "Email link clicked"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.email.error", 
                "Error handling email link: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    public void handleKnowledgeBase(ActionEvent event) {
        try {
            System.out.println(localeService.getMessage("support.debug.knowledge.base", "Opening knowledge base"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("support.debug.knowledge.base.error", 
                "Error opening knowledge base: {0}").replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    public void clearForm() {
        if (subjectField != null) {
            subjectField.clear();
        }
        if (messageField != null) {
            messageField.clear();
        }
        System.out.println(localeService.getMessage("support.debug.form.cleared", "Support form cleared"));
    }

    public void setSubject(String subject) {
        if (subjectField != null) {
            subjectField.setText(subject != null ? subject : "");
            System.out.println(localeService.getMessage("support.debug.subject.set", 
                "Subject set to: {0}").replace("{0}", subject != null ? subject : "empty"));
        }
    }

    public void setMessage(String message) {
        if (messageField != null) {
            messageField.setText(message != null ? message : "");
            System.out.println(localeService.getMessage("support.debug.message.set", 
                "Message set to: {0}").replace("{0}", message != null ? "provided" : "empty"));
        }
    }

    public void focusSubjectField() {
        if (subjectField != null) {
            Platform.runLater(() -> {
                subjectField.requestFocus();
                System.out.println(localeService.getMessage("support.debug.subject.focused", "Subject field focused"));
            });
        }
    }

    public boolean isFormValid() {
        return validateForm();
    }

    public String getCurrentSubject() {
        return subjectField != null ? subjectField.getText().trim() : "";
    }

    public String getCurrentMessage() {
        return messageField != null ? messageField.getText().trim() : "";
    }
}
