package com.finanzapp.controller;

import com.finanzapp.dao.NotificacionDAO;
import com.finanzapp.model.Notificacion;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador del panel de notificaciones de Fox Wallet.
 * <p>
 * Muestra los avisos automáticos generados por la app (superación de presupuesto,
 * objetivos alcanzados, etc.) con indicador visual de leído/no leído.
 * Al abrir el panel, todas las notificaciones se marcan como leídas.
 */
public class NotificacionesController implements Initializable {

    @FXML private VBox notifList;

    private final NotificacionDAO dao = new NotificacionDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) { cargarNotificacionesDelUsuario(); }

    /** Carga y renderiza las notificaciones del usuario en el panel. */
    private void cargarNotificacionesDelUsuario() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        List<Notificacion> notifs = dao.obtenerNotificacionesDeUsuario(uid);
        notifList.getChildren().clear();

        if (notifs.isEmpty()) {
            Label empty = new Label("No tienes notificaciones");
            empty.setStyle("-fx-text-fill:-color-text3;-fx-padding:24px;-fx-font-size:13px;");
            notifList.getChildren().add(empty);
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM, HH:mm", new Locale("es"));

        for (Notificacion n : notifs) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14, 16, 14, 16));
            String borderStyle = "-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;";
            String bg = n.isLeida() ? "transparent" : "-color-surface2";
            row.setStyle("-fx-background-color:" + bg + ";" + borderStyle);

            // Dot indicator
            Label dot = new Label("●");
            dot.setStyle("-fx-font-size:8px;-fx-text-fill:" + tipoColor(n.getTipo()) + ";" +
                (n.isLeida() ? "-fx-opacity:0.3;" : ""));

            VBox body = new VBox(4);
            HBox.setHgrow(body, Priority.ALWAYS);

            Label titulo = new Label(n.getTitulo());
            titulo.setStyle("-fx-font-weight:" + (n.isLeida() ? "400" : "600") + ";-fx-font-size:13px;-fx-text-fill:-color-text;");

            if (n.getMensaje() != null && !n.getMensaje().isBlank()) {
                Label msg = new Label(n.getMensaje());
                msg.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
                msg.setWrapText(true);
                body.getChildren().addAll(titulo, msg);
            } else {
                body.getChildren().add(titulo);
            }

            String fechaStr = n.getCreatedAt() != null ? n.getCreatedAt().format(dtf) : "";
            Label fecha = new Label(fechaStr);
            fecha.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");

            Label badge = new Label(n.getTipo());
            badge.setStyle("-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:6;-fx-background-color:" +
                tipoBg(n.getTipo()) + ";-fx-text-fill:" + tipoColor(n.getTipo()) + ";");

            row.getChildren().addAll(dot, body, fecha, badge);
            notifList.getChildren().add(row);
        }
    }

    @FXML void marcarLeidas() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        dao.marcarTodasLasNotificacionesComoLeidas(uid);
        cargarNotificacionesDelUsuario();
    }

    private String tipoColor(String tipo) {
        return switch (tipo != null ? tipo : "info") {
            case "success" -> "#1D9E75";
            case "warning" -> "#BA7517";
            case "danger"  -> "#D85A30";
            default        -> "#185FA5";
        };
    }

    private String tipoBg(String tipo) {
        return switch (tipo != null ? tipo : "info") {
            case "success" -> "#E1F5EE";
            case "warning" -> "#FAEEDA";
            case "danger"  -> "#FAECE7";
            default        -> "#E6F1FB";
        };
    }
}
