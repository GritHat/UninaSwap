package com.uninaswap.client.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Utility class to simplify the process of creating and showing alerts
 */
public class AlertHelper {

    /**
     * Default constructor
     */
    public AlertHelper() {
    }

    /**
     * Show an information alert with the provided title and message
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showInformationAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Show a warning alert with the provided title and message
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showWarningAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Show an error alert with the provided title and message
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showErrorAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Show an error alert for an exception, with a stack trace
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param ex      The exception to show
     */
    public static void showExceptionAlert(String title, String header, Throwable ex) {
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

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.add(textArea, 0, 0);
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(false);

            alert.showAndWait();
        });
    }

    /**
     * Create a confirmation dialog that returns a standard Alert object
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     * @return An Alert of type CONFIRMATION that the caller can use with
     *         showAndWait()
     */
    public static Alert createConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    /**
     * Shows a confirmation dialog and returns true if OK was pressed
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = createConfirmationDialog(title, header, content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Show an information alert on a specific window
     * 
     * @param owner   The owner window for the alert
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     */
    public static void showInformationAlert(Window owner, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initOwner(owner);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Show a confirmation alert and return the user's choice
     * 
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     * @return true if the user clicked OK/Yes, false if they clicked Cancel/No
     */
    public static boolean showConfirmationAlert(String title, String header, String content) {
        final boolean[] result = { false };

        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

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
     * 
     * @param title       The title of the alert
     * @param header      The header text of the alert
     * @param content     The content text of the alert
     * @param confirmText Text for the confirm button
     * @param cancelText  Text for the cancel button
     * @return true if the user clicked the confirm button, false otherwise
     */
    public static boolean showConfirmationAlert(String title, String header, String content,
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

    /**
     * Show a confirmation alert on a specific window
     * 
     * @param owner   The owner window for the alert
     * @param title   The title of the alert
     * @param header  The header text of the alert
     * @param content The content text of the alert
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmationAlert(Window owner, String title, String header, String content) {
        final boolean[] result = { false };

        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.initOwner(owner);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);

            Optional<ButtonType> buttonClicked = alert.showAndWait();
            return buttonClicked.isPresent() && buttonClicked.get() == ButtonType.OK;
        } else {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.initOwner(owner);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);

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
}