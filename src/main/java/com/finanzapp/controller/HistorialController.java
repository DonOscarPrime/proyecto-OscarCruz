package com.finanzapp.controller;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.renderer.MovimientoRenderer;
import com.finanzapp.service.MovimientoService;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del historial de transacciones.
 * Se encarga de filtros, cálculos y visualizacion de movimientos
 */
public class HistorialController implements Initializable {

    @FXML private ComboBox<String>  seleccionarMes;
    @FXML private ComboBox<String>  seleccionarTipo;
    @FXML private ComboBox<Integer> seleccionarAnio;
    @FXML private TextField         textBusc;
    @FXML private Label estadisticaIng;
    @FXML private Label estadisticaGast;
    @FXML private Label estadisticaBalan;
    @FXML private Label estadisticaCount;
    @FXML private VBox txList;

    private final MovimientoDAO      moverDAO      = new MovimientoDAO();
    private final MovimientoService  moverService  = new MovimientoService(moverDAO);
    private final MovimientoRenderer moverRenderer = new MovimientoRenderer();

    private List<Movimiento> todos;

    private static final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate hoy = LocalDate.now();

        List<String> mesesList = new ArrayList<>(Arrays.asList(MESES));
        mesesList.add(0, "Todos");
        javafx.collections.ObservableList<String> itemsMeses = FXCollections.observableArrayList(mesesList);
        seleccionarMes.setItems(itemsMeses);
        int mesActual = hoy.getMonthValue();
        int indiceMesActual = mesActual - 1;
        String nombreMesActual = MESES[indiceMesActual];
        seleccionarMes.setValue(nombreMesActual);

        List<Integer> anios = new ArrayList<>();
        int anioActual = hoy.getYear();
        int anioMinimo = anioActual - 3;
        for (int y = anioActual; y >= anioMinimo; y--) {
            anios.add(y);
        }
        javafx.collections.ObservableList<Integer> itemsAnios = FXCollections.observableArrayList(anios);
        seleccionarAnio.setItems(itemsAnios);
        seleccionarAnio.setValue(anioActual);

        javafx.collections.ObservableList<String> itemsTipo = FXCollections.observableArrayList("Todos", "Gastos", "Ingresos");
        seleccionarTipo.setItems(itemsTipo);
        seleccionarTipo.setValue("Todos");

        seleccionarMes.setOnAction(e -> cargarMovimientos());
        seleccionarAnio.setOnAction(e -> cargarMovimientos());
        seleccionarTipo.setOnAction(e -> filtrarYRenderizar());
        textBusc.textProperty().addListener((obs, o, n) -> filtrarYRenderizar());

        cargarMovimientos();
    }

    private void cargarMovimientos() {
        Session sesionActual = Session.getInstance();
        com.finanzapp.model.Usuario usuarioEnSesion = sesionActual.getUsuarioActual();
        int uid = usuarioEnSesion.getId();
        int anio = seleccionarAnio.getValue();
        String mes = seleccionarMes.getValue();

        if ("Todos".equals(mes)) {
            todos = moverDAO.obtenerMovimientosDeUsuario(uid);
        } else {
            List<String> listaMeses = Arrays.asList(MESES);
            int posicionMes = listaMeses.indexOf(mes);
            int desplazamientoBase1 = 1;
            int mesIdx = posicionMes + desplazamientoBase1;
            todos = moverService.obtenerPorMes(uid, anio, mesIdx);
        }

        filtrarYRenderizar();
    }

    private void filtrarYRenderizar() {
        String filtroTipo    = seleccionarTipo.getValue();
        String movi = textBusc.getText();

        List<Movimiento> filtrados = moverService.filtrar(todos, filtroTipo, movi);
        actualizarEstadisticas(filtrados);
        renderizarLista(filtrados);
    }

    private void actualizarEstadisticas(List<Movimiento> movs) {
        double ingresos = moverService.calcularTotalIngresos(movs);
        double gastos   = moverService.calcularTotalGastos(movs);
        double signoBalance  = moverService.calcularBalance(ingresos, gastos);

        String ingresosFormateados = Formateador.moneda(ingresos);
        String textoIngresos = "+" + ingresosFormateados + "€";
        estadisticaIng.setText(textoIngresos);
        String gastosFormateados = Formateador.moneda(gastos);
        String textoGastos = "-" + gastosFormateados + "€";
        estadisticaGast.setText(textoGastos);

        boolean balancePositivo = signoBalance >= 0;

        String prefixBalance;
        if (balancePositivo) {
            prefixBalance = "+";
        } else {
            prefixBalance = "";
        }
        String balanceFormateado = Formateador.moneda(signoBalance);
        String textoBalance = prefixBalance + balanceFormateado + "€";
        estadisticaBalan.setText(textoBalance);

        estadisticaBalan.getStyleClass().removeAll("stat-up", "stat-down");
        if (balancePositivo) {
            estadisticaBalan.getStyleClass().add("stat-up");
        } else {
            estadisticaBalan.getStyleClass().add("stat-down");
        }

        int cantidadMovimientos = movs.size();
        String textoContadorMovimientos = String.valueOf(cantidadMovimientos);
        estadisticaCount.setText(textoContadorMovimientos);
    }

    private void renderizarLista(List<Movimiento> movs) {
        txList.getChildren().clear();

        for (Movimiento m : movs) {
            javafx.scene.Node filaCompleta = moverRenderer.crearFilaCompleta(m);
            txList.getChildren().add(filaCompleta);
        }

        if (movs.isEmpty()) {
            javafx.scene.Node mensajeVacio = moverRenderer.crearMensajeVacio("No hay movimientos para el período seleccionado");
            txList.getChildren().add(mensajeVacio);
        }
    }
}
