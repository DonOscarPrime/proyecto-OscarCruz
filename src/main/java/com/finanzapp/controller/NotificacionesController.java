package com.finanzapp.controller;

import com.finanzapp.dao.NotificacionDAO;
import com.finanzapp.model.Notificacion;
import com.finanzapp.renderer.NotificacionRenderer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class NotificacionesController implements Initializable {

    @FXML private VBox notifList;

    private final NotificacionDAO      dao    = new NotificacionDAO();
    private final NotificacionRenderer cargar = new NotificacionRenderer();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        List<Notificacion> notifs = dao.obtenerNotificacionesActivas();

        notifList.getChildren().clear();

        if (notifs.isEmpty()) {
            javafx.scene.Node mensajeVacio = cargar.crearMensajeVacio();
            notifList.getChildren().add(mensajeVacio);
            return;
        }

        for (Notificacion n : notifs) {
            javafx.scene.Node filaNotificacion = cargar.crearFila(n);
            notifList.getChildren().add(filaNotificacion);
        }
    }
}
