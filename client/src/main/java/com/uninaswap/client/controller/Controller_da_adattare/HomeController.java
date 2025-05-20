package com.uninaswap.client.controller.Controller_da_adattare;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class HomeController {

    @FXML private HBox articoliPreferitiBox;
    @FXML private HBox utentiPreferitiBox;
    @FXML private HBox astePreferiteBox;

    @FXML
    public void initialize() {
        // Esempio: popola articoli preferiti con dati fittizi
        articoliPreferitiBox.getChildren().clear();
        for (int i = 1; i <= 3; i++) {
            VBox card = new VBox();
            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/new/UI_img/spermatozoi.png")));
            img.setFitWidth(100);
            img.setFitHeight(100);
            Text titolo = new Text("Articolo " + i);
            Text prezzo = new Text((10 * i) + "€");
            card.getChildren().addAll(img, titolo, prezzo);
            articoliPreferitiBox.getChildren().add(card);
        }

        // Esempio: popola utenti preferiti con dati fittizi
        utentiPreferitiBox.getChildren().clear();
        for (int i = 1; i <= 2; i++) {
            VBox userBox = new VBox();
            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/new/UI_img/spermatozoi.png")));
            img.setFitWidth(100);
            img.setFitHeight(100);
            Text nome = new Text("Utente " + i);
            userBox.getChildren().addAll(img, nome);
            utentiPreferitiBox.getChildren().add(userBox);
        }

        // Esempio: popola aste preferite con dati fittizi
        astePreferiteBox.getChildren().clear();
        for (int i = 1; i <= 2; i++) {
            VBox astaBox = new VBox();
            Text titolo = new Text("Asta " + i);
            Text offerta = new Text("Offerta: " + (i * 50) + "€");
            astaBox.getChildren().addAll(titolo, offerta);
            astePreferiteBox.getChildren().add(astaBox);
        }
    }

    @FXML
    public void apriIlProfilo() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void apriPreferiti() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void apriInventario() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void aggiungiAnnuncio() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void apriILeNotifiche() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void apriLeAste() {
        // Implementa la logica di apertura profilo
    }

    @FXML
    public void eseguiIlLogout(MouseEvent event) {
        ExceptionController.cambiaScena(event, "/new/fxml_new/LoginView.fxml", "UninaSwap - Login");
    }

    @FXML
    public void apriImpostazioni(MouseEvent event) {
        ExceptionController.cambiaScena(event, "/new/fxml_new/ImpostazioniView.fxml", "UninaSwap - Impostazioni");
    }
}
