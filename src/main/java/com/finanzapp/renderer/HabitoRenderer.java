package com.finanzapp.renderer;

import com.finanzapp.model.Habito;
import com.finanzapp.util.Formateador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class HabitoRenderer {

    public VBox crearTarjeta(Habito habito,
                             Consumer<Integer> alCambiarFreqActual,
                             Consumer<Integer> alCambiarFreqObj,
                             Runnable alEliminar) {
        VBox tarjeta = new VBox(10);
        tarjeta.setPrefWidth(240);
        tarjeta.setPadding(new Insets(16));
        tarjeta.setStyle(estiloNormal());
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloHover()));
        tarjeta.setOnMouseExited(e  -> tarjeta.setStyle(estiloNormal()));

        HBox cabecera    = crearCabecera(habito);
        HBox filaActual  = crearFilaFrecuencia("Actual:", habito, habito.getFrecuenciaActual(), 99, alCambiarFreqActual);
        HBox filaObj     = crearFilaFrecuencia("Objetivo:", habito, habito.getFrecuenciaObj(), 50, alCambiarFreqObj);
        Label costoLabel = crearEtiquetaCosto(habito);
        Label ahorroLabel = crearEtiquetaAhorro(habito);
        Button botonEliminar = crearBotonEliminar(alEliminar);

        tarjeta.getChildren().addAll(cabecera, new Separator(), filaActual, filaObj, costoLabel, ahorroLabel, botonEliminar);
        return tarjeta;
    }


    private HBox crearCabecera(Habito habito) {
        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label emojiLabel = new Label(habito.getEmoji());
        emojiLabel.setStyle("-fx-font-size:22px;");

        VBox nameBox = new VBox(2);
        Label nombre = new Label(habito.getNombre());
        nombre.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:-color-text;");
        double costeHabito = habito.getCoste();
        String descripcionHabito = habito.getDescripcion();
        String parteCoste = costeHabito + "€ ";
        String descripcionTexto = parteCoste + descripcionHabito;
        Label descripcion = new Label(descripcionTexto);
        descripcion.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        nameBox.getChildren().addAll(nombre, descripcion);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        cabecera.getChildren().addAll(emojiLabel, nameBox);
        return cabecera;
    }

    private HBox crearFilaFrecuencia(String etiquetaTexto, Habito habito,
                                     int valorInicial, int maximo, Consumer<Integer> alCambiar) {
        HBox fila = new HBox(8);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label etiqueta = new Label(etiquetaTexto);
        etiqueta.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");

        Spinner<Integer> spinner = new Spinner<>(0, maximo, valorInicial);
        spinner.setEditable(true);
        spinner.setPrefWidth(80);
        spinner.valueProperty().addListener((obs, oldV, newV) -> alCambiar.accept(newV));

        String unidadHabito = habito.getUnidad();
        String textoUnidad = "/" + unidadHabito;
        Label unidad = new Label(textoUnidad);
        unidad.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");

        fila.getChildren().addAll(etiqueta, spinner, unidad);
        return fila;
    }

    private Label crearEtiquetaCosto(Habito habito) {
        double gastoMensualActual = habito.getGastoActual();
        String gastoFormateado = Formateador.moneda(gastoMensualActual);
        String textoCosto = "Coste/mes actual: " + gastoFormateado + "€";
        Label etiqueta = new Label(textoCosto);
        etiqueta.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text2;");
        return etiqueta;
    }

    private Label crearEtiquetaAhorro(Habito habito) {
        double ahorroPotencial = habito.getAhorroPotencial();

        String texto;
        String color;
        if (ahorroPotencial > 0) {
            String ahorroFormateado = Formateador.moneda(ahorroPotencial);
            texto = "💚 Ahorro posible: " + ahorroFormateado + "€/mes";
            color = "#1D9E75";
        } else {
            texto = "Sin margen de ahorro";
            color = "-color-text3";
        }

        Label etiqueta = new Label(texto);
        String estiloAhorro = "-fx-font-size:11px;-fx-text-fill:" + color + ";";
        etiqueta.setStyle(estiloAhorro);
        return etiqueta;
    }

    private Button crearBotonEliminar(Runnable alEliminar) {
        Button boton = new Button("🗑 Eliminar");
        boton.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;-fx-cursor:hand;-fx-font-size:11px;");
        boton.setOnAction(e -> alEliminar.run());
        return boton;
    }

    private String estiloNormal() {
        return "-fx-background-color:-color-surface;-fx-background-radius:14;-fx-border-radius:14;" +
            "-fx-border-color:-color-border;-fx-border-width:1;-fx-cursor:hand;";
    }

    private String estiloHover() {
        return "-fx-background-color:-color-surface2;-fx-background-radius:14;-fx-border-radius:14;" +
            "-fx-border-color:-color-border2;-fx-border-width:1;-fx-cursor:hand;";
    }
}
