package com.finanzapp.renderer;

import com.finanzapp.model.Movimiento;
import com.finanzapp.util.Formateador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class MovimientoRenderer {

    private final DateTimeFormatter formatoCorto  = DateTimeFormatter.ofPattern("d MMM",      new Locale("es"));
    private final DateTimeFormatter formatoLargo  = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"));


    public HBox crearFilaConEliminar(Movimiento m, Runnable alEliminar) {
        HBox fila = crearContenedorFila(12);

        Label icono       = crearIcono(m);
        VBox  infoBox     = crearInfoBox(m);
        Label etiquetaFecha    = crearEtiquetaFechaCorta(m);
        Label etiquetaCantidad = crearEtiquetaCantidad(m);
        Button botonEliminar   = crearBotonEliminar(alEliminar);

        fila.getChildren().addAll(icono, infoBox, etiquetaFecha, etiquetaCantidad, botonEliminar);
        return fila;
    }

    public HBox crearFilaCompleta(Movimiento m) {
        HBox fila = crearContenedorFilaConHover(12);

        Label icono            = crearIcono(m);
        VBox  infoBox          = crearInfoBoxConNotas(m);
        Label etiquetaFecha    = crearEtiquetaFechaLarga(m);
        Label badge            = crearBadgeTipo(m);
        Label etiquetaCantidad = crearEtiquetaCantidad(m);

        fila.getChildren().addAll(icono, infoBox, etiquetaFecha, badge, etiquetaCantidad);
        return fila;
    }

    public Label crearMensajeVacio(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-text-fill:-color-text3;-fx-padding:24px;");
        return etiqueta;
    }

    private Label crearIcono(Movimiento m) {
        String emojiRaw = m.getCategoriaEmoji();
        String emoji;
        if (emojiRaw == null) {
            emoji = "💳";
        } else {
            emoji = emojiRaw;
        }
        Label icono = new Label(emoji);

        String fondoIcono;
        if (m.isIngreso()) {
            fondoIcono = "-fx-background-color:rgba(29,158,117,0.15);";
        } else {
            fondoIcono = "-fx-background-color:-color-surface2;";
        }
        String estiloBaseIcono = "-fx-font-size:18px;-fx-min-width:36px;-fx-min-height:36px;" +
            "-fx-background-radius:10;-fx-alignment:center;";
        String estiloIcono = estiloBaseIcono + fondoIcono;
        icono.setStyle(estiloIcono);
        return icono;
    }

    private VBox crearInfoBox(Movimiento m) {
        VBox infoBox = new VBox(2);
        Label nombre = new Label(m.getNombre());
        nombre.setStyle("-fx-font-weight:500;-fx-font-size:13px;-fx-text-fill:-color-text;");
        Label categoria = new Label(m.getCategoriaDisplay());
        categoria.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        infoBox.getChildren().addAll(nombre, categoria);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        return infoBox;
    }

    private VBox crearInfoBoxConNotas(Movimiento m) {
        VBox infoBox = new VBox(2);
        Label nombre = new Label(m.getNombre());
        nombre.setStyle("-fx-font-weight:500;-fx-font-size:13px;-fx-text-fill:-color-text;");
        Label categoria = new Label(m.getCategoriaDisplay());
        categoria.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");

        boolean notasNoNulas = m.getNotas() != null;
        boolean notasNoVacias = notasNoNulas && !m.getNotas().isBlank();
        boolean tieneNotas = notasNoNulas && notasNoVacias;
        if (tieneNotas) {
            String contenidoNota = m.getNotas();
            String textoNota = "📝 " + contenidoNota;
            Label nota = new Label(textoNota);
            nota.setStyle("-fx-font-size:10px;-fx-text-fill:-color-text3;");
            infoBox.getChildren().addAll(nombre, categoria, nota);
        } else {
            infoBox.getChildren().addAll(nombre, categoria);
        }

        HBox.setHgrow(infoBox, Priority.ALWAYS);
        return infoBox;
    }

    private Label crearEtiquetaFechaCorta(Movimiento m) {
        String textoFecha = "";
        if (m.getFecha() != null) {
            textoFecha = m.getFecha().format(formatoCorto);
        }
        Label etiqueta = new Label(textoFecha);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;min-width:50px;");
        return etiqueta;
    }

    private Label crearEtiquetaFechaLarga(Movimiento m) {
        String textoFecha = "";
        if (m.getFecha() != null) {
            textoFecha = m.getFecha().format(formatoLargo);
        }
        Label etiqueta = new Label(textoFecha);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;min-width:90px;");
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

    private Label crearBadgeTipo(Movimiento m) {
        String texto;
        String estiloColor;
        if (m.isIngreso()) {
            texto       = "Ingreso";
            estiloColor = "-fx-background-color:#E1F5EE;-fx-text-fill:#0F6E56;";
        } else {
            texto       = "Gasto";
            estiloColor = "-fx-background-color:#FAECE7;-fx-text-fill:#D85A30;";
        }
        Label badge = new Label(texto);
        String estiloBase = "-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:6;";
        String estiloBadge = estiloBase + estiloColor;
        badge.setStyle(estiloBadge);
        return badge;
    }

    private Button crearBotonEliminar(Runnable alEliminar) {
        Button boton = new Button("✕");
        boton.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;-fx-cursor:hand;-fx-font-size:11px;");
        boton.setOnAction(e -> alEliminar.run());
        return boton;
    }

    private HBox crearContenedorFila(int espaciado) {
        HBox fila = new HBox(espaciado);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 12, 10, 12));
        fila.setStyle("-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;");
        return fila;
    }

    private HBox crearContenedorFilaConHover(int espaciado) {
        HBox fila = new HBox(espaciado);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        String estiloNormal = "-fx-background-color:transparent;-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;";
        String estiloHover  = "-fx-background-color:-color-surface2;-fx-border-color:transparent transparent -color-border transparent;-fx-border-width:0 0 1 0;";
        fila.setStyle(estiloNormal);
        fila.setOnMouseEntered(e -> fila.setStyle(estiloHover));
        fila.setOnMouseExited(e  -> fila.setStyle(estiloNormal));
        return fila;
    }
}
