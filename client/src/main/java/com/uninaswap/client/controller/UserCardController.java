package com.uninaswap.client.controller;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.util.AlertHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.List;

public class UserCardController {

    @FXML
    private VBox itemCard;
    @FXML
    private ImageView itemImage;
    @FXML
    private Text itemName;

    private UserDTO user;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    //TODO: Check if it works
    public UserCardController() {
    }

    public UserCardController(UserDTO user) {
        this.user = user;
    }

    public void loadUserCardsIntoTab(FlowPane container, List<UserDTO> user) {
        container.getChildren().clear();
        for (UserDTO users : user) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserCardView.fxml"));
                loader.setResources(localeService.getResourceBundle());
                loader.setController(new UserCardController(users));
                container.getChildren().add(loader.load());
            } catch (IOException e) {
                AlertHelper.showErrorAlert(
                        localeService.getMessage("user.card.load.error.title"),
                        localeService.getMessage("user.card.load.error.header"),
                        e.getMessage());
            }
        }
    }

    @FXML
    private void openUserDetails(MouseEvent event) {
        if (user != null) {
            //TODO: open user profile detail
        }
    }
}
