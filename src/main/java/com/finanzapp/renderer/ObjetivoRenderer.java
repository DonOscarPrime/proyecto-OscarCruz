package com.finanzapp.renderer;

import com.finanzapp.model.Objetivo;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ObjetivoRenderer {

    public VBox crearTarjeta(Objetivo objetivo, Consumer<Double> alAportar, Runnable alEliminar) {
        VBox tarjeta = new VBox(10);
        tarjeta.setPrefWidth(280);
        tarjeta.setPadding(new Insets(16));
        tarjeta.setStyle(resolverEstiloTarjeta(objetivo));

        HBox cabecera      = crearCabecera(objetivo);
        ProgressBar barra  = crearBarra(objetivo);
        Label pctLabel     = crearEtiquetaPorcentaje(objetivo);
        Label restante     = crearEtiquetaRestante(objetivo);
        HBox filaAporte    = crearFilaAporte(objetivo, alAportar, alEliminar);

        tarjeta.getChildren().addAll(cabecera, barra, pctLabel, restante, filaAporte);

        if (objetivo.getFechaLimite() != null) {
            Label limite = new Label("Fecha límite: " + objetivo.getFechaLimite());
            limite.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
            tarjeta.getChildren().add(limite);
        }

        return tarjeta;
    }

    private HBox crearCabecera(Objetivo objetivo) {
        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        String textoEmoji = objetivo.getEmoji();
        if (textoEmoji == null) {
            textoEmoji = "🎯";
        }
        Label emoji = new Label(textoEmoji);
        emoji.setStyle("-fx-font-size:24px;");

        Label nombre = new Label(objetivo.getNombre());
        nombre.setStyle("-fx-font-size:14px;-fx-font-weight:600;-fx-text-fill:-color-text;");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        if (objetivo.iscompletado()) {
            Label badge = new Label("✓ Completado");
            badge.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;" +
                "-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:10px;");
            cabecera.getChildren().addAll(emoji, nombre, badge);
        } else {
            cabecera.getChildren().addAll(emoji, nombre);
        }

        return cabecera;
    }

    private ProgressBar crearBarra(Objetivo objetivo) {
        double porcentaje = 0;
        if (objetivo.getObjetivo() > 0) {
            double cociente = objetivo.getActual() / objetivo.getObjetivo();
            porcentaje = cociente * 100.0;
        }
        if (porcentaje > 100) {
            porcentaje = 100;
        }
        double progresoParaBarra = porcentaje / 100.0;
        ProgressBar barra = new ProgressBar(progresoParaBarra);
        barra.setMaxWidth(Double.MAX_VALUE);

        String colorBarra;
        if (objetivo.iscompletado()) {
            colorBarra = "#1D9E75";
        } else {
            colorBarra = "#185FA5";
        }
        barra.setStyle("-fx-accent:" + colorBarra + ";-fx-pref-height:10px;");
        return barra;
    }

    private Label crearEtiquetaPorcentaje(Objetivo objetivo) {
        double pct = 0;
        if (objetivo.getObjetivo() > 0) {
            double cociente = objetivo.getActual() / objetivo.getObjetivo();
            pct = cociente * 100.0;
        }
        if (pct > 100) {
            pct = 100;
        }
        double importeActual = objetivo.getActual();
        double importeObjetivo = objetivo.getObjetivo();
        String formatoPorcentaje = "%.0f%% · %.0f€ de %.0f€";
        String textoPorcentaje = String.format(formatoPorcentaje, pct, importeActual, importeObjetivo);

        Label etiqueta = new Label(textoPorcentaje);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text2;");
        return etiqueta;
    }

    private Label crearEtiquetaRestante(Objetivo objetivo) {
        double importeRestante = objetivo.getImporteRestante();
        String importeRestanteFormateado = Formateador.moneda(importeRestante);
        String textoRestante = "Faltan: " + importeRestanteFormateado + "€";
        Label etiqueta = new Label(textoRestante);
        etiqueta.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text3;");
        return etiqueta;
    }

    private HBox crearFilaAporte(Objetivo objetivo, Consumer<Double> alAportar, Runnable alEliminar) {
        HBox fila = new HBox(6);
        fila.setAlignment(Pos.CENTER_LEFT);

        TextField campoAporte = new TextField();
        campoAporte.setPromptText("Añadir €");
        campoAporte.setPrefWidth(90);
        campoAporte.setStyle("-fx-background-radius:6;-fx-border-radius:6;" +
            "-fx-border-color:-color-border2;-fx-padding:4 8;-fx-font-size:12px;");

        Button botonAportar = new Button("Aportar");
        botonAportar.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;" +
            "-fx-background-radius:6;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
        botonAportar.setOnAction(e -> {
            String textoRaw = campoAporte.getText();
            String textoSinEspacios = textoRaw.trim();
            String texto = textoSinEspacios.replace(",", ".");
            if (texto.isEmpty()) {
                resaltarError(campoAporte);
                return;
            }
            double importe;
            try {
                importe = Double.parseDouble(texto);
            } catch (NumberFormatException ex) {
                resaltarError(campoAporte);
                return;
            }
            if (importe == 0) {
                resaltarError(campoAporte);
                return;
            }
            alAportar.accept(importe);
        });

        Button botonEliminar = new Button("🗑");
        botonEliminar.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;" +
            "-fx-cursor:hand;-fx-font-size:12px;");
        botonEliminar.setOnAction(e -> alEliminar.run());

        fila.getChildren().addAll(campoAporte, botonAportar, botonEliminar);
        return fila;
    }

    private String resolverEstiloTarjeta(Objetivo objetivo) {
        Session sesionActual = Session.getInstance();
        boolean modoOscuro = sesionActual.isDarkMode();
        String colorFondo;

        if (objetivo.iscompletado()) {
            if (modoOscuro) {
                colorFondo = "#1A3D30";
            } else {
                colorFondo = "#E1F5EE";
            }
        } else {
            colorFondo = "-color-surface";
        }

        String estiloFondo = "-fx-background-color:" + colorFondo + ";";
        String estiloRedondeo = "-fx-background-radius:14;-fx-border-radius:14;";
        String estiloBorde = "-fx-border-color:-color-border;-fx-border-width:1;";
        String estiloTarjetaCompleto = estiloFondo + estiloRedondeo + estiloBorde;
        return estiloTarjetaCompleto;
    }

    private void resaltarError(TextField campo) {
        String estiloOriginal = campo.getStyle();
        String estiloError = "-fx-background-radius:6;-fx-border-radius:6;" +
            "-fx-border-color:#D85A30;-fx-padding:4 8;-fx-font-size:12px;";
        campo.setStyle(estiloError);
        String mensajeError = "La cantidad debe ser distinta de 0.";
        javafx.scene.control.Tooltip mensajeTooltip = new javafx.scene.control.Tooltip(mensajeError);
        campo.setTooltip(mensajeTooltip);
        double segundosPausa = 2;
        javafx.util.Duration duracion = javafx.util.Duration.seconds(segundosPausa);
        javafx.animation.PauseTransition pausa = new javafx.animation.PauseTransition(duracion);
        pausa.setOnFinished(ev -> campo.setStyle(estiloOriginal));
        pausa.play();
    }
}
