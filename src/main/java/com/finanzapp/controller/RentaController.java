package com.finanzapp.controller;

import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class RentaController implements Initializable {

    @FXML private ComboBox<String> cmbComunidad, cmbSituacion;
    @FXML private Spinner<Integer> spinHijos;
    @FXML private TextField txtSalario, txtRetenciones, txtCapMob, txtCapInm, txtGanancias;
    @FXML private VBox  resultPanel, tramosBox;
    @FXML private Label lblBase, lblCuota, lblRetenciones, lblResultado;

    // Tramos IRPF estatal 2024 (general)
    private static final double[][] TRAMOS_ESTATAL = {
        {0,      12450,  9.5},
        {12450,  20200,  12.0},
        {20200,  35200,  15.0},
        {35200,  60000,  18.5},
        {60000,  300000, 22.5},
        {300000, Double.MAX_VALUE, 24.5}
    };

    // Tramos autonómicos simplificados (Madrid)
    private static final double[][] TRAMOS_MADRID = {
        {0,      12450,  9.0},
        {12450,  17707,  11.2},
        {17707,  33007,  13.3},
        {33007,  53407,  17.9},
        {53407,  Double.MAX_VALUE, 21.0}
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbComunidad.setItems(FXCollections.observableArrayList(
            "Madrid","Cataluña","Andalucía","Valencia","País Vasco","Galicia","Castilla y León","Otra"));
        String com = Session.getInstance().getUsuarioActual().getComunidad();
        cmbComunidad.setValue(com != null ? com : "Madrid");

        cmbSituacion.setItems(FXCollections.observableArrayList(
            "Soltero/a sin hijos","Casado/a sin hijos","Soltero/a con hijos","Casado/a con hijos","Viudo/a"));
        cmbSituacion.setValue("Soltero/a sin hijos");

        // Pre-fill salary from user profile
        double ing = Session.getInstance().getUsuarioActual().getIngresosNetos();
        if (ing > 0) txtSalario.setText(String.format("%.0f", ing * 12 * 1.25)); // approx bruto
    }

    @FXML void calcular() {
        double salarioBruto = parseD(txtSalario.getText());
        double retenciones  = parseD(txtRetenciones.getText());
        double capMob       = parseD(txtCapMob.getText());
        double capInm       = parseD(txtCapInm.getText());
        double ganancias    = parseD(txtGanancias.getText());

        if (salarioBruto <= 0) return;

        // Reducción por rendimientos del trabajo (simplificada)
        double reduccionTrabajo;
        if (salarioBruto <= 14852)       reduccionTrabajo = 6498;
        else if (salarioBruto <= 17000)  reduccionTrabajo = 6498 - 1.14 * (salarioBruto - 14852);
        else                             reduccionTrabajo = 2000;

        double rendNeto = Math.max(0, salarioBruto - reduccionTrabajo);
        double mcp = Math.max(0, salarioBruto - rendNeto); // mínimo contribuyente personal
        double baseGeneral = rendNeto + capInm;
        double baseAhorro  = capMob + ganancias;

        // Min. personal y familiar
        double minPersonal = 5550;
        int hijos = spinHijos.getValue();
        double minFamiliar = 0;
        if (hijos == 1) minFamiliar = 2400;
        else if (hijos == 2) minFamiliar = 2400 + 2700;
        else if (hijos >= 3) minFamiliar = 2400 + 2700 + 4000;
        double minTotal = minPersonal + minFamiliar;

        double cuotaEstatal    = calcularCuota(baseGeneral, TRAMOS_ESTATAL) - calcularCuota(minTotal, TRAMOS_ESTATAL) * 0.5;
        double cuotaAutonomica = calcularCuota(baseGeneral, TRAMOS_MADRID)  - calcularCuota(minTotal, TRAMOS_MADRID)  * 0.5;
        double cuotaAhorro     = baseAhorro > 0 ? cuotaAhorro(baseAhorro) : 0;
        double cuotaTotal      = Math.max(0, cuotaEstatal + cuotaAutonomica + cuotaAhorro);
        double resultado       = cuotaTotal - retenciones;

        lblBase.setText(fmt(baseGeneral) + "€");
        lblCuota.setText(fmt(cuotaTotal) + "€");
        lblRetenciones.setText(fmt(retenciones) + "€");
        lblResultado.setText((resultado >= 0 ? "A pagar: " : "A devolver: ") + fmt(Math.abs(resultado)) + "€");
        lblResultado.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + (resultado >= 0 ? "#D85A30" : "#1D9E75") + ";");

        renderTramos(baseGeneral);
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);
    }

    private double calcularCuota(double base, double[][] tramos) {
        double cuota = 0;
        for (double[] t : tramos) {
            if (base <= 0) break;
            double aplicable = Math.min(base, t[1] - t[0]);
            cuota += aplicable * (t[2] / 100.0);
            base  -= aplicable;
        }
        return cuota;
    }

    private double cuotaAhorro(double base) {
        double cuota = 0;
        double[][] tramos = {{0,6000,19},{6000,50000,21},{50000,200000,23},{200000,Double.MAX_VALUE,27}};
        for (double[] t : tramos) {
            if (base <= 0) break;
            double ap = Math.min(base, t[1] - t[0]);
            cuota += ap * (t[2] / 100.0);
            base  -= ap;
        }
        return cuota;
    }

    private void renderTramos(double base) {
        tramosBox.getChildren().clear();
        for (double[] t : TRAMOS_ESTATAL) {
            if (base <= 0 || t[0] >= base) break;
            double desde = t[0], hasta = Math.min(base, t[1]);
            double aplicable = hasta - desde;
            double cuota = aplicable * (t[2] / 100.0);

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 8, 6, 8));
            row.setStyle("-fx-background-color:#F7F6F3;-fx-background-radius:6;");

            Label rango  = new Label(fmt(desde) + "€ – " + (t[1] == Double.MAX_VALUE ? "+" : fmt(t[1]) + "€"));
            rango.setStyle("-fx-font-size:12px;-fx-text-fill:#6B6A65;"); HBox.setHgrow(rango, Priority.ALWAYS);
            Label tipo   = new Label(String.format("%.1f%%", t[2])); tipo.setStyle("-fx-font-size:12px;-fx-font-weight:600;");
            Label cuotaL = new Label(fmt(cuota) + "€"); cuotaL.setStyle("-fx-font-size:12px;-fx-font-family:monospace;-fx-text-fill:#D85A30;");
            row.getChildren().addAll(rango, tipo, cuotaL);
            tramosBox.getChildren().add(row);
        }
    }

    private double parseD(String s) {
        try { return Double.parseDouble(s.trim().replace(",",".")); } catch (Exception e) { return 0; }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
