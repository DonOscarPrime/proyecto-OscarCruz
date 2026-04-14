package com.finanzapp.controller;

import com.finanzapp.util.DatabaseConnection;
import com.finanzapp.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PrestamosController implements Initializable {

    // ── Inputs ────────────────────────────────────────────────
    @FXML private TextField txtCapital, txtPlazo, txtTin, txtComision, txtSeguro;
    @FXML private Label     lblError, lblGuardado;

    // ── Tipo ──────────────────────────────────────────────────
    @FXML private Button    btnPersonal, btnHipoteca, btnCoche, btnEstudios;
    @FXML private TextField txtTipoPersonalizado;
    @FXML private Label     lblTipoActivo;
    private String tipoActual = "Personal";

    // ── Resultado ─────────────────────────────────────────────
    @FXML private Label  lblCuota, lblCuotaSub, lblTotal, lblIntereses, lblCostePor100, lblConsejo;
    @FXML private Label  lblPctCap, lblPctInt;
    @FXML private HBox   barraDistrib;

    // ── Tabla amortización ────────────────────────────────────
    @FXML private TableView<String[]>             tablaAmort;
    @FXML private TableColumn<String[], String>   colMes, colCuota, colCap, colInt, colSaldo;

    // ── Historial ─────────────────────────────────────────────
    @FXML private TableView<String[]>             tablaHistorial;
    @FXML private TableColumn<String[], String>   hColTipo, hColCapital, hColPlazo, hColTin, hColCuota;

    // ── Valores calculados (para guardar) ─────────────────────
    private double cuotaCalc, totalCalc, interesesCalc;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Columnas tabla amortización
        colMes.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[0]));
        colCuota.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));
        colCap.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[2]));
        colInt.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[3]));
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[4]));

        // Columnas historial
        hColTipo.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue()[0]));
        hColCapital.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));
        hColPlazo.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[2]));
        hColTin.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue()[3]));
        hColCuota.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue()[4]));

        // Filtro: solo dígitos, punto y coma en los campos numéricos
        aplicarFiltroNumerico(txtCapital);
        aplicarFiltroNumerico(txtPlazo);
        aplicarFiltroNumerico(txtTin);
        aplicarFiltroNumerico(txtComision);
        aplicarFiltroNumerico(txtSeguro);

        // Tipo personalizado: al escribir, deselecciona los botones predefinidos
        txtTipoPersonalizado.textProperty().addListener((o, ov, nv) -> {
            String txt = nv.trim();
            if (!txt.isEmpty()) {
                deselectarBotones();
                tipoActual = txt;
                lblTipoActivo.setText("Tipo seleccionado: " + tipoActual);
                limpiarGuardado();
            }
        });

        // Recalcular en tiempo real
        txtCapital.textProperty().addListener((o, ov, nv) -> { limpiarGuardado(); calcular(); });
        txtPlazo.textProperty().addListener((o, ov, nv)   -> { limpiarGuardado(); calcular(); });
        txtTin.textProperty().addListener((o, ov, nv)     -> { limpiarGuardado(); calcular(); });
        txtComision.textProperty().addListener((o, ov, nv) -> calcular());
        txtSeguro.textProperty().addListener((o, ov, nv)   -> calcular());

        cargarHistorial();
        calcular();
    }

    // ── Tipo de préstamo ──────────────────────────────────────

    @FXML void setTipo(javafx.event.ActionEvent e) {
        deselectarBotones();
        Button src = (Button) e.getSource();
        src.getStyleClass().add("loan-type-active");
        tipoActual = src.getText();

        // Limpiar tipo personalizado al elegir uno predefinido
        txtTipoPersonalizado.setText("");
        lblTipoActivo.setText("Tipo seleccionado: " + tipoActual);

        // Valores por defecto según tipo
        switch (tipoActual) {
            case "Personal"  -> setInputs("10000",  "36",  "6.5");
            case "Hipoteca"  -> setInputs("150000", "300", "3.0");
            case "Coche"     -> setInputs("15000",  "60",  "5.5");
            case "Estudios"  -> setInputs("8000",   "84",  "4.0");
        }
        limpiarGuardado();
        calcular();
    }

    /** Confirma el tipo personalizado al pulsar Enter en el campo. */
    @FXML void setTipoPersonalizado() {
        String txt = txtTipoPersonalizado.getText().trim();
        if (!txt.isEmpty()) {
            deselectarBotones();
            tipoActual = txt;
            lblTipoActivo.setText("Tipo seleccionado: " + tipoActual);
            limpiarGuardado();
        }
    }

    private void deselectarBotones() {
        for (Button b : new Button[]{btnPersonal, btnHipoteca, btnCoche, btnEstudios})
            b.getStyleClass().remove("loan-type-active");
    }

    private void setInputs(String capital, String plazo, String tin) {
        txtCapital.setText(capital);
        txtPlazo.setText(plazo);
        txtTin.setText(tin);
    }

    // ── Cálculo ───────────────────────────────────────────────

    private void calcular() {
        lblError.setText("");

        double capital  = parseDouble(txtCapital.getText());
        int    meses    = (int) parseDouble(txtPlazo.getText());
        double tin      = parseDouble(txtTin.getText());
        double comision = parseDouble(txtComision.getText()) / 100.0;
        double seguro   = parseDouble(txtSeguro.getText());

        // Validaciones
        if (capital <= 0)  { mostrarError("El capital debe ser mayor que 0."); return; }
        if (meses   <= 0)  { mostrarError("El plazo debe ser mayor que 0 meses."); return; }
        if (tin     < 0)   { mostrarError("El TIN no puede ser negativo."); return; }
        if (tin     > 100) { mostrarError("El TIN parece demasiado alto. Compruébalo (ej: 6.5 para 6,5%)."); return; }

        // Amortización francesa
        double rm = tin / 100.0 / 12.0;
        double cuota;
        if (rm == 0) {
            cuota = capital / meses;
        } else {
            cuota = capital * rm * Math.pow(1 + rm, meses) / (Math.pow(1 + rm, meses) - 1);
        }
        cuota += seguro;

        double totalPagar        = cuota * meses + capital * comision;
        double interesesTotales  = totalPagar - capital - capital * comision;
        double costePor100       = (totalPagar / capital) * 100;

        // Guardar para el botón "Guardar"
        cuotaCalc     = cuota;
        totalCalc     = totalPagar;
        interesesCalc = interesesTotales;

        // Labels resultado
        lblCuota.setText(fmt2(cuota) + "€");
        lblCuotaSub.setText("durante " + meses + " meses · TIN " +
                            String.format("%.2f", tin).replace(".", ",") + "%");
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
        double total = pctCap + (1 - pctCap);
        capBar.setMaxWidth(pctCap * 280);
        intBar.setMaxWidth((1 - pctCap) * 280);
        barraDistrib.getChildren().addAll(capBar, intBar);
        lblPctCap.setText(String.format("%.0f%%", pctCap * 100));
        lblPctInt.setText(String.format("%.0f%%", (1 - pctCap) * 100));

        // Consejo
        if (tin > 15)
            lblConsejo.setText("⚠ TIN elevado. Compara con otras entidades antes de firmar.");
        else if (meses > 180)
            lblConsejo.setText("Préstamo a largo plazo: los intereses totales son muy altos. Considera amortizar anticipadamente.");
        else if (interesesTotales / capital < 0.10)
            lblConsejo.setText("✓ Buen préstamo: los intereses representan menos del 10% del capital.");
        else
            lblConsejo.setText("Reducir el plazo en 6 meses podría ahorrarte ~" +
                               fmt2(cuota * 6 * 0.05) + "€ en intereses.");

        // Tabla amortización (primeros 24 meses)
        List<String[]> rows = new ArrayList<>();
        double saldo = capital;
        int limit = Math.min(meses, 24);
        for (int i = 1; i <= limit; i++) {
            double intMes = saldo * rm;
            double capMes = cuota - seguro - intMes;
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

    // ── Guardar simulación ────────────────────────────────────

    @FXML void guardarSimulacion() {
        double capital  = parseDouble(txtCapital.getText());
        int    meses    = (int) parseDouble(txtPlazo.getText());
        double tin      = parseDouble(txtTin.getText());

        if (capital <= 0 || meses <= 0) {
            lblGuardado.setStyle("-fx-text-fill:-color-danger;");
            lblGuardado.setText("Corrige los datos antes de guardar.");
            return;
        }

        String sql = """
            INSERT INTO simulaciones_prestamo
                (usuario_id, tipo_prestamo, capital, plazo_meses, tin,
                 cuota_mensual, total_pagar, total_intereses)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.getInstance().getUsuarioActual().getId());
            ps.setString(2, tipoActual);
            ps.setDouble(3, capital);
            ps.setInt(4, meses);
            ps.setDouble(5, tin);
            ps.setDouble(6, cuotaCalc);
            ps.setDouble(7, totalCalc);
            ps.setDouble(8, interesesCalc);
            ps.executeUpdate();

            lblGuardado.setStyle("-fx-text-fill:-color-accent;");
            lblGuardado.setText("✓ Simulación guardada correctamente.");
            cargarHistorial();
        } catch (SQLException e) {
            e.printStackTrace();
            lblGuardado.setStyle("-fx-text-fill:-color-danger;");
            lblGuardado.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }

    // ── Cargar historial ──────────────────────────────────────

    private void cargarHistorial() {
        String sql = """
            SELECT tipo_prestamo, capital, plazo_meses, tin, cuota_mensual
            FROM simulaciones_prestamo
            WHERE usuario_id = ?
            ORDER BY created_at DESC
            LIMIT 10
            """;
        List<String[]> rows = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.getInstance().getUsuarioActual().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("tipo_prestamo"),
                    fmt2(rs.getDouble("capital")) + "€",
                    rs.getInt("plazo_meses") + " meses",
                    String.format("%.2f%%", rs.getDouble("tin")).replace(".", ","),
                    fmt2(rs.getDouble("cuota_mensual")) + "€"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tablaHistorial.setItems(FXCollections.observableArrayList(rows));
    }

    // ── Helpers ───────────────────────────────────────────────

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblCuota.setText("—");
        lblTotal.setText("—");
        lblIntereses.setText("—");
        lblCostePor100.setText("—");
        lblCuotaSub.setText(" ");
        tablaAmort.setItems(FXCollections.emptyObservableList());
    }

    private void limpiarGuardado() {
        lblGuardado.setText("");
    }

    /** Solo permite dígitos, punto y coma en el TextField. */
    private void aplicarFiltroNumerico(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*[.,]?[0-9]*")) return change;
            return null;
        }));
    }

    private double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return 0; }
    }

    private String fmt2(double v) {
        return String.format("%,.2f", v).replace(",", "·").replace(".", ",").replace("·", ".");
    }
}
