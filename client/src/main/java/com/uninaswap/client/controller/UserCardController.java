package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.client.service.NavigationService;

public class UserCardController {

    @FXML private VBox itemCard;
    @FXML private ImageView itemImage;
    @FXML private Text itemName;

    private UserDTO user;

    private final NavigationService navigationService = NavigationService.getInstance();
/* da implementare
    public void setUser(UserDTO user) {
        this.user = user;
        itemName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            itemImage.setImage(new Image(user.getProfileImageUrl(), true));
        }
    }
*/
    @FXML
    private void openUserDetails(MouseEvent event) {
        if (user != null) {
            try {
                navigationService.loadProfileView();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                // Optionally, show an error dialog to the user
            }
        }
    }
}
