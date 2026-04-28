package com.finanzapp.controller;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Objetivo;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador del panel principal (dashboard) de Fox Wallet.
 * <p>
 * Muestra el resumen financiero del mes en curso: saldo, ingresos y gastos totales,
 * tasa de ahorro, gráfico donut por categorías, gráfico de barras de los últimos
 * 6 meses, las 5 transacciones más recientes y los objetivos de ahorro pendientes.
 */
public class DashboardController implements Initializable, MainController.ChildController {

    @FXML private Label lblBienvenida, lblFecha;
    @FXML private Label statSaldo, statIngresos, statGastos, statAhorro;
    @FXML private Label subIngresos, subGastos;
    @FXML private Canvas donutCanvas, barCanvas;
    @FXML private VBox   donutLegend, txList;
    @FXML private HBox   objetivosBox;

    private final MovimientoDAO movDAO = new MovimientoDAO();
    private final ObjetivoDAO   objDAO = new ObjetivoDAO();
    private MainController main;

    @Override
    public void setMain(MainController main) { this.main = main; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var u = Session.getInstance().getUsuarioActual();
        lblBienvenida.setText("Hola, " + u.getNombre().split(" ")[0] + " 👋");
        lblFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es"))));

        LocalDate now = LocalDate.now();
        List<Movimiento> movimientosMesActual = movDAO.obtenerMovimientosPorMes(u.getId(), now.getYear(), now.getMonthValue());

        double ingresos = movimientosMesActual.stream().filter(Movimiento::isIngreso).mapToDouble(Movimiento::getCantidad).sum();
        double gastos   = movimientosMesActual.stream().filter(m -> !m.isIngreso()).mapToDouble(Movimiento::getCantidad).sum();
        double saldo    = ingresos - gastos;
        double tasa     = ingresos > 0 ? (saldo / ingresos) * 100 : 0;

        statSaldo.setText(fmt(saldo) + "€");
        statSaldo.getStyleClass().removeAll("stat-up","stat-down");
        statSaldo.getStyleClass().add(saldo >= 0 ? "stat-up" : "stat-down");
        statIngresos.setText("+" + fmt(ingresos) + "€");
        subIngresos.setText("este mes");
        statGastos.setText("-" + fmt(gastos) + "€");
        subGastos.setText("este mes");
        statAhorro.setText(String.format("%.0f%%", tasa));
        statAhorro.getStyleClass().add(tasa >= 20 ? "stat-up" : tasa >= 0 ? "stat-amber" : "stat-down");

        dibujarGraficoDonutGastos(movimientosMesActual);
        dibujarGraficoBarrasMensual(u.getId(), now);
        mostrarTransaccionesRecientes(movDAO.obtenerUltimosMovimientos(u.getId(), 5));
        mostrarTarjetasObjetivosPendientes(objDAO.obtenerObjetivosDeUsuario(u.getId()));
    }

    // ── Gráfico donut: distribución de gastos por categoría ──────────────
    private void dibujarGraficoDonutGastos(List<Movimiento> movs) {
        Map<String, Double> cats = movs.stream()
            .filter(m -> !m.isIngreso())
            .collect(Collectors.groupingBy(
                m -> m.getCategoriaNombre() != null ? m.getCategoriaNombre() : "Otro",
                Collectors.summingDouble(Movimiento::getCantidad)));

        double total = cats.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return;

        String[] colors = {"#1D9E75","#BA7517","#185FA5","#8B5CF6","#D85A30","#6B6A65"};
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 120, 120);

        double startAngle = -90;
        int ci = 0;
        List<Map.Entry<String, Double>> sorted = cats.entrySet().stream()
            .sorted(Map.Entry.<String,Double>comparingByValue().reversed()).toList();

        for (Map.Entry<String,Double> e : sorted) {
            double arc = (e.getValue() / total) * 360;
            gc.setFill(Color.web(colors[ci % colors.length]));
            gc.fillArc(5, 5, 110, 110, startAngle, -arc, javafx.scene.shape.ArcType.ROUND);
            startAngle -= arc;
            ci++;
        }
        // Inner hole
        gc.setFill(Color.web("#F7F6F3"));
        gc.fillOval(30, 30, 60, 60);
        // Center text
        gc.setFill(Color.web("#1A1916"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText(fmt(total) + "€", 35, 57);
        gc.setFont(Font.font("System", 9));
        gc.setFill(Color.web("#A09F9B"));
        gc.fillText("total", 48, 68);

        // Legend
        donutLegend.getChildren().clear();
        ci = 0;
        for (Map.Entry<String,Double> e : sorted) {
            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill:" + colors[ci % colors.length] + ";-fx-font-size:10px;");
            Label name = new Label(e.getKey());
            name.setStyle("-fx-font-size:12px;");
            HBox.setHgrow(name, Priority.ALWAYS);
            Label val = new Label(fmt(e.getValue()) + "€");
            val.setStyle("-fx-font-size:12px;-fx-font-family:monospace;");
            row.getChildren().addAll(dot, name, val);
            donutLegend.getChildren().add(row);
            ci++;
        }
    }

    // ── Gráfico de barras: ingresos vs gastos últimos 6 meses ────────────
    private void dibujarGraficoBarrasMensual(int uid, LocalDate now) {
        GraphicsContext gc = barCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 340, 120);

        String[] labels = new String[6];
        double[] ing = new double[6];
        double[] gast = new double[6];
        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

        for (int i = 5; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            List<Movimiento> movs = movDAO.obtenerMovimientosPorMes(uid, d.getYear(), d.getMonthValue());
            int idx = 5 - i;
            labels[idx] = meses[d.getMonthValue() - 1];
            ing[idx]  = movs.stream().filter(Movimiento::isIngreso).mapToDouble(Movimiento::getCantidad).sum();
            gast[idx] = movs.stream().filter(m -> !m.isIngreso()).mapToDouble(Movimiento::getCantidad).sum();
        }

        double maxVal = 0;
        for (int i = 0; i < 6; i++) maxVal = Math.max(maxVal, Math.max(ing[i], gast[i]));
        if (maxVal == 0) maxVal = 1000;

        double barW = 22, gap = 12, chartH = 90, startX = 20;
        for (int i = 0; i < 6; i++) {
            double x = startX + i * (barW * 2 + gap + 4);
            double hI = (ing[i]  / maxVal) * chartH;
            double hG = (gast[i] / maxVal) * chartH;

            gc.setFill(Color.web("#1D9E75", 0.85));
            gc.fillRoundRect(x, chartH - hI, barW, hI, 4, 4);

            gc.setFill(Color.web("#D85A30", 0.65));
            gc.fillRoundRect(x + barW + 2, chartH - hG, barW, hG, 4, 4);

            gc.setFill(Color.web("#A09F9B"));
            gc.setFont(Font.font("System", 9));
            gc.fillText(labels[i], x + 3, chartH + 14);
        }
    }

    // ── Lista de transacciones recientes en el panel ─────────────────────
    private void mostrarTransaccionesRecientes(List<Movimiento> movs) {
        txList.getChildren().clear();
        for (Movimiento m : movs) {
            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setStyle("-fx-border-color: transparent transparent #F0EEE9 transparent;-fx-border-width:0 0 1 0;");

            Label icon = new Label(m.getCategoriaEmoji() != null ? m.getCategoriaEmoji() : "💳");
            icon.setStyle("-fx-font-size:18px;-fx-min-width:36px;-fx-min-height:36px;" +
                "-fx-background-radius:10;-fx-alignment:center;" +
                (m.isIngreso() ? "-fx-background-color:#E1F5EE;" : "-fx-background-color:#F0EEE9;"));

            VBox info = new VBox(2);
            Label name = new Label(m.getNombre()); name.setStyle("-fx-font-weight:500;-fx-font-size:13px;");
            Label cat  = new Label(m.getCategoriaDisplay()); cat.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");
            info.getChildren().addAll(name, cat);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label fecha = new Label(m.getFecha() != null ? m.getFecha().format(DateTimeFormatter.ofPattern("d MMM", new Locale("es"))) : "");
            fecha.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");

            Label amt = new Label((m.isIngreso() ? "+" : "-") + fmt(m.getCantidad()) + "€");
            amt.setStyle("-fx-font-family:monospace;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + (m.isIngreso() ? "#1D9E75" : "#D85A30") + ";");

            row.getChildren().addAll(icon, info, fecha, amt);
            txList.getChildren().add(row);
        }
        if (movs.isEmpty()) {
            Label empty = new Label("Sin movimientos este mes");
            empty.setStyle("-fx-text-fill:#A09F9B;-fx-padding:20px;");
            txList.getChildren().add(empty);
        }
    }

    // ── Tarjetas de objetivos de ahorro pendientes ───────────────────────
    private void mostrarTarjetasObjetivosPendientes(List<Objetivo> objs) {
        objetivosBox.getChildren().clear();
        for (Objetivo o : objs.stream().filter(ob -> !ob.isCompletado()).limit(3).toList()) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:14;-fx-border-radius:14;" +
                "-fx-border-color:#F0EEE9;-fx-border-width:1;-fx-padding:14;");
            HBox.setHgrow(card, Priority.ALWAYS);

            Label emoji = new Label(o.getEmoji() != null ? o.getEmoji() : "🎯");
            emoji.setStyle("-fx-font-size:22px;");
            Label name = new Label(o.getNombre()); name.setStyle("-fx-font-weight:500;-fx-font-size:13px;");

            ProgressBar pb = new ProgressBar(o.getPorcentaje() / 100.0);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setStyle("-fx-accent:#1D9E75;-fx-pref-height:8px;");

            Label pct = new Label(String.format("%.0f%% · %s€ de %s€",
                o.getPorcentaje(), fmt(o.getActual()), fmt(o.getObjetivo())));
            pct.setStyle("-fx-font-size:11px;-fx-text-fill:#A09F9B;");

            card.getChildren().addAll(emoji, name, pb, pct);
            objetivosBox.getChildren().add(card);
        }
    }

    @FXML void irGastos()    { if (main != null) main.showGastos(); }
    @FXML void irObjetivos() { if (main != null) main.showObjetivos(); }
    @FXML void irHistorial() { if (main != null) main.showHistorial(); }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",", "."); }
}
