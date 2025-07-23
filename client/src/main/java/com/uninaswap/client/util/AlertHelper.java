package com.uninaswap.client.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Utility class to simplify the process of creating and showing alerts
 * with consistent styling matching the UninaSwap design guidelines
 */
public class AlertHelper {

    /**
     * Default constructor
     */
    public AlertHelper() {
    }

    /**
     * Configure an alert with UninaSwap styling and proper focus management
     */
    private static void configureAlert(Alert alert, Window owner) {
        // Set owner to ensure proper layering
        if (owner != null) {
            alert.initOwner(owner);
        }
        
        // Set modality to ensure the alert stays on top and blocks interaction
        alert.initModality(Modality.APPLICATION_MODAL);
        
        // Apply custom styling
        alert.getDialogPane().getStylesheets().add(
            AlertHelper.class.getResource("/css/styles.css").toExternalForm()
        );
        
        // Add custom style classes for consistent theming
        alert.getDialogPane().getStyleClass().addAll("alert-dialog");
        
        // Force white background
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-background-radius: 12px;");
        
        // Ensure the alert is always on top
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        
        // Apply styling after the dialog is shown to ensure buttons are created
        Platform.runLater(() -> {
            // Style buttons according to project guidelines
            alert.getDialogPane().lookupAll(".button").forEach(node -> {
                if (node instanceof javafx.scene.control.Button button) {
                    // Remove default button styling
                    button.getStyleClass().removeAll("dialog-button");
                    
                    // Get button type to determine styling
                    String buttonText = button.getText().toLowerCase();
                    
                    // Style based on button text and type
                    if (buttonText.contains("ok") || buttonText.contains("sì") || 
                        buttonText.contains("conferma") || buttonText.contains("yes") ||
                        buttonText.contains("acquista") || buttonText.contains("invia")) {
                        
                        // Primary button styling
                        button.setStyle(
                            "-fx-background-color: -fx-primary; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 20px; " +
                            "-fx-border-radius: 20px; " +
                            "-fx-padding: 8px 15px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 3, 0, 0, 1);"
                        );
                        
                        // Add hover effects
                        button.setOnMouseEntered(e -> {
                            button.setStyle(
                                "-fx-background-color: -fx-primary-light; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 20px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-padding: 8px 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);"
                            );
                        });
                        
                        button.setOnMouseExited(e -> {
                            button.setStyle(
                                "-fx-background-color: -fx-primary; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 20px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-padding: 8px 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 3, 0, 0, 1);"
                            );
                        });
                        
                    } else {
                        // Secondary button styling
                        button.setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: -fx-text-mid; " +
                            "-fx-border-color: #dee2e6; " +
                            "-fx-border-width: 1px; " +
                            "-fx-background-radius: 20px; " +
                            "-fx-border-radius: 20px; " +
                            "-fx-padding: 8px 15px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
                        );
                        
                        // Add hover effects
                        button.setOnMouseEntered(e -> {
                            button.setStyle(
                                "-fx-background-color: -fx-primary-ultralight; " +
                                "-fx-text-fill: -fx-text-dark; " +
                                "-fx-border-color: -fx-primary-light; " +
                                "-fx-border-width: 1px; " +
                                "-fx-background-radius: 20px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-padding: 8px 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);"
                            );
                        });
                        
                        button.setOnMouseExited(e -> {
                            button.setStyle(
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: -fx-text-mid; " +
                                "-fx-border-color: #dee2e6; " +
                                "-fx-border-width: 1px; " +
                                "-fx-background-radius: 20px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-padding: 8px 15px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand;"
                            );
                        });
                    }
                }
            });
            
            // Style the dialog content area
            alert.getDialogPane().lookup(".content").setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 0 0 12px 12px; " +
                "-fx-padding: 20px;"
            );
            
            // Style the header area
            if (alert.getDialogPane().lookup(".header-panel") != null) {
                alert.getDialogPane().lookup(".header-panel").setStyle(
                    "-fx-background-color: -fx-background-light; " +
                    "-fx-background-radius: 12px 12px 0 0; " +
                    "-fx-padding: 20px; " +
                    "-fx-border-color: #e9ecef; " +
                    "-fx-border-width: 0 0 1px 0;"
                );
            }
            
            // Style the button bar
            if (alert.getDialogPane().lookup(".button-bar") != null) {
                alert.getDialogPane().lookup(".button-bar").setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 0 0 12px 12px; " +
                    "-fx-padding: 15px 20px 20px 20px;"
                );
            }
        });
        
        // Set minimum size for better appearance
        alert.getDialogPane().setMinHeight(200);
        alert.getDialogPane().setPrefWidth(400);
        
        // Center the alert on screen or owner
        Platform.runLater(() -> {
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
            
            // Ensure focus and bring to front
            stage.toFront();
            stage.requestFocus();
        });
    }

    /**
     * Show an information alert with the provided title and message
     */
    public static void showInformationAlert(String title, String header, String content) {
        showInformationAlert(null, title, header, content);
    }

    /**
     * Show an information alert on a specific window
     */
    public static void showInformationAlert(Window owner, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            
            configureAlert(alert, owner);
            alert.showAndWait();
        });
    }

    /**
     * Show a warning alert with the provided title and message
     */
    public static void showWarningAlert(String title, String header, String content) {
        showWarningAlert(null, title, header, content);
    }

    /**
     * Show a warning alert on a specific window
     */
    public static void showWarningAlert(Window owner, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            
            configureAlert(alert, owner);
            alert.showAndWait();
        });
    }

    /**
     * Show an error alert with the provided title and message
     */
    public static void showErrorAlert(String title, String header, String content) {
        showErrorAlert(null, title, header, content);
    }

    /**
     * Show an error alert on a specific window
     */
    public static void showErrorAlert(Window owner, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            
            configureAlert(alert, owner);
            alert.showAndWait();
        });
    }

    /**
     * Show an error alert for an exception, with a stack trace
     */
    public static void showExceptionAlert(String title, String header, Throwable ex) {
        showExceptionAlert(null, title, header, ex);
    }

    /**
     * Show an error alert for an exception on a specific window, with a stack trace
     */
    public static void showExceptionAlert(Window owner, String title, String header, Throwable ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(ex.getMessage());
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.getStyleClass().add("exception-text-area");
            
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.add(textArea, 0, 0);
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(false);
            
            // Increase size for exception dialogs
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(400);

            configureAlert(alert, owner);
            alert.showAndWait();
        });
    }

    /**
     * Create a confirmation dialog that returns a standard Alert object
     */
    public static Alert createConfirmationDialog(String title, String header, String content) {
        return createConfirmationDialog(null, title, header, content);
    }

    /**
     * Create a confirmation dialog on a specific window that returns a standard Alert object
     */
    public static Alert createConfirmationDialog(Window owner, String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        configureAlert(alert, owner);
        return alert;
    }

    /**
     * Shows a confirmation dialog and returns true if OK was pressed
     */
    public static boolean showConfirmationDialog(String title, String header, String content) {
        return showConfirmationDialog(null, title, header, content);
    }

    /**
     * Shows a confirmation dialog on a specific window and returns true if OK was pressed
     */
    public static boolean showConfirmationDialog(Window owner, String title, String header, String content) {
        Alert alert = createConfirmationDialog(owner, title, header, content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Show a confirmation alert and return the user's choice
     */
    public static boolean showConfirmationAlert(String title, String header, String content) {
        return showConfirmationAlert(null, title, header, content);
    }

    /**
     * Show a confirmation alert on a specific window and return the user's choice
     */
    public static boolean showConfirmationAlert(Window owner, String title, String header, String content) {
        final boolean[] result = { false };

        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

            configureAlert(alert, owner);

            Optional<ButtonType> buttonClicked = alert.showAndWait();
            return buttonClicked.isPresent() && buttonClicked.get() == ButtonType.OK;
        } else {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

                    configureAlert(alert, owner);

                    Optional<ButtonType> buttonClicked = alert.showAndWait();
                    result[0] = buttonClicked.isPresent() && buttonClicked.get() == ButtonType.OK;
                } finally {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            return result[0];
        }
    }

    /**
     * Show a confirmation alert with custom button text
     */
    public static boolean showConfirmationAlert(String title, String header, String content,
            String confirmText, String cancelText) {
        return showConfirmationAlert(null, title, header, content, confirmText, cancelText);
    }

    /**
     * Show a confirmation alert with custom button text on a specific window
     */
    public static boolean showConfirmationAlert(Window owner, String title, String header, String content,
            String confirmText, String cancelText) {
        final boolean[] result = { false };

        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            
            ButtonType confirmButton = new ButtonType(confirmText);
            ButtonType cancelButton = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(confirmButton, cancelButton);

            configureAlert(alert, owner);

            Optional<ButtonType> buttonClicked = alert.showAndWait();
            return buttonClicked.isPresent() && buttonClicked.get() == confirmButton;
        } else {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);

                    ButtonType confirmButton = new ButtonType(confirmText);
                    ButtonType cancelButton = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(confirmButton, cancelButton);

                    configureAlert(alert, owner);

                    Optional<ButtonType> buttonClicked = alert.showAndWait();
                    result[0] = buttonClicked.isPresent() && buttonClicked.get() == confirmButton;
                } finally {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            return result[0];
        }
    }
}