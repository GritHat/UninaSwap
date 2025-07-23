package com.uninaswap.client.controller;

import com.uninaswap.client.service.FollowerService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * 
 */
public class UserFollowersController {
    /**
     * 
     */
    @FXML
    private Label titleLabel;
    /**
     * 
     */
    @FXML
    private Label userNameLabel;
    /**
     * 
     */
    @FXML
    private Label followingCountLabel;
    /**
     * 
     */
    @FXML
    private Label followersCountLabel;
    /**
     * 
     */
    @FXML
    private TabPane followersTabPane;
    /**
     * 
     */
    @FXML
    private TableView<UserViewModel> followingTable;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followingNameColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followingUsernameColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followingDateColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, Void> followingActionsColumn;
    /**
     * 
     */
    @FXML
    private TableView<UserViewModel> followersTable;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followersNameColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followersUsernameColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, String> followersDateColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<UserViewModel, Void> followersActionsColumn;
    /**
     * 
     */
    @FXML
    private Button refreshButton;
    /**
     * 
     */
    @FXML
    private Button closeButton;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final FollowerService followerService = FollowerService.getInstance();
    /**
     * 
     */
    private UserViewModel currentUser;
    /**
     * 
     */
    private final ObservableList<UserViewModel> followingUsers = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<UserViewModel> followerUsers = FXCollections.observableArrayList();

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupLabels();
        setupTables();
        updateCounts();
    }

    /**
     * 
     */
    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("followers.title", "Followers & Following"));
        refreshButton.setText(localeService.getMessage("followers.refresh", "Refresh"));
        closeButton.setText(localeService.getMessage("followers.close", "Close"));
    }

    /**
     * 
     */
    private void setupTables() {
        followingNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayName()));

        followingUsernameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));

        followingDateColumn.setCellValueFactory(_ -> new SimpleStringProperty("N/A")); 
                                                                                              

        followersNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayName()));

        followersUsernameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));

        followersDateColumn.setCellValueFactory(_ -> new SimpleStringProperty("N/A")); 
                                                                                              

        setupFollowingActionsColumn();
        setupFollowersActionsColumn();
        followingTable.setItems(followingUsers);
        followersTable.setItems(followerUsers);
    }

    /**
     * 
     */
    private void setupFollowingActionsColumn() {
        followingActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button unfollowButton = new Button("Unfollow");
            private final Button viewProfileButton = new Button("View");
            private final HBox actionBox = new HBox(5, viewProfileButton, unfollowButton);

            {
                viewProfileButton.getStyleClass().add("secondary-button");
                unfollowButton.getStyleClass().add("danger-button");

                viewProfileButton.setOnAction(_ -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleViewProfile(user);
                });

                unfollowButton.setOnAction(_ -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleUnfollow(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    /**
     * 
     */
    private void setupFollowersActionsColumn() {
        followersActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button followButton = new Button("Follow");
            private final Button viewProfileButton = new Button("View");
            private final HBox actionBox = new HBox(5, viewProfileButton, followButton);

            {
                viewProfileButton.getStyleClass().add("secondary-button");
                followButton.getStyleClass().add("primary-button");

                viewProfileButton.setOnAction(_ -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleViewProfile(user);
                });

                followButton.setOnAction(_ -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleFollow(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    boolean isFollowing = followerService.isFollowingLocally(user.getId());
                    followButton.setText(isFollowing ? "Unfollow" : "Follow");
                    followButton.getStyleClass().clear();
                    followButton.getStyleClass().add(isFollowing ? "danger-button" : "primary-button");
                    setGraphic(actionBox);
                }
            }
        });
    }

    /**
     * @param user
     */
    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                loadFollowData();
            });
        }
    }

    /**
     * 
     */
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getDisplayName());
            updateCounts();
        }
    }

    /**
     * 
     */
    private void updateCounts() {
        followingCountLabel.setText(String.valueOf(followerService.getFollowingCount()));
        followersCountLabel.setText(String.valueOf(followerService.getFollowerCount()));
    }

    /**
     * 
     */
    private void loadFollowData() {
        if (currentUser == null)
            return;
        followerService.getFollowing()
                .thenAccept(following -> Platform.runLater(() -> {
                    followingUsers.clear();
                    following.forEach(userDTO -> {
                    
                    });
                    updateCounts();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("followers.error.title", "Error"),
                            localeService.getMessage("followers.error.load.following", "Failed to load following"),
                            ex.getMessage()));
                    return null;
                });

        followerService.getFollowers()
                .thenAccept(followers -> Platform.runLater(() -> {
                    followerUsers.clear();
                    followers.forEach(userDTO -> {
                       
                    });
                    updateCounts();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("followers.error.title", "Error"),
                            localeService.getMessage("followers.error.load.followers", "Failed to load followers"),
                            ex.getMessage()));
                    return null;
                });
    }

    /**
     * @param user
     */
    private void handleViewProfile(UserViewModel user) {
        System.out.println("View profile for user: " + user.getUsername());
    }

    /**
     * @param user
     */
    private void handleFollow(UserViewModel user) {
        followerService.followUser(user.getId())
                .thenAccept(_ -> Platform.runLater(() -> {
                    followersTable.refresh();
                    updateCounts();
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("followers.follow.success.title", "Success"),
                            localeService.getMessage("followers.follow.success.header", "User Followed"),
                            localeService.getMessage("followers.follow.success.message",
                                    "You are now following " + user.getDisplayName()));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("followers.error.title", "Error"),
                            localeService.getMessage("followers.error.follow", "Failed to follow user"),
                            ex.getMessage()));
                    return null;
                });
    }

    /**
     * @param user
     */
    private void handleUnfollow(UserViewModel user) {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("followers.unfollow.title", "Unfollow User"),
                localeService.getMessage("followers.unfollow.header", "Confirm Unfollow"),
                localeService.getMessage("followers.unfollow.message",
                        "Are you sure you want to unfollow " + user.getDisplayName() + "?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                followerService.unfollowUser(user.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            followingUsers.removeIf(u -> u.getId().equals(user.getId()));
                            updateCounts();
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("followers.unfollow.success.title", "Success"),
                                    localeService.getMessage("followers.unfollow.success.header", "User Unfollowed"),
                                    localeService.getMessage("followers.unfollow.success.message",
                                            "You are no longer following " + user.getDisplayName()));
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertHelper.showErrorAlert(
                                    localeService.getMessage("followers.error.title", "Error"),
                                    localeService.getMessage("followers.error.unfollow", "Failed to unfollow user"),
                                    ex.getMessage()));
                            return null;
                        });
            }
        });
    }

    /**
     * 
     */
    @FXML
    private void handleRefresh() {
        updateCounts();
        loadFollowData();
    }

    /**
     * 
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}