package com.finanzapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PrestamosController implements Initializable {

    @FXML private Slider sliderCapital, sliderPlazo, sliderTin;
    @FXML private Label  lblCapitalVal, lblPlazoVal, lblTinVal;
    @FXML private TextField txtComision, txtSeguro;
    @FXML private Label  lblCuota, lblCuotaSub, lblTotal, lblIntereses, lblCostePor100, lblConsejo;
    @FXML private Label  lblPctCap, lblPctInt;
    @FXML private HBox   barraDistrib;
    @FXML private Button btnPersonal, btnHipoteca, btnCoche, btnEstudios;

    @FXML private TableView<String[]>   tablaAmort;
    @FXML private TableColumn<String[], String> colMes, colCuota, colCap, colInt, colSaldo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Slider listeners
        sliderCapital.valueProperty().addListener((o, ov, nv) -> {
            double v = Math.round(nv.doubleValue() / 500.0) * 500.0;
            lblCapitalVal.setText(fmt(v) + "€");
            calcular();
        });
        sliderPlazo.valueProperty().addListener((o, ov, nv) -> {
            int v = (int) Math.round(nv.doubleValue() / 6.0) * 6;
            lblPlazoVal.setText(v + " meses");
            calcular();
        });
        sliderTin.valueProperty().addListener((o, ov, nv) -> {
            double v = Math.round(nv.doubleValue() * 2.0) / 2.0;
            lblTinVal.setText(String.format("%.1f%%", v).replace(".", ","));
            calcular();
        });
        txtComision.textProperty().addListener((o, ov, nv) -> calcular());
        txtSeguro.textProperty().addListener((o, ov, nv)   -> calcular());

        // Table columns
        colMes.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[0]));
        colCuota.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));
        colCap.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[2]));
        colInt.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[3]));
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[4]));

        lblCapitalVal.setText("10.000€");
        lblPlazoVal.setText("36 meses");
        lblTinVal.setText("6,5%");
        calcular();
    }

    @FXML void setTipo(javafx.event.ActionEvent e) {
        for (Button b : new Button[]{btnPersonal,btnHipoteca,btnCoche,btnEstudios})
            b.getStyleClass().remove("loan-type-active");
        Button src = (Button) e.getSource();
        src.getStyleClass().add("loan-type-active");

        // Set default values per type
        if (src == btnPersonal)  { sliderCapital.setValue(10000); sliderPlazo.setValue(36); sliderTin.setValue(6.5); }
        if (src == btnHipoteca)  { sliderCapital.setValue(150000); sliderPlazo.setValue(300); sliderTin.setValue(3.0); }
        if (src == btnCoche)     { sliderCapital.setValue(15000); sliderPlazo.setValue(60); sliderTin.setValue(5.5); }
        if (src == btnEstudios)  { sliderCapital.setValue(8000); sliderPlazo.setValue(84); sliderTin.setValue(4.0); }
        calcular();
    }

    private void calcular() {
        double capital  = sliderCapital.getValue();
        int    meses    = (int) sliderPlazo.getValue();
        double tin      = sliderTin.getValue() / 100.0;
        double comision = parseDouble(txtComision.getText()) / 100.0;
        double seguro   = parseDouble(txtSeguro.getText());

        // French amortization (cuota constante)
        double rm = tin / 12.0;
        double cuota;
        if (rm == 0) {
            cuota = capital / meses;
        } else {
            cuota = capital * rm * Math.pow(1 + rm, meses) / (Math.pow(1 + rm, meses) - 1);
        }
        cuota += seguro;

        double totalPagar     = cuota * meses + capital * comision;
        double interesesTotales = totalPagar - capital - capital * comision;
        double costePor100    = (totalPagar / capital) * 100;

        lblCuota.setText(fmt2(cuota) + "€");
        lblCuotaSub.setText("durante " + meses + " meses");
        lblTotal.setText(fmt2(totalPagar) + "€");
        lblIntereses.setText(fmt2(interesesTotales) + "€");
        lblCostePor100.setText(fmt2(costePor100) + "€");

        // Barra distribución
        double pctCap = capital / totalPagar;
        barraDistrib.getChildren().clear();
        Region capBar = new Region();
        capBar.setStyle("-fx-background-color:#185FA5;-fx-background-radius:6 0 0 6;");
        capBar.setPrefHeight(20);
        Region intBar = new Region();
        intBar.setStyle("-fx-background-color:#D85A30;-fx-opacity:0.7;-fx-background-radius:0 6 6 0;");
        intBar.setPrefHeight(20);
        HBox.setHgrow(capBar, Priority.ALWAYS);
        HBox.setHgrow(intBar, Priority.ALWAYS);
        capBar.setMaxWidth(pctCap * 300); // approximate
        intBar.setMaxWidth((1 - pctCap) * 300);
        barraDistrib.getChildren().addAll(capBar, intBar);
        lblPctCap.setText(String.format("%.0f%%", pctCap * 100));
        lblPctInt.setText(String.format("%.0f%%", (1 - pctCap) * 100));

        // Consejo
        if (tin * 100 > 15) lblConsejo.setText("⚠ El TIN es elevado. Compara con otras entidades antes de firmar.");
        else if (meses > 180) lblConsejo.setText("Préstamo a largo plazo: los intereses totales son muy altos. Considera amortizar anticipadamente.");
        else if (interesesTotales / capital < 0.1) lblConsejo.setText("✓ Buen préstamo: los intereses representan menos del 10% del capital.");
        else lblConsejo.setText("Consejo: reducir el plazo en 6 meses podría ahorrarte " + fmt2(cuota * 6 - interesesTotales * 0.05) + "€ en intereses.");

        // Tabla amortización (primeros 24 meses)
        List<String[]> rows = new ArrayList<>();
        double saldo = capital;
        int limit = Math.min(meses, 24);
        for (int i = 1; i <= limit; i++) {
            double intMes   = saldo * rm;
            double capMes   = cuota - seguro - intMes;
            if (capMes < 0) capMes = 0;
            saldo -= capMes;
            rows.add(new String[]{
                String.valueOf(i),
                fmt2(cuota) + "€",
                fmt2(capMes) + "€",
                fmt2(intMes) + "€",
                fmt2(Math.max(0, saldo)) + "€"
            });
        }
        tablaAmort.setItems(FXCollections.observableArrayList(rows));
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replace(",",".")); } catch (Exception e) { return 0; }
    }

    private String fmt(double v)  { return String.format("%,.0f", v).replace(",","."); }
    private String fmt2(double v) { return String.format("%,.2f", v).replace(",","·").replace(".",",").replace("·","."); }
}
