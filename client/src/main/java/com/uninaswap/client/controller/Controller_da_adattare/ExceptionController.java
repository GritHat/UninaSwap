package com.uninaswap.client.controller.Controller_da_adattare;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;

public class ExceptionController {

    // Cambio scena tramite MouseEvent (es. click su ImageView)
    public static void cambiaScena(MouseEvent event, String fxmlPath, String titolo) {
        cambiaScenaGenerico((Node) event.getSource(), fxmlPath, titolo);
    }

    // Cambio scena tramite ActionEvent (es. click su Button o Hyperlink)
    public static void cambiaScena(ActionEvent event, String fxmlPath, String titolo) {
        cambiaScenaGenerico((Node) event.getSource(), fxmlPath, titolo);
    }

    // Metodo generico privato
    private static void cambiaScenaGenerico(Node source, String fxmlPath, String titolo) {
        try {
            System.out.println("Tentativo di caricare: " + fxmlPath);
            java.net.URL resource = ExceptionController.class.getResource(fxmlPath);
            System.out.println("URL risorsa: " + resource);

            if (resource == null) {
                throw new IOException("FXML file not found at path: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle(titolo);
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della pagina: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void handleException(Exception e, String messageKey) {
        // You can log the exception and/or show an error dialog here
        System.err.println("Exception: " + e.getMessage() + " | Message Key: " + messageKey);
        e.printStackTrace();
        // Optionally, integrate with your UI to show the error to the user
    }

}
