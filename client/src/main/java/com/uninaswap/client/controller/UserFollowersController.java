package com.uninaswap.client.controller;

import com.uninaswap.client.service.FollowerService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
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

import java.time.format.DateTimeFormatter;

public class UserFollowersController implements Refreshable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label followingCountLabel;

    @FXML
    private Label followersCountLabel;

    @FXML
    private TabPane followersTabPane;

    // Following Tab
    @FXML
    private Tab followingTab;

    @FXML
    private TableView<UserViewModel> followingTable;

    @FXML
    private TableColumn<UserViewModel, String> followingNameColumn;

    @FXML
    private TableColumn<UserViewModel, String> followingUsernameColumn;

    @FXML
    private TableColumn<UserViewModel, String> followingDateColumn;

    @FXML
    private TableColumn<UserViewModel, Void> followingActionsColumn;

    // Followers Tab
    @FXML
    private Tab followersTab;

    @FXML
    private TableView<UserViewModel> followersTable;

    @FXML
    private TableColumn<UserViewModel, String> followersNameColumn;

    @FXML
    private TableColumn<UserViewModel, String> followersUsernameColumn;

    @FXML
    private TableColumn<UserViewModel, String> followersDateColumn;

    @FXML
    private TableColumn<UserViewModel, Void> followersActionsColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    // Additional UI elements for localization
    @FXML
    private Label followingCountDescriptionLabel;

    @FXML
    private Label followersCountDescriptionLabel;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final FollowerService followerService = FollowerService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data
    private UserViewModel currentUser;
    private final ObservableList<UserViewModel> followingUsers = FXCollections.observableArrayList();
    private final ObservableList<UserViewModel> followerUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        setupLabels();
        setupTables();
        updateCounts();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("followers.debug.initialized", "UserFollowers controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update all text labels
        setupLabels();
        
        // Update tab headers
        updateTabHeaders();
        
        // Update table column headers
        updateTableHeaders();
        
        // Update action buttons in tables
        refreshActionButtons();
        
        // Update user info if user is set
        if (currentUser != null) {
            updateUserInfo();
        }
        
        // Update counts display
        updateCounts();
        
        // Update table placeholders
        updateTablePlaceholders();
        
        System.out.println(localeService.getMessage("followers.debug.ui.refreshed", "UserFollowers UI refreshed"));
    }

    private void setupLabels() {
        if (titleLabel != null) {
            titleLabel.setText(localeService.getMessage("followers.title", "Followers & Following"));
        }
        if (refreshButton != null) {
            refreshButton.setText(localeService.getMessage("followers.button.refresh", "Refresh"));
        }
        if (closeButton != null) {
            closeButton.setText(localeService.getMessage("followers.button.close", "Close"));
        }
        if (followingCountDescriptionLabel != null) {
            followingCountDescriptionLabel.setText(localeService.getMessage("followers.following.label", "Following"));
        }
        if (followersCountDescriptionLabel != null) {
            followersCountDescriptionLabel.setText(localeService.getMessage("followers.followers.label", "Followers"));
        }
    }

    private void updateTabHeaders() {
        if (followingTab != null) {
            followingTab.setText(localeService.getMessage("followers.tab.following", "Following"));
        }
        if (followersTab != null) {
            followersTab.setText(localeService.getMessage("followers.tab.followers", "Followers"));
        }
    }

    private void updateTableHeaders() {
        // Following table headers
        if (followingNameColumn != null) {
            followingNameColumn.setText(localeService.getMessage("followers.column.name", "Name"));
        }
        if (followingUsernameColumn != null) {
            followingUsernameColumn.setText(localeService.getMessage("followers.column.username", "Username"));
        }
        if (followingDateColumn != null) {
            followingDateColumn.setText(localeService.getMessage("followers.column.since", "Since"));
        }
        if (followingActionsColumn != null) {
            followingActionsColumn.setText(localeService.getMessage("followers.column.actions", "Actions"));
        }

        // Followers table headers
        if (followersNameColumn != null) {
            followersNameColumn.setText(localeService.getMessage("followers.column.name", "Name"));
        }
        if (followersUsernameColumn != null) {
            followersUsernameColumn.setText(localeService.getMessage("followers.column.username", "Username"));
        }
        if (followersDateColumn != null) {
            followersDateColumn.setText(localeService.getMessage("followers.column.since", "Since"));
        }
        if (followersActionsColumn != null) {
            followersActionsColumn.setText(localeService.getMessage("followers.column.actions", "Actions"));
        }
    }

    private void updateTablePlaceholders() {
        if (followingTable != null) {
            Label followingPlaceholder = new Label(localeService.getMessage("followers.following.empty", "Not following anyone yet"));
            followingPlaceholder.getStyleClass().add("placeholder-text");
            followingTable.setPlaceholder(followingPlaceholder);
        }
        
        if (followersTable != null) {
            Label followersPlaceholder = new Label(localeService.getMessage("followers.followers.empty", "No followers yet"));
            followersPlaceholder.getStyleClass().add("placeholder-text");
            followersTable.setPlaceholder(followersPlaceholder);
        }
    }

    private void setupTables() {
        // Following table setup
        followingNameColumn.setCellValueFactory(cellData -> {
            String displayName = cellData.getValue().getDisplayName();
            return new SimpleStringProperty(displayName != null ? displayName : 
                localeService.getMessage("followers.name.unknown", "Unknown"));
        });

        followingUsernameColumn.setCellValueFactory(cellData -> {
            String username = cellData.getValue().getUsername();
            return new SimpleStringProperty(username != null ? "@" + username : 
                localeService.getMessage("followers.username.unknown", "@unknown"));
        });

        followingDateColumn.setCellValueFactory(cellData -> {
            // TODO: Implement when join date is available in UserViewModel
            return new SimpleStringProperty(localeService.getMessage("followers.date.na", "N/A"));
        });

        // Followers table setup
        followersNameColumn.setCellValueFactory(cellData -> {
            String displayName = cellData.getValue().getDisplayName();
            return new SimpleStringProperty(displayName != null ? displayName : 
                localeService.getMessage("followers.name.unknown", "Unknown"));
        });

        followersUsernameColumn.setCellValueFactory(cellData -> {
            String username = cellData.getValue().getUsername();
            return new SimpleStringProperty(username != null ? "@" + username : 
                localeService.getMessage("followers.username.unknown", "@unknown"));
        });

        followersDateColumn.setCellValueFactory(cellData -> {
            // TODO: Implement when join date is available in UserViewModel
            return new SimpleStringProperty(localeService.getMessage("followers.date.na", "N/A"));
        });

        // Setup action columns
        setupFollowingActionsColumn();
        setupFollowersActionsColumn();

        // Bind tables to observable lists
        followingTable.setItems(followingUsers);
        followersTable.setItems(followerUsers);
        
        // Set initial placeholders
        updateTablePlaceholders();
    }

    private void setupFollowingActionsColumn() {
        followingActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button unfollowButton = new Button();
            private final Button viewProfileButton = new Button();
            private final HBox actionBox = new HBox(5, viewProfileButton, unfollowButton);

            {
                // Set initial button text
                updateButtonTexts();
                
                viewProfileButton.getStyleClass().add("secondary-button");
                unfollowButton.getStyleClass().add("danger-button");

                viewProfileButton.setOnAction(e -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleViewProfile(user);
                });

                unfollowButton.setOnAction(e -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleUnfollow(user);
                });
            }

            private void updateButtonTexts() {
                viewProfileButton.setText(localeService.getMessage("followers.button.view", "View"));
                unfollowButton.setText(localeService.getMessage("followers.button.unfollow", "Unfollow"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    updateButtonTexts(); // Update button texts when cell is updated
                }
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void setupFollowersActionsColumn() {
        followersActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button followButton = new Button();
            private final Button viewProfileButton = new Button();
            private final HBox actionBox = new HBox(5, viewProfileButton, followButton);

            {
                // Set initial button text
                updateButtonTexts();
                
                viewProfileButton.getStyleClass().add("secondary-button");

                viewProfileButton.setOnAction(e -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleViewProfile(user);
                });

                followButton.setOnAction(e -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    boolean isFollowing = followerService.isFollowingLocally(user.getId());
                    if (isFollowing) {
                        handleUnfollow(user);
                    } else {
                        handleFollow(user);
                    }
                });
            }

            private void updateButtonTexts() {
                viewProfileButton.setText(localeService.getMessage("followers.button.view", "View"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    // Update follow button text based on current follow status
                    boolean isFollowing = followerService.isFollowingLocally(user.getId());
                    followButton.setText(isFollowing ? 
                        localeService.getMessage("followers.button.unfollow", "Unfollow") : 
                        localeService.getMessage("followers.button.follow", "Follow"));
                    followButton.getStyleClass().clear();
                    followButton.getStyleClass().add(isFollowing ? "danger-button" : "primary-button");
                    
                    // Update view button text as well
                    updateButtonTexts();
                    setGraphic(actionBox);
                }
            }
        });
    }

    private void refreshActionButtons() {
        // Force refresh of action columns to update button texts
        if (followingActionsColumn != null) {
            followingActionsColumn.setVisible(false);
            followingActionsColumn.setVisible(true);
        }
        if (followersActionsColumn != null) {
            followersActionsColumn.setVisible(false);
            followersActionsColumn.setVisible(true);
        }
    }

    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                loadFollowData();
                System.out.println(localeService.getMessage("followers.debug.user.set", "User set for followers view: {0}")
                    .replace("{0}", user.getDisplayName() != null ? user.getDisplayName() : "unknown"));
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : 
                localeService.getMessage("followers.user.unknown", "Unknown User"));
            updateCounts();
        }
    }

    private void updateCounts() {
        if (followingCountLabel != null) {
            int followingCount = followingUsers.size();
            followingCountLabel.setText(String.valueOf(followingCount));
            System.out.println(localeService.getMessage("followers.debug.following.count", "Following count updated: {0}")
                .replace("{0}", String.valueOf(followingCount)));
        }
        
        if (followersCountLabel != null) {
            int followersCount = followerUsers.size();
            followersCountLabel.setText(String.valueOf(followersCount));
            System.out.println(localeService.getMessage("followers.debug.followers.count", "Followers count updated: {0}")
                .replace("{0}", String.valueOf(followersCount)));
        }
    }

    private void loadFollowData() {
        if (currentUser == null) {
            System.out.println(localeService.getMessage("followers.debug.no.user", "No user set for loading follow data"));
            return;
        }

        System.out.println(localeService.getMessage("followers.debug.loading.data", "Loading follow data for user"));

        // Load following users
        followerService.getFollowing()
                .thenAccept(following -> Platform.runLater(() -> {
                    followingUsers.clear();
                    following.forEach(userDTO -> {
                        // TODO: Convert DTOs to ViewModels when ViewModelMapper is available
                        // followingUsers.add(viewModelMapper.toViewModel(userDTO));
                    });
                    updateCounts();
                    System.out.println(localeService.getMessage("followers.debug.following.loaded", "Following list loaded: {0} users")
                        .replace("{0}", String.valueOf(following.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("followers.debug.following.error", "Error loading following: {0}")
                            .replace("{0}", ex.getMessage()));
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("followers.error.title", "Error"),
                                localeService.getMessage("followers.error.load.following", "Failed to load following"),
                                localeService.getMessage("followers.error.load.following.content", 
                                    "Could not load the list of users you are following: {0}")
                                    .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });

        // Load followers
        followerService.getFollowers()
                .thenAccept(followers -> Platform.runLater(() -> {
                    followerUsers.clear();
                    followers.forEach(userDTO -> {
                        // TODO: Convert DTOs to ViewModels when ViewModelMapper is available
                        // followerUsers.add(viewModelMapper.toViewModel(userDTO));
                    });
                    updateCounts();
                    System.out.println(localeService.getMessage("followers.debug.followers.loaded", "Followers list loaded: {0} users")
                        .replace("{0}", String.valueOf(followers.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("followers.debug.followers.error", "Error loading followers: {0}")
                            .replace("{0}", ex.getMessage()));
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("followers.error.title", "Error"),
                                localeService.getMessage("followers.error.load.followers", "Failed to load followers"),
                                localeService.getMessage("followers.error.load.followers.content", 
                                    "Could not load the list of your followers: {0}")
                                    .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    private void handleViewProfile(UserViewModel user) {
        try {
            navigationService.navigateToUserProfile(user.getId());
            System.out.println(localeService.getMessage("followers.debug.view.profile", "Opening profile for user: {0}")
                .replace("{0}", user.getDisplayName() != null ? user.getDisplayName() : "unknown"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("followers.debug.view.profile.error", "Error opening user profile: {0}")
                .replace("{0}", e.getMessage()));
            
            AlertHelper.showErrorAlert(
                localeService.getMessage("followers.error.title", "Error"),
                localeService.getMessage("followers.error.view.profile", "Failed to open profile"),
                localeService.getMessage("followers.error.view.profile.content", "Could not open user profile: {0}")
                    .replace("{0}", e.getMessage())
            );
        }
    }

    private void handleFollow(UserViewModel user) {
        String userName = user.getDisplayName() != null ? user.getDisplayName() : 
            localeService.getMessage("followers.user.unknown", "Unknown User");
        
        System.out.println(localeService.getMessage("followers.debug.following.user", "Following user: {0}")
            .replace("{0}", userName));

        followerService.followUser(user.getId())
                .thenAccept(followerViewModel -> Platform.runLater(() -> {
                    // Update UI to reflect new follow status
                    followersTable.refresh();
                    updateCounts();
                    
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("followers.follow.success.title", "Success"),
                            localeService.getMessage("followers.follow.success.header", "User Followed"),
                            localeService.getMessage("followers.follow.success.message",
                                    "You are now following {0}")
                                    .replace("{0}", userName));
                    
                    System.out.println(localeService.getMessage("followers.debug.follow.success", "Successfully followed user: {0}")
                        .replace("{0}", userName));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("followers.debug.follow.error", "Error following user: {0}")
                            .replace("{0}", ex.getMessage()));
                        
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("followers.error.title", "Error"),
                                localeService.getMessage("followers.error.follow", "Failed to follow user"),
                                localeService.getMessage("followers.error.follow.content", "Could not follow user: {0}")
                                    .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    private void handleUnfollow(UserViewModel user) {
        String userName = user.getDisplayName() != null ? user.getDisplayName() : 
            localeService.getMessage("followers.user.unknown", "Unknown User");
        
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("followers.unfollow.title", "Unfollow User"),
                localeService.getMessage("followers.unfollow.header", "Confirm Unfollow"),
                localeService.getMessage("followers.unfollow.message",
                        "Are you sure you want to unfollow {0}?")
                        .replace("{0}", userName));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println(localeService.getMessage("followers.debug.unfollowing.user", "Unfollowing user: {0}")
                    .replace("{0}", userName));
                
                followerService.unfollowUser(user.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            // Remove user from following list
                            followingUsers.removeIf(u -> u.getId().equals(user.getId()));
                            updateCounts();
                            
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("followers.unfollow.success.title", "Success"),
                                    localeService.getMessage("followers.unfollow.success.header", "User Unfollowed"),
                                    localeService.getMessage("followers.unfollow.success.message",
                                            "You are no longer following {0}")
                                            .replace("{0}", userName));
                            
                            System.out.println(localeService.getMessage("followers.debug.unfollow.success", "Successfully unfollowed user: {0}")
                                .replace("{0}", userName));
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                System.err.println(localeService.getMessage("followers.debug.unfollow.error", "Error unfollowing user: {0}")
                                    .replace("{0}", ex.getMessage()));
                                
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("followers.error.title", "Error"),
                                        localeService.getMessage("followers.error.unfollow", "Failed to unfollow user"),
                                        localeService.getMessage("followers.error.unfollow.content", "Could not unfollow user: {0}")
                                            .replace("{0}", ex.getMessage()));
                            });
                            return null;
                        });
            }
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println(localeService.getMessage("followers.debug.refresh.requested", "Refresh requested"));
        
        updateCounts();
        loadFollowData();
        
        // Show brief feedback to user
        if (refreshButton != null) {
            String originalText = refreshButton.getText();
            refreshButton.setText(localeService.getMessage("followers.button.refreshing", "Refreshing..."));
            refreshButton.setDisable(true);
            
            // Reset button after a short delay
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            refreshButton.setText(originalText);
                            refreshButton.setDisable(false);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }

    @FXML
    private void handleClose() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
                System.out.println(localeService.getMessage("followers.debug.closed", "UserFollowers dialog closed"));
            } else {
                System.err.println(localeService.getMessage("followers.debug.close.error", "Cannot close dialog: no stage found"));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("followers.debug.close.exception", "Error closing dialog: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    
    /**
     * Check if user has followers or following
     */
    public boolean hasFollowData() {
        return !followingUsers.isEmpty() || !followerUsers.isEmpty();
    }

    /**
     * Get following count
     */
    public int getFollowingCount() {
        return followingUsers.size();
    }

    /**
     * Get followers count
     */
    public int getFollowersCount() {
        return followerUsers.size();
    }

    /**
     * Refresh data (can be called externally)
     */
    public void refreshData() {
        loadFollowData();
        System.out.println(localeService.getMessage("followers.debug.external.refresh", "External refresh triggered"));
    }

    /**
     * Get the current user
     */
    public UserViewModel getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear all data
     */
    public void clearData() {
        currentUser = null;
        followingUsers.clear();
        followerUsers.clear();
        if (userNameLabel != null) {
            userNameLabel.setText("");
        }
        updateCounts();
        System.out.println(localeService.getMessage("followers.debug.data.cleared", "UserFollowers data cleared"));
    }

    /**
     * Check if user is set
     */
    public boolean hasUser() {
        return currentUser != null;
    }

    /**
     * Get user display name
     */
    public String getUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : 
            localeService.getMessage("followers.no.user", "No User");
    }

    /**
     * Force refresh of the tables display
     */
    public void refreshTables() {
        if (followingTable != null) {
            followingTable.refresh();
        }
        if (followersTable != null) {
            followersTable.refresh();
        }
        updateTablePlaceholders();
        System.out.println(localeService.getMessage("followers.debug.tables.refreshed", "Tables display refreshed"));
    }

    /**
     * Check if user is following another user
     */
    public boolean isFollowing(String userId) {
        return followingUsers.stream().anyMatch(user -> user.getId().equals(userId));
    }

    /**
     * Check if user is followed by another user
     */
    public boolean isFollowedBy(String userId) {
        return followerUsers.stream().anyMatch(user -> user.getId().equals(userId));
    }
}