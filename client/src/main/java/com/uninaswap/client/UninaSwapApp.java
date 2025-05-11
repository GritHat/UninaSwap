package com.uninaswap.client;

import com.uninaswap.client.service.LocaleService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UninaSwapApp extends Application {
    
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