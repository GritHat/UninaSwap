package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

/**
 * 
 */
public class FavoritesDrawerController {

    /**
     * 
     */
    @FXML
    private HBox externalDrawerContainer;
    /**
     * 
     */
    @FXML
    private VBox drawerContainer;
    /**
     * 
     */
    @FXML
    private TitledPane favoritesPane;
    /**
     * 
     */
    @FXML
    private VBox favoritesContainer;
    /**
     * 
     */
    @FXML
    private Label favoritesCountLabel;
    /**
     * 
     */
    @FXML
    private TitledPane followingPane;
    /**
     * 
     */
    @FXML
    private VBox followingContainer;
    /**
     * 
     */
    @FXML
    private Label followingCountLabel;

    /**
     * 
     */
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    /**
     * 
     */
    private final BooleanProperty drawerVisible = new SimpleBooleanProperty(false);
    /**
     * 
     */
    private MainController mainController;
    /**
     * 
     */
    private ObservableList<FavoriteViewModel> userFavorites;
    /**
     * 
     */
    private Timeline showAnimation;
    /**
     * 
     */
    private Timeline hideAnimation;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupDrawerToggle();
        setupExpandablePanes();
        setupFavoritesObservableList();
        loadFollowing();

        
        drawerContainer.setVisible(false);
        drawerContainer.setManaged(false);
        drawerContainer.getStyleClass().add("drawer-hidden");
        
        
        externalDrawerContainer.setMouseTransparent(true);
        
        
        drawerContainer.getStyleClass().add("drawer-content-animated");
    }

    /**
     * 
     */
    private void setupDrawerToggle() {
        drawerVisible.addListener((_, _, newVal) -> {
            if (newVal) {
                showDrawerAnimated();
            } else {
                hideDrawerAnimated();
            }
            
            
            if (mainController != null) {
                mainController.onFavoritesDrawerStateChanged();
            }
        });
    }

    /**
     * Show drawer with slide-in animation
     */
    /**
     * 
     */
    private void showDrawerAnimated() {
        if (hideAnimation != null) {
            hideAnimation.stop();
        }
        drawerContainer.setVisible(true);
        drawerContainer.setManaged(true);
        externalDrawerContainer.setMouseTransparent(false);
        drawerContainer.getStyleClass().removeAll("drawer-hidden");
        drawerContainer.getStyleClass().add("drawer-visible");
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), drawerContainer);
        slideIn.setFromX(280);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), drawerContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition showAnimationTransition = new ParallelTransition(slideIn, fadeIn);
        showAnimation = new Timeline();
        showAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1), _ -> showAnimationTransition.play()));
        showAnimation.play();
    }

    /**
     * Hide drawer with slide-out animation
     */
    /**
     * 
     */
    private void hideDrawerAnimated() {
        if (showAnimation != null) {
            showAnimation.stop();
        }
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), drawerContainer);
        slideOut.setFromX(0);
        slideOut.setToX(280);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), drawerContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition hideAnimationTransition = new ParallelTransition(slideOut, fadeOut);
        hideAnimationTransition.setOnFinished(_ -> {
            drawerContainer.setVisible(false);
            drawerContainer.setManaged(false);
            externalDrawerContainer.setMouseTransparent(true);
            drawerContainer.getStyleClass().removeAll("drawer-visible");
            drawerContainer.getStyleClass().add("drawer-hidden");
        });

        hideAnimation = new Timeline();
        hideAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1), _ -> hideAnimationTransition.play()));
        hideAnimation.play();
    }

    /**
     * 
     */
    private void setupExpandablePanes() {
        favoritesPane.setExpanded(true);
        followingPane.setExpanded(false);
        favoritesPane.expandedProperty().addListener((_, _, newVal) -> {
            if (newVal && followingPane.isExpanded()) {
                createExpandCollapseAnimation(followingPane, false).play();
            }
        });

        followingPane.expandedProperty().addListener((_, _, newVal) -> {
            if (newVal && favoritesPane.isExpanded()) {
                createExpandCollapseAnimation(favoritesPane, false).play();
            }
        });
    }

    /**
     * Create smooth expand/collapse animation for titled panes
     * 
     * @param pane The TitledPane to animate
     * @param expand True to expand, false to collapse
     */
    /**
     * @param pane
     * @param expand
     * @return
     */
    private Timeline createExpandCollapseAnimation(TitledPane pane, boolean expand) {
        Timeline animation = new Timeline();
        
        if (expand) {
            animation.getKeyFrames().add(new KeyFrame(Duration.millis(200), _ -> pane.setExpanded(true)));
        } else {
            animation.getKeyFrames().add(new KeyFrame(Duration.millis(200), _ -> pane.setExpanded(false)));
        }
        
        return animation;
    }

    /**
     * 
     */
    private void setupFavoritesObservableList() {
        userFavorites = favoritesService.getUserFavoritesList();
        userFavorites.addListener((ListChangeListener<FavoriteViewModel>) _ -> {
            Platform.runLater(() -> {
                populateFavoritesList(userFavorites);
                updateFavoritesCount(userFavorites.size());
            });
        });
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private void loadFavorites() {
        if (userFavorites.isEmpty()) {
            favoritesService.refreshUserFavorites();
        } else {
            populateFavoritesList(userFavorites);
            updateFavoritesCount(userFavorites.size());
        }
    }

    /**
     * 
     */
    private void loadFollowing() {
        Platform.runLater(() -> {
            populateFollowingList(List.of());
            updateFollowingCount(0);
        });
    }

    /**
     * @param favorites
     */
    private void populateFavoritesList(ObservableList<FavoriteViewModel> favorites) {
        favoritesContainer.getChildren().clear();

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label(localeService.getMessage("favorites.drawer.empty", "No favorites yet"));
            emptyLabel.getStyleClass().add("drawer-empty-label");
            favoritesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (FavoriteViewModel favorite : favorites) {
            if (favorite.getListing() != null) {
                VBox listingItem = createListingItem(favorite.getListing());
                favoritesContainer.getChildren().add(listingItem);
            }
        }
    }

    /**
     * @param following
     */
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

    /**
     * @param listing
     * @return
     */
    private VBox createListingItem(ListingViewModel listing) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");
        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");
        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(40);
        thumbnail.setFitHeight(40);
        thumbnail.setPreserveRatio(true);
        thumbnail.getStyleClass().add("drawer-thumbnail");
        loadThumbnailImage(thumbnail, listing);
        VBox textContainer = new VBox(2);
        Label title = new Label(listing.getTitle());
        title.setMaxWidth(170);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.getStyleClass().add("drawer-item-title");

        Text type = new Text(listing.getListingTypeValue());
        type.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(title, type);

        header.getChildren().addAll(thumbnail, textContainer);
        item.setOnMouseClicked(_ -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), item);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), item);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            SequentialTransition clickAnimation = new SequentialTransition(scaleDown, scaleUp);
            clickAnimation.setOnFinished(_ -> handleListingClick(listing));
            clickAnimation.play();
        });

        item.getChildren().add(header);
        return item;
    }

    /**
     * @param user
     * @return
     */
    private VBox createUserItem(UserViewModel user) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");
        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("drawer-avatar");
        loadAvatarImage(avatar, user);
        VBox textContainer = new VBox(2);
        Text displayName = new Text(user.getDisplayName());
        displayName.getStyleClass().add("drawer-item-title");

        Text username = new Text("@" + user.getUsername());
        username.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(displayName, username);

        header.getChildren().addAll(avatar, textContainer);
        item.setOnMouseClicked(_ -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), item);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), item);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            
            SequentialTransition clickAnimation = new SequentialTransition(scaleDown, scaleUp);
            clickAnimation.setOnFinished(_ -> handleUserClick(user));
            clickAnimation.play();
        });

        item.getChildren().add(header);
        return item;
    }

    /**
     * @param thumbnail
     * @param listing
     */
    private void loadThumbnailImage(ImageView thumbnail, ListingViewModel listing) {
        String imagePath = getFirstImagePath(listing);

        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
            ImageService.getInstance().fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                thumbnail.setImage(image);
                            } else {
                                setDefaultThumbnail(thumbnail);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to load thumbnail image: " + ex.getMessage());
                        Platform.runLater(() -> setDefaultThumbnail(thumbnail));
                        return null;
                    });
        } else {
            setDefaultThumbnail(thumbnail);
        }
    }

    /**
     * @param listing
     * @return
     */
    private String getFirstImagePath(ListingViewModel listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            for (ListingItemViewModel item : listing.getItems()) {
                String imagePath = item.getItem().getImagePath();
                if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
                    return imagePath;
                }
            }
        }
        return null;
    }

    /**
     * @param avatar
     * @param user
     */
    private void loadAvatarImage(ImageView avatar, UserViewModel user) {
        String imagePath = user.getProfileImagePath();
        
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageService.getInstance().fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                avatar.setImage(image);
                            } else {
                                setDefaultAvatar(avatar);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to load avatar image: " + ex.getMessage());
                        Platform.runLater(() -> setDefaultAvatar(avatar));
                        return null;
                    });
        } else {
            setDefaultAvatar(avatar);
        }
    }

    /**
     * @param imageView
     */
    private void setDefaultThumbnail(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default thumbnail: " + e.getMessage());
        }
    }

    /**
     * @param imageView
     */
    private void setDefaultAvatar(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/default_profile.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
        }
    }

    /**
     * @param listing
     */
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

    /**
     * @param user
     */
    private void handleUserClick(UserViewModel user) {
        try {
            Parent profileView = navigationService.loadProfileView(userSessionService.getUserViewModel());
            mainController.setContent(profileView);
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open user profile: " + e.getMessage());
        }
    }

    /**
     * @param count
     */
    private void updateFavoritesCount(int count) {
        String countText = localeService.getMessage("favorites.drawer.count", count);
        favoritesCountLabel.setText(countText);
    }

    /**
     * @param count
     */
    private void updateFollowingCount(int count) {
        String countText = localeService.getMessage("following.drawer.count", count);
        followingCountLabel.setText(countText);
    }

    /**
     * 
     */
    public void refreshData() {
        favoritesService.refreshUserFavorites();
        loadFollowing();
    }

    /**
     * @param visible
     */
    public void setDrawerVisibleInstantly(boolean visible) {
        drawerContainer.setVisible(visible);
        drawerContainer.setManaged(visible);
        externalDrawerContainer.setMouseTransparent(!visible);
        if (visible) {
            drawerContainer.getStyleClass().removeAll("drawer-hidden");
            drawerContainer.getStyleClass().add("drawer-visible");
            drawerContainer.setTranslateX(0);
            drawerContainer.setOpacity(1.0);
        } else {
            drawerContainer.getStyleClass().removeAll("drawer-visible");
            drawerContainer.getStyleClass().add("drawer-hidden");
            drawerContainer.setTranslateX(280);
            drawerContainer.setOpacity(0.0);
        }
        drawerVisible.set(visible);
    }

    /**
     * @param mainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * @return
     */
    public boolean isDrawerVisible() {
        return drawerVisible.get();
    }

    /**
     * @return
     */
    public BooleanProperty drawerVisibleProperty() {
        return drawerVisible;
    }

    /**
     * 
     */
    public void showDrawer() {
        drawerVisible.set(true);
    }

    /**
     * 
     */
    public void hideDrawer() {
        drawerVisible.set(false);
    }
}