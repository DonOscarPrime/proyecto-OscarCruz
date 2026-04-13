package com.finanzapp;

import com.finanzapp.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle("FinanzApp");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        navigateTo("login");
        stage.show();
    }

    /** Cambia la vista cargando un FXML. */
    public static void navigateTo(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/finanzapp/fxml/" + fxmlName + ".fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                MainApp.class.getResource("/com/finanzapp/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
