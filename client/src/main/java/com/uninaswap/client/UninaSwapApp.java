package com.uninaswap.client;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UninaSwapApp extends Application {

    private final EventBusService eventBus = EventBusService.getInstance();

    @Override
    public void init() {
        // Subscribe to logout events for global cleanup
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            // Clear any global listeners that should be reset on logout
            System.out.println("Global logout cleanup performed");
        });
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        loader.setResources(LocaleService.getInstance().getResourceBundle());
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 400, 300);
        
        primaryStage.setTitle("UninaSwap - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}