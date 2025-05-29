package com.uninaswap.client.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Utility class for creating modal dialogs that demand user attention
 * and prevent interaction with the main application until dismissed.
 */
public class ModalDialogUtil {

    /**
     * Creates and shows a modal dialog with the given title and message.
     * This dialog blocks interaction with all other application windows until closed.
     *
     * @param owner The owner window, can be null
     * @param title The dialog title
     * @param message The dialog message
     * @param buttonText Text for the dismiss button
     */
    public static void showModalDialog(Window owner, String title, String message, String buttonText) {
        Platform.runLater(() -> {
            // Create a new stage for the modal dialog
            final Stage dialog = new Stage();
            
            // Make it application modal - it must be dealt with before the application can continue
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            // If we have an owner, set it and make the dialog positioned relative to the owner
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            // Create the UI components
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setMaxWidth(400); // Set a reasonable max width for long messages
            
            Button closeButton = new Button(buttonText);
            closeButton.setOnAction(e -> dialog.close());
            closeButton.setDefaultButton(true); // Make it the default button (responds to Enter)
            
            // Container for the close button
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(20, 0, 0, 0));
            buttonBox.getChildren().add(closeButton);
            
            // Main container
            VBox content = new VBox(20); // 20px spacing between elements
            content.setPadding(new Insets(30));
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(titleLabel, messageLabel, buttonBox);
            
            // Set a minimum size for the dialog
            content.setMinWidth(300);
            content.setMinHeight(200);
            
            // Create scene and add CSS styling
            Scene scene = new Scene(content);
            scene.getStylesheets().add(ModalDialogUtil.class.getResource("/css/styles.css").toExternalForm());
            
            // Add a specific style class to the content pane for custom styling
            content.getStyleClass().add("modal-dialog");
            
            // Configure the dialog stage
            dialog.setScene(scene);
            dialog.setTitle(title);
            dialog.setResizable(false);
            
            // Show the dialog and wait for it to be closed
            dialog.showAndWait();
        });
    }
    
    /**
     * Shows a modal error dialog.
     *
     * @param owner The owner window
     * @param title The error title
     * @param message The error message
     */
    public static void showErrorDialog(Window owner, String title, String message) {
        showModalDialog(owner, title, message, "OK");
    }
    
    /**
     * Shows a modal confirmation dialog with custom buttons.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The dialog message
     * @param confirmButtonText Text for the confirm button
     * @param cancelButtonText Text for the cancel button
     * @param onConfirm Action to perform on confirm
     * @return true if confirmed, false if canceled
     */
    public static boolean showConfirmationDialog(Window owner, String title, String message, 
                                                String confirmButtonText, String cancelButtonText,
                                                Runnable onConfirm) {
        final boolean[] result = {false};
        
        Platform.runLater(() -> {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setMaxWidth(400);
            
            Button confirmButton = new Button(confirmButtonText);
            confirmButton.getStyleClass().add("button-primary");
            confirmButton.setOnAction(e -> {
                result[0] = true;
                dialog.close();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            });
            
            Button cancelButton = new Button(cancelButtonText);
            cancelButton.setOnAction(e -> {
                result[0] = false;
                dialog.close();
            });
            
            // Container for the buttons
            HBox buttonBox = new HBox(20); // 20px spacing between buttons
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(20, 0, 0, 0));
            buttonBox.getChildren().addAll(cancelButton, confirmButton);
            
            // Main container
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(titleLabel, messageLabel, buttonBox);
            
            content.setMinWidth(400);
            content.setMinHeight(200);
            
            Scene scene = new Scene(content);
            scene.getStylesheets().add(ModalDialogUtil.class.getResource("/css/styles.css").toExternalForm());
            
            content.getStyleClass().add("modal-dialog");
            
            dialog.setScene(scene);
            dialog.setTitle(title);
            dialog.setResizable(false);
            
            dialog.showAndWait();
        });
        
        return result[0];
    }
}
