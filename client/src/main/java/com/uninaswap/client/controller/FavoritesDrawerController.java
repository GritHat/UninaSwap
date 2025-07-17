package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class FavoritesDrawerController {

    @FXML
    private VBox drawerContainer;

    @FXML
    private Button toggleButton;

    @FXML
    private ImageView toggleIcon;

    // Favorites section
    @FXML
    private TitledPane favoritesPane;

    @FXML
    private VBox favoritesContainer;

    @FXML
    private Label favoritesCountLabel;

    // Following section
    @FXML
    private TitledPane followingPane;

    @FXML
    private VBox followingContainer;

    @FXML
    private Label followingCountLabel;

    // Services
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    // State
    private final BooleanProperty drawerVisible = new SimpleBooleanProperty(false);
    private MainController mainController;

    @FXML
    public void initialize() {
        setupLabels();
        setupDrawerToggle();
        setupExpandablePanes();
        loadFavorites();
        loadFollowing();

        // Hide drawer initially
        drawerContainer.setVisible(false);
        drawerContainer.setManaged(false);
    }

    private void setupLabels() {
        toggleButton.setTooltip(new Tooltip(localeService.getMessage("favorites.drawer.toggle", "Toggle Favorites")));
        favoritesPane.setText(localeService.getMessage("favorites.drawer.favorites", "Favorite Listings"));
        followingPane.setText(localeService.getMessage("favorites.drawer.following", "Following"));
    }

    private void setupDrawerToggle() {
        drawerVisible.addListener((obs, oldVal, newVal) -> {
            drawerContainer.setVisible(newVal);
            drawerContainer.setManaged(newVal);

            // Update toggle icon
            String iconPath = newVal ? "/images/icons/chevron-right.png" : "/images/icons/chevron-left.png";
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                toggleIcon.setImage(icon);
            } catch (Exception e) {
                System.err.println("Could not load toggle icon: " + e.getMessage());
            }
        });
    }

    private void setupExpandablePanes() {
        // Set favorites pane expanded by default
        favoritesPane.setExpanded(true);
        followingPane.setExpanded(false);

        // Add accordion behavior
        favoritesPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && followingPane.isExpanded()) {
                followingPane.setExpanded(false);
            }
        });

        followingPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && favoritesPane.isExpanded()) {
                favoritesPane.setExpanded(false);
            }
        });
    }

    @FXML
    private void handleToggleDrawer() {
        drawerVisible.set(!drawerVisible.get());
    }

    private void loadFavorites() {
        favoritesService.getUserFavorites()
                .thenAccept(favorites -> Platform.runLater(() -> {
                    populateFavoritesList(favorites);
                    updateFavoritesCount(favorites.size());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Failed to load favorites: " + ex.getMessage());
                        updateFavoritesCount(0);
                    });
                    return null;
                });
    }

    private void loadFollowing() {
        // TODO: Implement when FollowerService is available
        // For now, use empty list
        Platform.runLater(() -> {
            populateFollowingList(List.of());
            updateFollowingCount(0);
        });
    }

    private void populateFavoritesList(List<?> favorites) {
        favoritesContainer.getChildren().clear();

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label(localeService.getMessage("favorites.drawer.empty", "No favorites yet"));
            emptyLabel.getStyleClass().add("drawer-empty-label");
            favoritesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Object favorite : favorites) {
            if (favorite instanceof FavoriteViewModel) {
                FavoriteViewModel favoriteViewModel = (FavoriteViewModel) favorite;
                if (favoriteViewModel.getListing() != null) {
                    VBox listingItem = createListingItem(favoriteViewModel.getListing());
                    favoritesContainer.getChildren().add(listingItem);
                }
            }
        }
    }

    private void populateFollowingList(List<UserViewModel> following) {
        followingContainer.getChildren().clear();

        if (following.isEmpty()) {
            Label emptyLabel = new Label(localeService.getMessage("following.drawer.empty", "Not following anyone"));
            emptyLabel.getStyleClass().add("drawer-empty-label");
            followingContainer.getChildren().add(emptyLabel);
            return;
        }

        for (UserViewModel user : following) {
            VBox userItem = createUserItem(user);
            followingContainer.getChildren().add(userItem);
        }
    }

    private VBox createListingItem(ListingViewModel listing) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");

        // Listing image and title
        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");

        // Thumbnail image
        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(40);
        thumbnail.setFitHeight(40);
        thumbnail.setPreserveRatio(true);
        thumbnail.getStyleClass().add("drawer-thumbnail");

        // Load listing image
        if (listing.getItems() != null && !listing.getItems().isEmpty() &&
                listing.getItems().get(0).getImagePath() != null) {
            try {
                Image image = new Image("file:" + listing.getItems().get(0).getImagePath());
                thumbnail.setImage(image);
            } catch (Exception e) {
                setDefaultThumbnail(thumbnail);
            }
        } else {
            setDefaultThumbnail(thumbnail);
        }

        // Title and type
        VBox textContainer = new VBox(2);
        Text title = new Text(listing.getTitle());
        title.getStyleClass().add("drawer-item-title");

        Text type = new Text(listing.getListingTypeValue());
        type.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(title, type);

        header.getChildren().addAll(thumbnail, textContainer);

        // Click handler
        item.setOnMouseClicked(e -> handleListingClick(listing));

        item.getChildren().add(header);
        return item;
    }

    private VBox createUserItem(UserViewModel user) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");

        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");

        // User avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("drawer-avatar");

        // Load user image or use default
        if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
            try {
                Image image = new Image("file:" + user.getProfileImagePath());
                avatar.setImage(image);
            } catch (Exception e) {
                setDefaultAvatar(avatar);
            }
        } else {
            setDefaultAvatar(avatar);
        }

        // User info
        VBox textContainer = new VBox(2);
        Text displayName = new Text(user.getDisplayName());
        displayName.getStyleClass().add("drawer-item-title");

        Text username = new Text("@" + user.getUsername());
        username.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(displayName, username);

        header.getChildren().addAll(avatar, textContainer);

        // Click handler
        item.setOnMouseClicked(e -> handleUserClick(user));

        item.getChildren().add(header);
        return item;
    }

    private void setDefaultThumbnail(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default thumbnail: " + e.getMessage());
        }
    }

    private void setDefaultAvatar(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/default_profile.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
        }
    }

    private void handleListingClick(ListingViewModel listing) {
        try {
            Parent listingDetailsView = navigationService.loadListingDetails(listing);
            mainController.setContent(listingDetailsView);
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open listing details: " + e.getMessage());
        }
    }

    private void handleUserClick(UserViewModel user) {
        try {
            Parent profileView = navigationService.loadProfileView();
            mainController.setContent(profileView);
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open user profile: " + e.getMessage());
        }
    }

    private void updateFavoritesCount(int count) {
        String countText = localeService.getMessage("favorites.drawer.count", "({0})", count);
        favoritesCountLabel.setText(countText);
    }

    private void updateFollowingCount(int count) {
        String countText = localeService.getMessage("following.drawer.count", "({0})", count);
        followingCountLabel.setText(countText);
    }

    public void refreshData() {
        loadFavorites();
        loadFollowing();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean isDrawerVisible() {
        return drawerVisible.get();
    }

    public BooleanProperty drawerVisibleProperty() {
        return drawerVisible;
    }

    public void showDrawer() {
        drawerVisible.set(true);
    }

    public void hideDrawer() {
        drawerVisible.set(false);
    }
}