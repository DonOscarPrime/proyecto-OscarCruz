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

public class ObjetivosController implements Initializable {

    @FXML private VBox     formPanel;
    @FXML private FlowPane objetivosGrid;
    @FXML private TextField txtNombre, txtEmoji, txtObjetivo, txtActual;
    @FXML private DatePicker dateLimite;
    @FXML private Label lblError;

    private final ObjetivoDAO dao = new ObjetivoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) { cargar(); }

    private void cargar() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        List<Objetivo> objs = dao.listarPorUsuario(uid);
        objetivosGrid.getChildren().clear();
        for (Objetivo o : objs) objetivosGrid.getChildren().add(buildCard(o));
    }

    private VBox buildCard(Objetivo o) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPadding(new Insets(16));
        String bg = o.isCompletado() ? "#E1F5EE" : "#FFFFFF";
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;" +
            "-fx-border-radius:14;-fx-border-color:#F0EEE9;-fx-border-width:1;");

        // Header
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label emoji = new Label(o.getEmoji() != null ? o.getEmoji() : "🎯");
        emoji.setStyle("-fx-font-size:24px;");
        Label name = new Label(o.getNombre());
        name.setStyle("-fx-font-size:14px;-fx-font-weight:600;");
        HBox.setHgrow(name, Priority.ALWAYS);
        if (o.isCompletado()) {
            Label badge = new Label("✓ Completado");
            badge.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:10px;");
            header.getChildren().addAll(emoji, name, badge);
        } else {
            header.getChildren().addAll(emoji, name);
        }

        // Progress bar
        double pct = o.getPorcentaje();
        ProgressBar pb = new ProgressBar(pct / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent:" + (o.isCompletado() ? "#1D9E75" : "#185FA5") + ";-fx-pref-height:10px;");

        Label pctLbl = new Label(String.format("%.0f%% · %.0f€ de %.0f€", pct, o.getActual(), o.getObjetivo()));
        pctLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#6B6A65;");

        Label restante = new Label("Faltan: " + fmt(o.getRestante()) + "€");
        restante.setStyle("-fx-font-size:12px;-fx-text-fill:#A09F9B;");

        // Aporte rápido
        HBox aporteRow = new HBox(6);
        aporteRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField aporteTxt = new TextField();
        aporteTxt.setPromptText("Añadir €");
        aporteTxt.setPrefWidth(90);
        aporteTxt.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#E0DDDA;-fx-padding:4 8;-fx-font-size:12px;");

        Button aporteBtn = new Button("Aportar");
        aporteBtn.setStyle("-fx-background-color:#1D9E75;-fx-text-fill:white;-fx-background-radius:6;" +
            "-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
        aporteBtn.setOnAction(e -> {
            try {
                double aporte = Double.parseDouble(aporteTxt.getText().replace(",","."));
                double nuevo  = Math.min(o.getObjetivo(), o.getActual() + aporte);
                dao.actualizarAporte(o.getId(), nuevo);
                cargar();
            } catch (NumberFormatException ex) { /* ignore */ }
        });

        Button delBtn = new Button("🗑");
        delBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#D85A30;-fx-cursor:hand;-fx-font-size:12px;");
        delBtn.setOnAction(e -> { dao.eliminar(o.getId()); cargar(); });

        aporteRow.getChildren().addAll(aporteTxt, aporteBtn, delBtn);

        card.getChildren().addAll(header, pb, pctLbl, restante, aporteRow);
        if (o.getFechaLimite() != null) {
            Label limite = new Label("Fecha límite: " + o.getFechaLimite());
            limite.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");
            card.getChildren().add(limite);
        }
        return card;
    }

    @FXML void toggleForm() {
        boolean visible = !formPanel.isVisible();
        formPanel.setVisible(visible);
        formPanel.setManaged(visible);
        if (!visible) { txtNombre.clear(); txtEmoji.clear(); txtObjetivo.clear(); txtActual.clear(); lblError.setText(""); }
    }

    @FXML void guardar() {
        lblError.setText("");
        String nombre = txtNombre.getText().trim();
        String objStr = txtObjetivo.getText().trim().replace(",",".");
        if (nombre.isEmpty() || objStr.isEmpty()) { lblError.setText("Rellena los campos obligatorios."); return; }
        double obj;
        try { obj = Double.parseDouble(objStr); } catch (NumberFormatException e) { lblError.setText("Importe no válido."); return; }

        double actual = 0;
        if (!txtActual.getText().isBlank()) {
            try { actual = Double.parseDouble(txtActual.getText().replace(",",".")); } catch (NumberFormatException ignored) {}
        }

        Objetivo o = new Objetivo();
        o.setUsuarioId(Session.getInstance().getUsuarioActual().getId());
        o.setNombre(nombre);
        o.setObjetivo(obj);
        o.setActual(actual);
        o.setEmoji(txtEmoji.getText().isBlank() ? "🎯" : txtEmoji.getText().trim());
        o.setFechaLimite(dateLimite.getValue());

        if (dao.insertar(o)) {
            toggleForm();
            cargar();
        } else {
            lblError.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
