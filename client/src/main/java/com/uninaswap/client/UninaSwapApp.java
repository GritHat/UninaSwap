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
        // Change path to avoid spaces in directory names - use underscore instead of space
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/new/fxml_new/LoginView.fxml"));
        
        // Add debugging to see if we can load the file
        System.out.println("Trying to load: " + getClass().getResource("/new/fxml_new/LoginView.fxml"));
        
        loader.setResources(LocaleService.getInstance().getResourceBundle());
        
        try {
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 500);
            
            primaryStage.setTitle("UninaSwap - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}