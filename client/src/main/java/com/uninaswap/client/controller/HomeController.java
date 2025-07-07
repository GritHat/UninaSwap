package com.uninaswap.client.controller;


import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class HomeController {
    @FXML
    private HBox UserCardBox;

    private final UserCardController userCard = new UserCardController();


    public void initialize() {
        System.out.println("Home view initialized.");

        userCard.loadUserCardsIntoTab(UserCardBox);
    }

    public void handleUserAction() {
        System.out.println("User action handled in home view.");
    }
}
