package com.finanzapp.controller;

import com.finanzapp.dao.CategoriaDAO;
import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.NotificacionDAO;
import com.finanzapp.model.Categoria;
import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Notificacion;
import com.finanzapp.model.Usuario;
import com.finanzapp.renderer.MovimientoRenderer;
import com.finanzapp.service.MovimientoService;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import com.finanzapp.validator.MovimientoValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del panel de movimientos de Fox Wallet.
 Gestiona el formulario y coordina validación, lógica y visualización.
 */
public class GastosController implements Initializable {

    @FXML private Label estadisticaIngreso;
    @FXML private Label estadisticaGastos;
    @FXML private Label estadisticaBalance;
    @FXML private Label subBalance;
    @FXML private Label estadisticaTasa;
    @FXML private ComboBox<String>    cmbTipo;
    @FXML private ComboBox<String>    cmbFiltroTipo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtNombre;
    @FXML private TextField txtNotas;
    @FXML private TextField TextoMensage;
    @FXML private DatePicker dateFecha;
    @FXML private Label lblMsg;
    @FXML private Label TextoError;
    @FXML private VBox txList;

    private final MovimientoDAO       movDAO              = new MovimientoDAO();
    private final CategoriaDAO        catDAO              = new CategoriaDAO();
    private final NotificacionDAO     notifDAO            = new NotificacionDAO();
    private final MovimientoService   movimeentoService   = new MovimientoService(movDAO);
    private final MovimientoRenderer  movimientoREnderes  = new MovimientoRenderer();
    private final MovimientoValidator movimientoValidador = new MovimientoValidator();

    private List<Movimiento> todos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTipo.setItems(FXCollections.observableArrayList("Gasto", "Ingreso"));
        cmbTipo.setValue("Gasto");
        cmbTipo.setOnAction(e -> actualizarCategoriasPorTipo());

        cmbFiltroTipo.setItems(FXCollections.observableArrayList("Todos", "Gastos", "Ingresos"));
        cmbFiltroTipo.setValue("Todos");
        cmbFiltroTipo.setOnAction(e -> filtrarYRenderizar());

        TextoMensage.textProperty().addListener((obs, o, n) -> filtrarYRenderizar());

        dateFecha.setValue(LocalDate.now());
        actualizarCategoriasPorTipo();
        cargarMovimiento();
    }

    private void cargarMovimiento() {
        Session sesionActual = Session.getInstance();
        Usuario usuarioEnSesion = sesionActual.getUsuarioActual();
        int uid = usuarioEnSesion.getId();
        todos = movimeentoService.obtenerDeUsuario(uid);
        actualizarEstadisticas();
        filtrarYRenderizar();
    }

    private void actualizarCategoriasPorTipo() {
        String tipoSeleccionado = cmbTipo.getValue();
        String tipoParaDAO;
        if ("Ingreso".equals(tipoSeleccionado)) {
            tipoParaDAO = "ingreso";
        } else {
            tipoParaDAO = "gasto";
        }

        List<Categoria> cats = catDAO.obtenerCategoriasPorTipo(tipoParaDAO);
        cmbCategoria.setItems(FXCollections.observableArrayList(cats));
        if (!cats.isEmpty()) {
            cmbCategoria.setValue(cats.get(0));
        }
    }

    //Estadísticas

    private void actualizarEstadisticas() {
        double ingresos = movimeentoService.calcularTotalIngresos(todos);
        double gastos   = movimeentoService.calcularTotalGastos(todos);
        double balance  = movimeentoService.calcularBalance(ingresos, gastos);
        double tasa     = movimeentoService.calcularTasaAhorro(ingresos, balance);

        estadisticaIngreso.setText(movimeentoService.etiquetaImporte(ingresos, true));
        String gastosFormateados = Formateador.moneda(gastos);
        String textoGastos = "-" + gastosFormateados + "€";
        estadisticaGastos.setText(textoGastos);

        String prefixBalance;
        if (balance >= 0) {
            prefixBalance = "+";
        } else {
            prefixBalance = "";
        }
        String balanceFormateado = Formateador.moneda(balance);
        String textoBalance = prefixBalance + balanceFormateado + "€";
        estadisticaBalance.setText(textoBalance);

        estadisticaBalance.getStyleClass().removeAll("stat-up", "stat-down");
        if (balance >= 0) {
            estadisticaBalance.getStyleClass().add("stat-up");
            subBalance.setText("margen positivo");
        } else {
            estadisticaBalance.getStyleClass().add("stat-down");
            subBalance.setText("⚠ gastos > ingresos");
        }

        String tasaFormateadaGastos = Formateador.porcentaje(tasa);
        estadisticaTasa.setText(tasaFormateadaGastos);
        estadisticaTasa.getStyleClass().removeAll("stat-up", "stat-amber", "stat-down");

        String EstiloTasa;
        if (tasa >= 20) {
            EstiloTasa = "stat-up";
        } else if (tasa >= 0) {
            EstiloTasa = "stat-amber";
        } else {
            EstiloTasa = "stat-down";
        }
        estadisticaTasa.getStyleClass().add(EstiloTasa);
    }

    private void filtrarYRenderizar() {
        String filtroTipo          = cmbFiltroTipo.getValue();
        String textoBusquedaBruto  = TextoMensage.getText();
        String textoBusquedaSinEsp = textoBusquedaBruto.trim();
        String textoBusqueda       = textoBusquedaSinEsp.toLowerCase();

        List<Movimiento> filtrados = new java.util.ArrayList<>();
        for (Movimiento m : todos) {
            if ("Gastos".equals(filtroTipo) && m.isIngreso()) {
                continue;
            }
            if ("Ingresos".equals(filtroTipo) && !m.isIngreso()) {
                continue;
            }
            if (!textoBusqueda.isEmpty()) {
                String nombreMovimiento = m.getNombre();
                String nombreEnMinusculas = nombreMovimiento.toLowerCase();
                boolean enNombre = nombreEnMinusculas.contains(textoBusqueda);
                boolean categoriaNoNula = m.getCategoriaNombre() != null;
                boolean enCategoria;
                if (categoriaNoNula) {
                    String categoriaNombre = m.getCategoriaNombre();
                    String categoriaEnMinusculas = categoriaNombre.toLowerCase();
                    enCategoria = categoriaEnMinusculas.contains(textoBusqueda);
                } else {
                    enCategoria = false;
                }
                if (!enNombre && !enCategoria) {
                    continue;
                }
            }
            filtrados.add(m);
        }
        mostrarDatos(filtrados);
    }

    private void mostrarDatos(List<Movimiento> movs) {
        txList.getChildren().clear();

        for (Movimiento m : movs) {
            Runnable accionEliminar = () -> {
                movimeentoService.eliminar(m.getId());
                cargarMovimiento();
            };
            javafx.scene.Node filaEliminar = movimientoREnderes.crearFilaConEliminar(m, accionEliminar);
            txList.getChildren().add(filaEliminar);
        }

        if (movs.isEmpty()) {
            txList.getChildren().add(movimientoREnderes.crearMensajeVacio("Sin movimientos"));
        }
    }

    // Presupuesto

    /**
     * Comprueba si el nuevo gasto hace superar el presupuesto mensual del usuario.
     * Genera una notificación al cruzar el 80 % y otra al cruzar el 100 %.
     * Solo dispara la alerta en el momento exacto en que se cruza el umbral,
     * evitando notificaciones duplicadas en cada gasto posterior.
     */
    private void comprobarPresupuesto(int uid, double cantidadNuevoGasto) {
        Usuario u = Session.getInstance().getUsuarioActual();
        double presupuesto = u.getPresupuestoMensual();
        if (presupuesto <= 0) return;

        LocalDate hoy = LocalDate.now();
        double totalMes   = movDAO.obtenerTotalPorTipoYMes(uid, hoy.getYear(), hoy.getMonthValue(), "gasto");
        double totalAntes = totalMes - cantidadNuevoGasto;

        if (totalAntes <= presupuesto && totalMes > presupuesto) {
            Notificacion notif = new Notificacion();
            notif.setUsuarioId(uid);
            notif.setTitulo("Presupuesto superado");
            notif.setMensaje(String.format(
                "Has superado tu presupuesto mensual de %.2f€. Gastos del mes: %.2f€.",
                presupuesto, totalMes));
            notif.setTipo("danger");
            notifDAO.registrarNotificacion(notif);

        } else if (totalAntes <= presupuesto * 0.8 && totalMes > presupuesto * 0.8) {
            Notificacion notif = new Notificacion();
            notif.setUsuarioId(uid);
            notif.setTitulo("¡Presupuesto casi al límite!");
            notif.setMensaje(String.format(
                "Llevas el %.0f%% del presupuesto mensual gastado (%.2f€ de %.2f€).",
                (totalMes / presupuesto) * 100, totalMes, presupuesto));
            notif.setTipo("warning");
            notifDAO.registrarNotificacion(notif);
        }
    }

    // Guardar

    @FXML
    private void guardar() {
        TextoError.setText("");
        lblMsg.setText("");

        String cantStr = txtCantidad.getText().trim();
        String errorCantidad = movimientoValidador.validarCantidad(cantStr);
        if (errorCantidad != null) {
            TextoError.setText(errorCantidad);
            return;
        }

        String desc = txtNombre.getText().trim();
        String errorDesc = movimientoValidador.validarDescripcion(desc);
        if (errorDesc != null) {
            TextoError.setText(errorDesc);
            return;
        }

        double cant = movimientoValidador.parsearCantidad(cantStr);

        Movimiento m = new Movimiento();
        Session sesionGuardar = Session.getInstance();
        Usuario usuarioGuardar = sesionGuardar.getUsuarioActual();
        int uidGuardar = usuarioGuardar.getId();
        m.setUsuarioId(uidGuardar);

        String tipoSeleccionado = cmbTipo.getValue();
        if ("Ingreso".equals(tipoSeleccionado)) {
            m.setTipo("ingreso");
        } else {
            m.setTipo("gasto");
        }

        m.setCantidad(cant);
        m.setNombre(desc);
        m.setNotas(txtNotas.getText());

        LocalDate fechaSelect = dateFecha.getValue();
        if (fechaSelect != null) {
            m.setFecha(fechaSelect);
        } else {
            m.setFecha(LocalDate.now());
        }

        if (cmbCategoria.getValue() != null) {
            Categoria categoria = cmbCategoria.getValue();
            int idCategoria = categoria.getId();
            m.setCategoriaId(idCategoria);
        }

        boolean guardadoExitoso = movimeentoService.registrar(m);
        if (guardadoExitoso) {
            lblMsg.setText("Movimiento guardado correctamente.");
            txtCantidad.clear();
            txtNombre.clear();
            txtNotas.clear();
            dateFecha.setValue(LocalDate.now());
            cargarMovimiento();
            if ("gasto".equals(m.getTipo())) {
                comprobarPresupuesto(m.getUsuarioId(), m.getCantidad());
            }
        } else {
            TextoError.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }
}
