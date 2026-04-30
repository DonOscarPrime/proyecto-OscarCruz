package com.finanzapp.controller;

import com.finanzapp.dao.HabitoDAO;
import com.finanzapp.model.Habito;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del simulador de ahorro de hábitos de Fox Wallet.
 * <p>
 * Permite al usuario registrar sus hábitos de gasto recurrentes (café, gimnasio,
 * suscripciones…), establecer una frecuencia objetivo y visualizar cuánto podría
 * ahorrar mensual y anualmente si reduce su consumo actual.
 */
public class SimuladorController implements Initializable {

    @FXML private Label lblActual, lblObj, lblMes, lblAnio, lblTip;
    @FXML private VBox     formPanel;
    @FXML private FlowPane habitosGrid;
    @FXML private TextField txtEmoji, txtNombre, txtCoste, txtFreqActual, txtFreqObj;
    @FXML private ComboBox<String> cmbUnidad;

    private final HabitoDAO dao = new HabitoDAO();
    private List<Habito> habitos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbUnidad.setItems(FXCollections.observableArrayList("semana", "mes"));
        cmbUnidad.setValue("semana");
        cargarHabitosYResumen();
    }

    /** Carga los hábitos del usuario y actualiza el resumen del simulador. */
    private void cargarHabitosYResumen() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        habitos = dao.obtenerHabitosDeUsuario(uid);
        actualizarResumenAhorroPotencial();
        renderizarTarjetasHabito();
    }

    /** Recalcula y muestra el ahorro mensual/anual potencial según los hábitos actuales. */
    private void actualizarResumenAhorroPotencial() {
        double actual = habitos.stream().mapToDouble(Habito::getGastoMensualActual).sum();
        double obj    = habitos.stream().mapToDouble(Habito::getGastoMensualObj).sum();
        double mesAhorro = actual - obj;
        double anioAhorro = mesAhorro * 12;

        lblActual.setText(fmt(actual) + "€/mes");
        lblObj.setText(fmt(obj) + "€/mes");
        lblMes.setText(fmt(mesAhorro) + "€");
        lblAnio.setText(fmt(anioAhorro) + "€");

        if (mesAhorro > 0) {
            lblTip.setText("💡 Podrías ahorrar " + fmt(mesAhorro) + "€ al mes (" + fmt(anioAhorro) + "€/año) reduciendo tus hábitos según los objetivos.");
        } else {
            lblTip.setText("Ajusta las frecuencias de tus hábitos para ver cómo cambia tu ahorro mensual.");
        }
    }

    /** Renderiza las tarjetas de hábitos en el grid del simulador. */
    private void renderizarTarjetasHabito() {
        habitosGrid.getChildren().clear();
        for (Habito h : habitos) habitosGrid.getChildren().add(construirTarjetaHabito(h));
    }

    /** Construye la tarjeta visual de un hábito con controles de frecuencia. */
    private VBox construirTarjetaHabito(Habito habito) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:-color-surface;-fx-background-radius:14;-fx-border-radius:14;" +
            "-fx-border-color:-color-border;-fx-border-width:1;-fx-cursor:hand;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:-color-surface2;-fx-background-radius:14;" +
            "-fx-border-radius:14;-fx-border-color:-color-border2;-fx-border-width:1;-fx-cursor:hand;"));
        card.setOnMouseExited(e  -> card.setStyle("-fx-background-color:-color-surface;-fx-background-radius:14;" +
            "-fx-border-radius:14;-fx-border-color:-color-border;-fx-border-width:1;-fx-cursor:hand;"));

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label emojiLbl = new Label(habito.getEmoji()); emojiLbl.setStyle("-fx-font-size:22px;");
        VBox nameBox = new VBox(2);
        Label name = new Label(habito.getNombre()); name.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:-color-text;");
        Label desc = new Label(habito.getCoste() + "€ " + habito.getDescripcion());
        desc.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        nameBox.getChildren().addAll(name, desc);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        header.getChildren().addAll(emojiLbl, nameBox);

        // Frecuencia actual
        HBox freqRow = new HBox(8);
        freqRow.setAlignment(Pos.CENTER_LEFT);
        Label freqLbl = new Label("Actual:"); freqLbl.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
        Spinner<Integer> spinner = new Spinner<>(0, 100, habito.getFrecuenciaActual());
        spinner.setEditable(true);
        spinner.setPrefWidth(80);
        Label unidadLbl = new Label("/" + habito.getUnidad()); unidadLbl.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        spinner.valueProperty().addListener((obs, oldV, newV) -> {
            habito.setFrecuenciaActual(newV);
            dao.actualizarFrecuenciasHabito(habito);
            actualizarResumenAhorroPotencial();
        });
        freqRow.getChildren().addAll(freqLbl, spinner, unidadLbl);

        // Objetivo
        HBox objRow = new HBox(8);
        objRow.setAlignment(Pos.CENTER_LEFT);
        Label objLbl = new Label("Objetivo:"); objLbl.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
        Spinner<Integer> spinnerObj = new Spinner<>(0, 100, habito.getFrecuenciaObj());
        spinnerObj.setEditable(true);
        spinnerObj.setPrefWidth(80);
        Label unidadObj = new Label("/" + habito.getUnidad()); unidadObj.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;");
        spinnerObj.valueProperty().addListener((obs, oldV, newV) -> {
            habito.setFrecuenciaObj(newV);
            dao.actualizarFrecuenciasHabito(habito);
            actualizarResumenAhorroPotencial();
        });
        objRow.getChildren().addAll(objLbl, spinnerObj, unidadObj);

        // Costes y ahorro mensual potencial
        Label costoActual = new Label("Coste/mes actual: " + fmt(habito.getGastoMensualActual()) + "€");
        costoActual.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text2;");

        double ahorroPotencial = habito.getAhorroMensualPotencial();
        Label ahorroLbl = new Label(ahorroPotencial > 0
            ? "💚 Ahorro posible: " + fmt(ahorroPotencial) + "€/mes"
            : "Sin margen de ahorro");
        ahorroLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + (ahorroPotencial > 0 ? "#1D9E75" : "-color-text3") + ";");

        Button del = new Button("🗑 Eliminar");
        del.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;-fx-cursor:hand;-fx-font-size:11px;");
        del.setOnAction(e -> { dao.eliminarHabito(habito.getId()); cargarHabitosYResumen(); });

        card.getChildren().addAll(header, new Separator(), freqRow, objRow, costoActual, ahorroLbl, del);
        return card;
    }

    @FXML void toggleForm() {
        boolean v = !formPanel.isVisible();
        formPanel.setVisible(v); formPanel.setManaged(v);
    }

    @FXML void guardarHabito() {
        String nombre = txtNombre.getText().trim();
        String costeStr = txtCoste.getText().trim().replace(",",".");
        if (nombre.isEmpty() || costeStr.isEmpty()) return;
        try {
            Habito h = new Habito();
            h.setUsuarioId(Session.getInstance().getUsuarioActual().getId());
            h.setEmoji(txtEmoji.getText().isBlank() ? "💡" : txtEmoji.getText().trim());
            h.setNombre(nombre);
            h.setCoste(Double.parseDouble(costeStr));
            h.setFrecuenciaActual(txtFreqActual.getText().isBlank() ? 0 : Integer.parseInt(txtFreqActual.getText().trim()));
            h.setFrecuenciaObj(txtFreqObj.getText().isBlank() ? 0 : Integer.parseInt(txtFreqObj.getText().trim()));
            h.setUnidad(cmbUnidad.getValue());
            h.setDescripcion("por vez");
            dao.registrarHabito(h);
            toggleForm();
            txtEmoji.clear(); txtNombre.clear(); txtCoste.clear(); txtFreqActual.clear(); txtFreqObj.clear();
            cargarHabitosYResumen();
        } catch (NumberFormatException ex) { /* ignore */ }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
