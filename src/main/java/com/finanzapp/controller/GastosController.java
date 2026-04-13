package com.finanzapp.controller;

import com.finanzapp.dao.CategoriaDAO;
import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.model.Categoria;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GastosController implements Initializable {

    @FXML private Label statIngresos, statGastos, statBalance, subBalance, statTasa;
    @FXML private ComboBox<String>    cmbTipo, cmbFiltroTipo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtCantidad, txtNombre, txtNotas, txtBuscar;
    @FXML private DatePicker dateFecha;
    @FXML private Label lblMsg, lblError;
    @FXML private VBox txList;

    private final MovimientoDAO movDAO = new MovimientoDAO();
    private final CategoriaDAO  catDAO = new CategoriaDAO();
    private List<Movimiento> todos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTipo.setItems(FXCollections.observableArrayList("Gasto", "Ingreso"));
        cmbTipo.setValue("Gasto");
        cmbTipo.setOnAction(e -> actualizarCategorias());

        cmbFiltroTipo.setItems(FXCollections.observableArrayList("Todos", "Gastos", "Ingresos"));
        cmbFiltroTipo.setValue("Todos");
        cmbFiltroTipo.setOnAction(e -> aplicarFiltro());

        txtBuscar.textProperty().addListener((obs, o, n) -> aplicarFiltro());

        dateFecha.setValue(LocalDate.now());
        actualizarCategorias();
        cargarMovimientos();
    }

    private void actualizarCategorias() {
        String tipo = "Ingreso".equals(cmbTipo.getValue()) ? "ingreso" : "gasto";
        List<Categoria> cats = catDAO.listarPorTipo(tipo);
        cmbCategoria.setItems(FXCollections.observableArrayList(cats));
        if (!cats.isEmpty()) cmbCategoria.setValue(cats.get(0));
    }

    private void cargarMovimientos() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        todos = movDAO.listarPorUsuario(uid);
        actualizarStats();
        aplicarFiltro();
    }

    private void actualizarStats() {
        double ing  = todos.stream().filter(Movimiento::isIngreso).mapToDouble(Movimiento::getCantidad).sum();
        double gast = todos.stream().filter(m -> !m.isIngreso()).mapToDouble(Movimiento::getCantidad).sum();
        double bal  = ing - gast;
        double tasa = ing > 0 ? (bal / ing) * 100 : 0;

        statIngresos.setText("+" + fmt(ing) + "€");
        statGastos.setText("-" + fmt(gast) + "€");
        statBalance.setText((bal >= 0 ? "+" : "") + fmt(bal) + "€");
        statBalance.getStyleClass().removeAll("stat-up","stat-down");
        statBalance.getStyleClass().add(bal >= 0 ? "stat-up" : "stat-down");
        subBalance.setText(bal >= 0 ? "margen positivo" : "⚠ gastos > ingresos");
        statTasa.setText(String.format("%.0f%%", tasa));
        statTasa.getStyleClass().removeAll("stat-up","stat-amber","stat-down");
        statTasa.getStyleClass().add(tasa >= 20 ? "stat-up" : tasa >= 0 ? "stat-amber" : "stat-down");
    }

    private void aplicarFiltro() {
        String filtro  = cmbFiltroTipo.getValue();
        String buscar  = txtBuscar.getText().toLowerCase();
        List<Movimiento> filtrados = todos.stream()
            .filter(m -> {
                if ("Gastos".equals(filtro)   && m.isIngreso())  return false;
                if ("Ingresos".equals(filtro) && !m.isIngreso()) return false;
                if (!buscar.isEmpty() && !m.getNombre().toLowerCase().contains(buscar)) return false;
                return true;
            }).collect(Collectors.toList());
        renderList(filtrados);
    }

    private void renderList(List<Movimiento> movs) {
        txList.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", new Locale("es"));
        for (Movimiento m : movs) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setStyle("-fx-border-color:transparent transparent #F0EEE9 transparent;-fx-border-width:0 0 1 0;");

            Label icon = new Label(m.getCategoriaEmoji() != null ? m.getCategoriaEmoji() : "💳");
            icon.setStyle("-fx-font-size:18px;-fx-min-width:36px;-fx-min-height:36px;" +
                "-fx-background-radius:10;-fx-alignment:center;" +
                (m.isIngreso() ? "-fx-background-color:#E1F5EE;" : "-fx-background-color:#F0EEE9;"));

            VBox info = new VBox(2);
            Label name = new Label(m.getNombre()); name.setStyle("-fx-font-weight:500;-fx-font-size:13px;");
            Label cat  = new Label(m.getCategoriaDisplay()); cat.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");
            info.getChildren().addAll(name, cat);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label fecha = new Label(m.getFecha() != null ? m.getFecha().format(fmt) : "");
            fecha.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;min-width:50px;");

            Label amt = new Label((m.isIngreso() ? "+" : "-") + this.fmt(m.getCantidad()) + "€");
            amt.setStyle("-fx-font-family:monospace;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + (m.isIngreso() ? "#1D9E75" : "#D85A30") + ";");

            Button del = new Button("✕");
            del.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;-fx-cursor:hand;-fx-font-size:11px;");
            del.setOnAction(e -> {
                movDAO.eliminar(m.getId());
                cargarMovimientos();
            });

            row.getChildren().addAll(icon, info, fecha, amt, del);
            txList.getChildren().add(row);
        }
        if (movs.isEmpty()) {
            Label empty = new Label("Sin movimientos");
            empty.setStyle("-fx-text-fill:#A09F9B;-fx-padding:24px;");
            txList.getChildren().add(empty);
        }
    }

    @FXML private void guardar() {
        lblError.setText(""); lblMsg.setText("");
        String cantStr = txtCantidad.getText().trim().replace(",",".");
        if (cantStr.isEmpty()) { lblError.setText("Introduce una cantidad."); return; }
        double cant;
        try { cant = Double.parseDouble(cantStr); } catch (NumberFormatException e) {
            lblError.setText("Cantidad no válida."); return; }
        if (cant <= 0) { lblError.setText("La cantidad debe ser mayor que 0."); return; }

        String desc = txtNombre.getText().trim();
        if (desc.isEmpty()) { lblError.setText("Introduce una descripción."); return; }

        Movimiento m = new Movimiento();
        m.setUsuarioId(Session.getInstance().getUsuarioActual().getId());
        m.setTipo("Ingreso".equals(cmbTipo.getValue()) ? "ingreso" : "gasto");
        m.setCantidad(cant);
        m.setNombre(desc);
        m.setNotas(txtNotas.getText());
        m.setFecha(dateFecha.getValue() != null ? dateFecha.getValue() : LocalDate.now());
        if (cmbCategoria.getValue() != null) m.setCategoriaId(cmbCategoria.getValue().getId());

        if (movDAO.insertar(m)) {
            lblMsg.setText("✓ Movimiento guardado correctamente.");
            txtCantidad.clear(); txtNombre.clear(); txtNotas.clear();
            dateFecha.setValue(LocalDate.now());
            cargarMovimientos();
        } else {
            lblError.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
