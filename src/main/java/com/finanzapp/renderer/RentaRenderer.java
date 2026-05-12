package com.finanzapp.renderer;

import com.finanzapp.model.RentaCalculo;
import com.finanzapp.service.RentaService;
import com.finanzapp.util.Formateador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Renderiza los tramos IRPF simplificados en el panel de resultados.
 */
public class RentaRenderer {

    public void renderizarTramos(VBox tramosBox, RentaCalculo calculo) {
        tramosBox.getChildren().clear();

        Label hdr = new Label("TRAMOS APLICADOS (escala orientativa)");
        hdr.setStyle("-fx-font-size:10px;-fx-font-weight:600;" +
                     "-fx-text-fill:-color-text3;-fx-padding:4 0 4 0;");
        tramosBox.getChildren().add(hdr);

        double base = calculo.getBaseImponible();

        for (double[] t : RentaService.TRAMOS) {
            if (base <= 0 || t[0] >= calculo.getBaseImponible()) break;

            double hasta = Math.min(calculo.getBaseImponible(), t[1]);
            double aplicable = hasta - t[0];
            double cuota = aplicable * (t[2] / 100.0);

            HBox fila = crearFila(t[0], t[1], t[2], cuota);
            tramosBox.getChildren().add(fila);
        }
    }

    private HBox crearFila(double desde, double hastaOriginal, double tipo, double cuota) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(5, 8, 5, 8));
        fila.setStyle("-fx-background-color:-color-surface2;-fx-background-radius:6;");

        String textoHasta = hastaOriginal == Double.MAX_VALUE
            ? "+"
            : Formateador.moneda(hastaOriginal) + "€";

        Label rango = new Label(Formateador.moneda(desde) + "€ – " + textoHasta);
        rango.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
        HBox.setHgrow(rango, Priority.ALWAYS);

        Label tipoLabel = new Label(String.format("%.0f%%", tipo));
        tipoLabel.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:-color-text;");

        Label cuotaLabel = new Label(Formateador.moneda(cuota) + "€");
        cuotaLabel.setStyle("-fx-font-size:12px;-fx-font-family:monospace;-fx-text-fill:#185FA5;");

        fila.getChildren().addAll(rango, tipoLabel, cuotaLabel);
        return fila;
    }
}
