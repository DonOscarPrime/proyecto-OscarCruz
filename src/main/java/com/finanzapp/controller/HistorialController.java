package com.finanzapp.controller;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HistorialController implements Initializable {

    @FXML private ComboBox<String>  cmbMes, cmbTipo;
    @FXML private ComboBox<Integer> cmbAnio;
    @FXML private TextField         txtBuscar;
    @FXML private Label statIng, statGast, statBal, statCount;
    @FXML private VBox txList;

    private final MovimientoDAO dao = new MovimientoDAO();
    private List<Movimiento> todos;

    private static final String[] MESES = {
        "Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate now = LocalDate.now();

        List<String> mesesList = new ArrayList<>(Arrays.asList(MESES));
        mesesList.add(0, "Todos");
        cmbMes.setItems(FXCollections.observableArrayList(mesesList));
        cmbMes.setValue(MESES[now.getMonthValue() - 1]);

        List<Integer> anios = new ArrayList<>();
        for (int y = now.getYear(); y >= now.getYear() - 3; y--) anios.add(y);
        cmbAnio.setItems(FXCollections.observableArrayList(anios));
        cmbAnio.setValue(now.getYear());

        cmbTipo.setItems(FXCollections.observableArrayList("Todos","Gastos","Ingresos"));
        cmbTipo.setValue("Todos");

        cmbMes.setOnAction(e -> cargar());
        cmbAnio.setOnAction(e -> cargar());
        cmbTipo.setOnAction(e -> aplicarFiltro());
        txtBuscar.textProperty().addListener((obs, o, n) -> aplicarFiltro());

        cargar();
    }

    private void cargar() {
        int uid  = Session.getInstance().getUsuarioActual().getId();
        int anio = cmbAnio.getValue();
        String mes = cmbMes.getValue();

        if ("Todos".equals(mes)) {
            todos = dao.listarPorUsuario(uid);
        } else {
            int mesIdx = Arrays.asList(MESES).indexOf(mes) + 1;
            todos = dao.listarPorMes(uid, anio, mesIdx);
        }
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String tipo   = cmbTipo.getValue();
        String buscar = txtBuscar.getText().toLowerCase();

        List<Movimiento> filtrados = todos.stream()
            .filter(m -> {
                if ("Gastos".equals(tipo)   && m.isIngreso())  return false;
                if ("Ingresos".equals(tipo) && !m.isIngreso()) return false;
                if (!buscar.isEmpty() && !m.getNombre().toLowerCase().contains(buscar) &&
                    (m.getCategoriaNombre() == null || !m.getCategoriaNombre().toLowerCase().contains(buscar))) return false;
                return true;
            }).collect(Collectors.toList());

        actualizarStats(filtrados);
        renderList(filtrados);
    }

    private void actualizarStats(List<Movimiento> movs) {
        double ing  = movs.stream().filter(Movimiento::isIngreso).mapToDouble(Movimiento::getCantidad).sum();
        double gast = movs.stream().filter(m -> !m.isIngreso()).mapToDouble(Movimiento::getCantidad).sum();
        double bal  = ing - gast;

        statIng.setText("+" + fmt(ing) + "€");
        statGast.setText("-" + fmt(gast) + "€");
        statBal.setText((bal >= 0 ? "+" : "") + fmt(bal) + "€");
        statBal.getStyleClass().removeAll("stat-up","stat-down");
        statBal.getStyleClass().add(bal >= 0 ? "stat-up" : "stat-down");
        statCount.setText(String.valueOf(movs.size()));
    }

    private void renderList(List<Movimiento> movs) {
        txList.getChildren().clear();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es"));

        for (Movimiento m : movs) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setStyle("-fx-border-color:transparent transparent #F0EEE9 transparent;-fx-border-width:0 0 1 0;");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#FAFAF8;-fx-border-color:transparent transparent #F0EEE9 transparent;-fx-border-width:0 0 1 0;"));
            row.setOnMouseExited(e  -> row.setStyle("-fx-background-color:transparent;-fx-border-color:transparent transparent #F0EEE9 transparent;-fx-border-width:0 0 1 0;"));

            Label icon = new Label(m.getCategoriaEmoji() != null ? m.getCategoriaEmoji() : "💳");
            icon.setStyle("-fx-font-size:18px;-fx-min-width:36px;-fx-min-height:36px;" +
                "-fx-background-radius:10;-fx-alignment:center;" +
                (m.isIngreso() ? "-fx-background-color:#E1F5EE;" : "-fx-background-color:#F0EEE9;"));

            VBox info = new VBox(2);
            Label name = new Label(m.getNombre()); name.setStyle("-fx-font-weight:500;-fx-font-size:13px;");
            Label cat  = new Label(m.getCategoriaDisplay()); cat.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");
            if (m.getNotas() != null && !m.getNotas().isBlank()) {
                Label nota = new Label("📝 " + m.getNotas()); nota.setStyle("-fx-font-size:10px;-fx-text-fill:#A09F9B;");
                info.getChildren().addAll(name, cat, nota);
            } else {
                info.getChildren().addAll(name, cat);
            }
            HBox.setHgrow(info, Priority.ALWAYS);

            Label fecha = new Label(m.getFecha() != null ? m.getFecha().format(dtf) : "");
            fecha.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;min-width:90px;");

            Label tipoBadge = new Label(m.isIngreso() ? "Ingreso" : "Gasto");
            tipoBadge.setStyle("-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:6;" +
                (m.isIngreso() ? "-fx-background-color:#E1F5EE;-fx-text-fill:#0F6E56;" :
                                 "-fx-background-color:#FAECE7;-fx-text-fill:#D85A30;"));

            Label amt = new Label((m.isIngreso() ? "+" : "-") + fmt(m.getCantidad()) + "€");
            amt.setStyle("-fx-font-family:monospace;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + (m.isIngreso() ? "#1D9E75" : "#D85A30") + ";min-width:80px;-fx-alignment:CENTER_RIGHT;");

            row.getChildren().addAll(icon, info, fecha, tipoBadge, amt);
            txList.getChildren().add(row);
        }
        if (movs.isEmpty()) {
            Label empty = new Label("No hay movimientos para el período seleccionado");
            empty.setStyle("-fx-text-fill:#A09F9B;-fx-padding:32px;-fx-font-size:13px;");
            txList.getChildren().add(empty);
        }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
