package com.finanzapp.renderer;

import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Objetivo;
import com.finanzapp.util.Formateador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Construye las filas y tarjetas del panel principal
 * de Fox Wallet: transacciones recientes y tarjetas de objetivos.
 */
public class DashboardRenderer {

    private final DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("d MMM", new Locale("es"));

    public HBox crearFilaTransaccion(Movimiento m) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 12, 10, 12));
        fila.setStyle("-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;");

        Label icono       = crearIconoMovimiento(m);
        VBox  infoBox     = crearInfoBoxMovimiento(m);
        Label fechaLabel  = crearEtiquetaFecha(m);
        Label cantLabel   = crearEtiquetaCantidad(m);

        fila.getChildren().addAll(icono, infoBox, fechaLabel, cantLabel);
        return fila;
    }

    public VBox crearTarjetaObjetivo(Objetivo objetivo) {
        VBox tarjeta = new VBox(8);
        tarjeta.setStyle("-fx-background-color:-color-surface;-fx-background-radius:14;-fx-border-radius:14;" +
            "-fx-border-color:-color-border;-fx-border-width:1;-fx-padding:14;");
        HBox.setHgrow(tarjeta, Priority.ALWAYS);

        String emojiRaw = objetivo.getEmoji();
        String emojiObjetivo;
        if (emojiRaw != null) {
            emojiObjetivo = emojiRaw;
        } else {
            emojiObjetivo = "🎯";
        }

        Label emojiLabel = new Label(emojiObjetivo);
        emojiLabel.setStyle("-fx-font-size:22px;");

        Label nombreLabel = new Label(objetivo.getNombre());
        nombreLabel.setStyle("-fx-font-weight:500;-fx-font-size:13px;-fx-text-fill:-color-text;");

        double porcentajeProgreso = objetivo.getProgreso();
        double progresoDecimal = porcentajeProgreso / 100.0;
        ProgressBar barra = new ProgressBar(progresoDecimal);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setStyle("-fx-accent:#1D9E75;-fx-pref-height:8px;");
        double importeActual = objetivo.getActual();
        double importeObjetivo = objetivo.getObjetivo();
        String importeActualFormateado = Formateador.moneda(importeActual);
        String importeObjetivoFormateado = Formateador.moneda(importeObjetivo);
        String formatoPorcentajeDash = "%.0f%% · %s€ de %s€";
        String textoPorcentaje = String.format(formatoPorcentajeDash,
            porcentajeProgreso,
            importeActualFormateado,
            importeObjetivoFormateado);
        Label pctLabel = new Label(textoPorcentaje);
        pctLabel.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");

        tarjeta.getChildren().addAll(emojiLabel, nombreLabel, barra, pctLabel);
        return tarjeta;
    }

    public HBox crearFilaLeyendaDonut(String nombreCategoria, double valor, String colorHex) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label punto = new Label("●");
        String estiloPunto = "-fx-text-fill:" + colorHex + ";-fx-font-size:10px;";
        punto.setStyle(estiloPunto);

        Label nombre = new Label(nombreCategoria);
        nombre.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text;");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        String valorFormateado = Formateador.moneda(valor);
        String textoValor = valorFormateado + "€";
        Label valorLabel = new Label(textoValor);
        valorLabel.setStyle("-fx-font-size:12px;-fx-font-family:monospace;-fx-text-fill:-color-text;");

        fila.getChildren().addAll(punto, nombre, valorLabel);
        return fila;
    }

    public Label crearMensajeVacioTransacciones() {
        Label etiqueta = new Label("Sin movimientos este mes");
        etiqueta.setStyle("-fx-text-fill:-color-text3;-fx-padding:20px;");
        return etiqueta;
    }

    private Label crearIconoMovimiento(Movimiento m) {
        String emojiRaw = m.getCategoriaEmoji();
        String emoji;
        if (emojiRaw != null) {
            emoji = emojiRaw;
        } else {
            emoji = "💳";
        }

        Label icono = new Label(emoji);
        String fondo;
        if (m.isIngreso()) {
            fondo = "-fx-background-color:rgba(29,158,117,0.15);";
        } else {
            fondo = "-fx-background-color:-color-surface2;";
        }
        String estiloBaseIcono = "-fx-font-size:18px;-fx-min-width:36px;-fx-min-height:36px;" +
            "-fx-background-radius:10;-fx-alignment:center;";
        String estiloIcono = estiloBaseIcono + fondo;
        icono.setStyle(estiloIcono);
        return icono;
    }

    private VBox crearInfoBoxMovimiento(Movimiento m) {
        VBox infoBox = new VBox(2);
        Label nombre = new Label(m.getNombre());
        nombre.setStyle("-fx-font-weight:500;-fx-font-size:13px;-fx-text-fill:-color-text;");
        Label categoria = new Label(m.getCategoriaDisplay());
        categoria.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        infoBox.getChildren().addAll(nombre, categoria);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        return infoBox;
    }

    private Label crearEtiquetaFecha(Movimiento m) {
        String texto = "";
        if (m.getFecha() != null) {
            texto = m.getFecha().format(formatoCorto);
        }
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        return etiqueta;
    }

    private Label crearEtiquetaCantidad(Movimiento m) {
        String prefijo;
        String color;
        if (m.isIngreso()) {
            prefijo = "+";
            color   = "#1D9E75";
        } else {
            prefijo = "-";
            color   = "#D85A30";
        }
        double cantidadMovimiento = m.getCantidad();
        String cantidadFormateada = Formateador.moneda(cantidadMovimiento);
        String textoCantidad = prefijo + cantidadFormateada + "€";
        Label etiqueta = new Label(textoCantidad);
        String estiloBase = "-fx-font-family:monospace;-fx-font-size:13px;-fx-font-weight:bold;";
        String estiloColor = "-fx-text-fill:" + color + ";";
        String estiloEtiqueta = estiloBase + estiloColor;
        etiqueta.setStyle(estiloEtiqueta);
        return etiqueta;
    }
}
