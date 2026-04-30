package com.finanzapp.controller;

import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.model.Objetivo;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del panel de objetivos de ahorro de Fox Wallet.
 * <p>
 * Muestra las metas financieras del usuario en tarjetas con barra de progreso.
 * Permite crear nuevos objetivos, añadir aportaciones y eliminarlos.
 * El progreso se actualiza a través del procedimiento almacenado MySQL.
 */
public class ObjetivosController implements Initializable {

    @FXML private VBox     formPanel;
    @FXML private FlowPane objetivosGrid;
    @FXML private TextField txtNombre, txtEmoji, txtObjetivo, txtActual;
    @FXML private DatePicker dateLimite;
    @FXML private Label lblError;

    private final ObjetivoDAO dao = new ObjetivoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) { cargarObjetivosDeAhorro(); }

    /** Carga los objetivos de ahorro del usuario y renderiza sus tarjetas. */
    private void cargarObjetivosDeAhorro() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        List<Objetivo> objetivos = dao.obtenerObjetivosDeUsuario(uid);
        objetivosGrid.getChildren().clear();
        for (Objetivo objetivo : objetivos)
            objetivosGrid.getChildren().add(construirTarjetaObjetivo(objetivo));
    }

    /** Construye la tarjeta visual de un objetivo de ahorro con barra de progreso. */
    private VBox construirTarjetaObjetivo(Objetivo objetivo) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPadding(new Insets(16));
        boolean dark = Session.getInstance().isDarkMode();
        String bg = objetivo.isCompletado() ? (dark ? "#1A3D30" : "#E1F5EE") : "-color-surface";
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;" +
            "-fx-border-radius:14;-fx-border-color:-color-border;-fx-border-width:1;");

        // Cabecera: emoji + nombre del objetivo
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label emoji = new Label(objetivo.getEmoji() != null ? objetivo.getEmoji() : "🎯");
        emoji.setStyle("-fx-font-size:24px;");
        Label name = new Label(objetivo.getNombre());
        name.setStyle("-fx-font-size:14px;-fx-font-weight:600;-fx-text-fill:-color-text;");
        HBox.setHgrow(name, Priority.ALWAYS);
        if (objetivo.isCompletado()) {
            Label badge = new Label("✓ Completado");
            badge.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;" +
                "-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:10px;");
            header.getChildren().addAll(emoji, name, badge);
        } else {
            header.getChildren().addAll(emoji, name);
        }

        // Barra de progreso hacia la meta
        double porcentaje = objetivo.getPorcentajeProgreso();
        ProgressBar pb = new ProgressBar(porcentaje / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent:" + (objetivo.isCompletado() ? "#1D9E75" : "#185FA5") + ";-fx-pref-height:10px;");

        Label pctLbl = new Label(String.format("%.0f%% · %.0f€ de %.0f€",
            porcentaje, objetivo.getActual(), objetivo.getObjetivo()));
        pctLbl.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text2;");

        Label restante = new Label("Faltan: " + fmt(objetivo.getImporteRestante()) + "€");
        restante.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text3;");

        // Fila de aporte rápido
        HBox aporteRow = new HBox(6);
        aporteRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField aporteTxt = new TextField();
        aporteTxt.setPromptText("Añadir €");
        aporteTxt.setPrefWidth(90);
        aporteTxt.setStyle("-fx-background-radius:6;-fx-border-radius:6;" +
            "-fx-border-color:-color-border2;-fx-padding:4 8;-fx-font-size:12px;");

        Button aporteBtn = new Button("Aportar");
        aporteBtn.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;" +
            "-fx-background-radius:6;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
        aporteBtn.setOnAction(e -> {
            try {
                double importeAporte = Double.parseDouble(aporteTxt.getText().replace(",","."));
                double nuevoTotal    = Math.min(objetivo.getObjetivo(), objetivo.getActual() + importeAporte);
                dao.registrarAporteObjetivo(objetivo.getId(), nuevoTotal);
                cargarObjetivosDeAhorro();
            } catch (NumberFormatException ex) { /* importe no válido, ignorar */ }
        });

        Button delBtn = new Button("🗑");
        delBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;" +
            "-fx-cursor:hand;-fx-font-size:12px;");
        delBtn.setOnAction(e -> { dao.eliminarObjetivo(objetivo.getId()); cargarObjetivosDeAhorro(); });

        aporteRow.getChildren().addAll(aporteTxt, aporteBtn, delBtn);
        card.getChildren().addAll(header, pb, pctLbl, restante, aporteRow);

        if (objetivo.getFechaLimite() != null) {
            Label limite = new Label("Fecha límite: " + objetivo.getFechaLimite());
            limite.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
            card.getChildren().add(limite);
        }
        return card;
    }

    @FXML void toggleForm() {
        boolean visible = !formPanel.isVisible();
        formPanel.setVisible(visible);
        formPanel.setManaged(visible);
        if (!visible) {
            txtNombre.clear(); txtEmoji.clear(); txtObjetivo.clear();
            txtActual.clear(); lblError.setText("");
        }
    }

    @FXML void guardar() {
        lblError.setText("");
        String nombre = txtNombre.getText().trim();
        String objStr = txtObjetivo.getText().trim().replace(",",".");
        if (nombre.isEmpty() || objStr.isEmpty()) {
            lblError.setText("Rellena los campos obligatorios.");
            return;
        }
        double importeMeta;
        try { importeMeta = Double.parseDouble(objStr); }
        catch (NumberFormatException e) { lblError.setText("Importe no válido."); return; }

        double importeInicial = 0;
        if (!txtActual.getText().isBlank()) {
            try { importeInicial = Double.parseDouble(txtActual.getText().replace(",",".")); }
            catch (NumberFormatException ignored) {}
        }

        Objetivo nuevoObjetivo = new Objetivo();
        nuevoObjetivo.setUsuarioId(Session.getInstance().getUsuarioActual().getId());
        nuevoObjetivo.setNombre(nombre);
        nuevoObjetivo.setObjetivo(importeMeta);
        nuevoObjetivo.setActual(importeInicial);
        nuevoObjetivo.setEmoji(txtEmoji.getText().isBlank() ? "🎯" : txtEmoji.getText().trim());
        nuevoObjetivo.setFechaLimite(dateLimite.getValue());

        if (dao.crearObjetivo(nuevoObjetivo)) {
            toggleForm();
            cargarObjetivosDeAhorro();
        } else {
            lblError.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
