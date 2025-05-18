package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
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
    public void apri_profilo() {
        // Implementa la logica di apertura profilo
    }
}
