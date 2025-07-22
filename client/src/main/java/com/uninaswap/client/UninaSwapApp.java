package com.uninaswap.client;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 
 */
public class UninaSwapApp extends Application {

    /**
     * 
     */
    private final EventBusService eventBus = EventBusService.getInstance();

    /**
     *
     */
    @Override
    public void init() {
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            System.out.println("Global logout cleanup performed");
        });
    }

    /**
     *
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        loader.setResources(LocaleService.getInstance().getResourceBundle());
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 640);

        primaryStage.setTitle("UninaSwap - Login");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}