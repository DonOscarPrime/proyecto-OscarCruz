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
        stage.setTitle("Fox Wallet");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        irA("login");
        stage.show();
    }

    /** Cambia la vista cargando un FXML. */
    public static void irA(String fxmlName) {
        try {
            String prefijoCarpetaFxml = "/com/finanzapp/fxml/";
            String sufijoCarpetaFxml = ".fxml";
            String rutaFxml = prefijoCarpetaFxml + fxmlName + sufijoCarpetaFxml;
            FXMLLoader cargar = new FXMLLoader(MainApp.class.getResource(rutaFxml));

            javafx.scene.Parent raiz = cargar.load();
            Scene scene = new Scene(raiz);

            String rutaCss = "/com/finanzapp/css/styles.css";
            java.net.URL recursosCss = MainApp.class.getResource(rutaCss);
            String urlCss = recursosCss.toExternalForm();
            scene.getStylesheets().add(urlCss);

            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Mantiene compatibilidad con llamadas previas. */
    public static void navigateTo(String fxmlName) {
        irA(fxmlName);
    }

    @Override
    public void stop() {
        DatabaseConnection.cerrarConexion();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
