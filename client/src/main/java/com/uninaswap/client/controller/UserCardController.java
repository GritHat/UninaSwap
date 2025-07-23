package com.uninaswap.client.controller;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.util.AlertHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.List;

/**
 * 
 */
public class UserCardController {

    /**
     * 
     */
    @FXML
    private VBox itemCard;
    /**
     * 
     */
    @FXML
    private ImageView itemImage;
    /**
     * 
     */
    @FXML
    private Text itemName;

    /**
     * 
     */
    private UserDTO user;
    /**
     * 
     */
    private List<UserDTO> users;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();

    /**
     * 
     */
    public UserCardController() {
    }

    /**
     * @param user
     */
    public UserCardController(UserDTO user) {
        this.user = user;
    }
    /**
     * @param <T>
     * @param container
     */
    public <T extends Pane> void loadUserCardsIntoTab(T container) {
        container.getChildren().clear();
        try {
            for (UserDTO user : users) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserCardView.fxml"));
                    loader.setResources(localeService.getResourceBundle());
                    loader.setController(new UserCardController(user));
                    container.getChildren().add(loader.load());
                } catch (IOException e) {
                    AlertHelper.showErrorAlert(
                            localeService.getMessage("user.card.load.error.title"),
                            localeService.getMessage("user.card.load.error.header"),
                            e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("La lista è vuota o non inizializzata");
        }

    }

    /**
     * @param event
     */
    @FXML
    private void openUserDetails(MouseEvent event) {
        if (user != null) {
        }
    }
}
