package com.finanzapp.renderer;

import com.finanzapp.model.Notificacion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NotificacionRenderer {

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d MMM, HH:mm", new Locale("es"));

    public HBox crearFila(Notificacion n) {
        HBox fila = crearContenedor(n);

        Label dot      = crearDot(n);
        VBox  cuerpo   = crearCuerpo(n);
        Label fecha    = crearEtiquetaFecha(n);
        Label badge    = crearBadge(n);

        fila.getChildren().addAll(dot, cuerpo, fecha, badge);
        return fila;
    }

    public Label crearMensajeVacio() {
        Label etiqueta = new Label("No tienes notificaciones");
        etiqueta.setStyle("-fx-text-fill:-color-text3;-fx-padding:24px;-fx-font-size:13px;");
        return etiqueta;
    }


    private HBox crearContenedor(Notificacion n) {
        HBox fila = new HBox(14);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(14, 16, 14, 16));
        String borde = "-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;";
        fila.setStyle("-fx-background-color:transparent;" + borde);
        return fila;
    }

    private Label crearDot(Notificacion n) {
        String colorTipo = tipoColor(n.getTipo());
        Label dot = new Label("●");
        dot.setStyle("-fx-font-size:8px;-fx-text-fill:" + colorTipo + ";");
        return dot;
    }

    private VBox crearCuerpo(Notificacion n) {
        VBox cuerpo = new VBox(4);
        HBox.setHgrow(cuerpo, Priority.ALWAYS);

        Label titulo = new Label(n.getTitulo());
        titulo.setStyle("-fx-font-weight:400;-fx-font-size:13px;-fx-text-fill:-color-text;");

        boolean mensajeNoNulo = n.getMensaje() != null;
        boolean mensajeNoVacio = mensajeNoNulo && !n.getMensaje().isBlank();
        boolean tieneMensaje = mensajeNoNulo && mensajeNoVacio;
        if (tieneMensaje) {
            Label msg = new Label(n.getMensaje());
            msg.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
            msg.setWrapText(true);
            cuerpo.getChildren().addAll(titulo, msg);
        } else {
            cuerpo.getChildren().add(titulo);
        }

        return cuerpo;
    }

    private Label crearEtiquetaFecha(Notificacion n) {
        String texto = "";
        if (n.getCreatedAt() != null) {
            texto = n.getCreatedAt().format(formatoFecha);
        }
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        return etiqueta;
    }

    private Label crearBadge(Notificacion n) {
        String tipoBadge = n.getTipo();
        String colorFondoBadge = tipoBg(tipoBadge);
        String colorTextoBadge = tipoColor(tipoBadge);
        String estiloBadge = "-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:6;" +
            "-fx-background-color:" + colorFondoBadge + ";" +
            "-fx-text-fill:" + colorTextoBadge + ";";
        Label badge = new Label(tipoBadge);
        badge.setStyle(estiloBadge);
        return badge;
    }

    private String tipoColor(String tipo) {
        String tipoSeguro;
        if (tipo != null) {
            tipoSeguro = tipo;
        } else {
            tipoSeguro = "info";
        }

        if ("success".equals(tipoSeguro)) {
            return "#1D9E75";
        } else if ("warning".equals(tipoSeguro)) {
            return "#BA7517";
        } else if ("danger".equals(tipoSeguro)) {
            return "#D85A30";
        } else {
            return "#185FA5";
        }
    }

    private String tipoBg(String tipo) {
        String tipoSeguro;
        if (tipo != null) {
            tipoSeguro = tipo;
        } else {
            tipoSeguro = "info";
        }

        if ("success".equals(tipoSeguro)) {
            return "#E1F5EE";
        } else if ("warning".equals(tipoSeguro)) {
            return "#FAEEDA";
        } else if ("danger".equals(tipoSeguro)) {
            return "#FAECE7";
        } else {
            return "#E6F1FB";
        }
    }
}
