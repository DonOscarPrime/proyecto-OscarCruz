package com.finanzapp.controller;

import com.finanzapp.MainApp;
import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Objetivo;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.Session;
import com.finanzapp.validator.UsuarioValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador del panel de perfil del usuario en Fox Wallet.
 * Administra los datos del perfil, validaciones y acceso a la información.
 */
public class PerfilController implements Initializable {

    // Header
    @FXML private Label avatarLabel;
    @FXML private Label textoNombre;
    @FXML private Label textoEmail;
    @FXML private Label textoSituacion;
    @FXML private Label textoComunidad;
    @FXML private Label estadisticasMovimiento;
    @FXML private Label estadisObjetivos;

    // Datos personales – vista
    @FXML private Label  vNombre;
    @FXML private Label  vEmail;
    @FXML private Label  vTelefono;
    @FXML private Label  vFechaNac;
    @FXML private Label  vComunidad;
    @FXML private VBox   vistaPanel;
    @FXML private VBox   editPanel;
    @FXML private Button botonToggle;

    // Datos personales – edición
    @FXML private TextField     eNombre;
    @FXML private TextField     eEmail;
    @FXML private TextField     eTelefono;
    @FXML private DatePicker    eFechaNac;
    @FXML private ComboBox<String> eComunidad;
    @FXML private Label lblPerfilMensaje;
    @FXML private Label lblPerfilErr;

    // Seguridad
    @FXML private PasswordField passActual;
    @FXML private PasswordField passNueva;
    @FXML private PasswordField passConfirm;
    @FXML private Label lblPassMsg;
    @FXML private Label lblPassErr;

    // Financiera
    @FXML private ComboBox<String> cmbSituacion;
    @FXML private ComboBox<String> cmbObjetivo;
    @FXML private TextField        txtIngresos;
    @FXML private TextField        txtPresupuesto;
    @FXML private Label            lblFinMsg;

    private final UsuarioDAO      usuDAO    = new UsuarioDAO();
    private final MovimientoDAO   movDAO    = new MovimientoDAO();
    private final ObjetivoDAO     objDAO    = new ObjetivoDAO();
    private final UsuarioValidator validator = new UsuarioValidator();

    private static final List<String> COMUNIDADES = List.of(
        "Madrid", "Cataluña", "Andalucía", "Valencia", "País Vasco", "Galicia", "Castilla y León", "Otra");
    private static final List<String> SITUACIONES = List.of(
        "Estudiante", "Empleado", "Autónomo", "Funcionario", "Desempleado");
    private static final List<String> OBJETIVOS = List.of(
        "Ahorrar más cada mes", "Controlar mis gastos", "Iniciarme en inversión",
        "Saldar deudas", "Comprar vivienda");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        javafx.collections.ObservableList<String> itemsComunidades = FXCollections.observableArrayList(COMUNIDADES);
        eComunidad.setItems(itemsComunidades);
        javafx.collections.ObservableList<String> itemsSituaciones = FXCollections.observableArrayList(SITUACIONES);
        cmbSituacion.setItems(itemsSituaciones);
        javafx.collections.ObservableList<String> itemsObjetivos = FXCollections.observableArrayList(OBJETIVOS);
        cmbObjetivo.setItems(itemsObjetivos);
        cargarDatosPerfil();
    }

    //Carga de datos

    private void cargarDatosPerfil() {
        Session sesionActual = Session.getInstance();
        Usuario u = sesionActual.getUsuarioActual();
        rellenarCabecera(u);

        int idUsuarioPerfil = u.getId();
        List<com.finanzapp.model.Movimiento> listaMovimientos = movDAO.obtenerMovimientosDeUsuario(idUsuarioPerfil);
        int cantidadMovimientos = listaMovimientos.size();
        String textoCantidadMovimientos = String.valueOf(cantidadMovimientos);
        estadisticasMovimiento.setText(textoCantidadMovimientos);
        int idUsuarioObjs = u.getId();
        List<Objetivo> todosObjs = objDAO.obtenerObjetivosUsuario(idUsuarioObjs);
        int activos = 0;
        for (Objetivo obj : todosObjs) {
            if (!obj.iscompletado()) {
                activos = activos + 1;
            }
        }
        String textoObjetivosActivos = String.valueOf(activos);
        estadisObjetivos.setText(textoObjetivosActivos);

        poblarVistaDatos(u);
        rellenaFormulario(u);
        rellenadatosFinancieros(u);
    }

    private void rellenarCabecera(Usuario u) {
        avatarLabel.setText(u.getIniciales());
        textoNombre.setText(u.getNombre());
        textoEmail.setText(u.getEmail());

        String situacion = u.getSituacionLaboral();
        if (situacion != null) {
            textoSituacion.setText(situacion);
        } else {
            textoSituacion.setText("—");
        }

        String comunidad = u.getComunidad();
        if (comunidad != null) {
            textoComunidad.setText(comunidad);
        } else {
            textoComunidad.setText("—");
        }
    }

    private void poblarEstadisticas(Usuario u) {
        int idUsuarioStats = u.getId();
        List<com.finanzapp.model.Movimiento> listaMovimientosStats = movDAO.obtenerMovimientosDeUsuario(idUsuarioStats);
        int cantidadMovimientos = listaMovimientosStats.size();
        String textoCantidadMovimientosStats = String.valueOf(cantidadMovimientos);
        estadisticasMovimiento.setText(textoCantidadMovimientosStats);

        int idUsuarioObjsStats = u.getId();
        List<Objetivo> todos = objDAO.obtenerObjetivosUsuario(idUsuarioObjsStats);
        int activos = 0;
        for (Objetivo obj : todos) {
            if (!obj.iscompletado()) {
                activos = activos + 1;
            }
        }
        String textoObjetivosActivosStats = String.valueOf(activos);
        estadisObjetivos.setText(textoObjetivosActivosStats);
    }

    private void poblarVistaDatos(Usuario u) {
        Locale localeEspanol = new Locale("es");
        String patronFecha = "d 'de' MMMM 'de' yyyy";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(patronFecha, localeEspanol);

        vNombre.setText(u.getNombre());
        vEmail.setText(u.getEmail());

        String telefono = u.getTelefono();
        if (telefono != null) {
            vTelefono.setText(telefono);
        } else {
            vTelefono.setText("—");
        }

        if (u.getFechaNacimiento() != null) {
            String fechaFormateada = u.getFechaNacimiento().format(dtf);
            vFechaNac.setText(fechaFormateada);
        } else {
            vFechaNac.setText("—");
        }

        String comunidad = u.getComunidad();
        if (comunidad != null) {
            vComunidad.setText(comunidad);
        } else {
            vComunidad.setText("—");
        }
    }

    private void rellenaFormulario(Usuario u) {
        eNombre.setText(u.getNombre());
        eEmail.setText(u.getEmail());

        String telefono = u.getTelefono();
        if (telefono != null) {
            eTelefono.setText(telefono);
        } else {
            eTelefono.setText("");
        }

        eFechaNac.setValue(u.getFechaNacimiento());

        String comunidad = u.getComunidad();
        if (comunidad != null) {
            eComunidad.setValue(comunidad);
        } else {
            String comunidadPorDefecto = COMUNIDADES.get(0);
            eComunidad.setValue(comunidadPorDefecto);
        }
    }

    private void rellenadatosFinancieros(Usuario u) {
        String situacion = u.getSituacionLaboral();
        if (situacion != null) {
            cmbSituacion.setValue(situacion);
        } else {
            String situacionPorDefecto = SITUACIONES.get(0);
            cmbSituacion.setValue(situacionPorDefecto);
        }

        double ingresosNetos = u.getIngresosNetos();
        if (ingresosNetos > 0) {
            int ingresosComoEntero = (int) ingresosNetos;
            String ingresosComoTexto = String.valueOf(ingresosComoEntero);
            txtIngresos.setText(ingresosComoTexto);
        } else {
            txtIngresos.setText("");
        }

        String objetivo = u.getObjetivoFinanciero();
        if (objetivo != null) {
            cmbObjetivo.setValue(objetivo);
        } else {
            String objetivoPorDefecto = OBJETIVOS.get(0);
            cmbObjetivo.setValue(objetivoPorDefecto);
        }

        double presupuestoMensual = u.getPresupuestoMensual();
        if (presupuestoMensual > 0) {
            int presupuestoComoEntero = (int) presupuestoMensual;
            String presupuestoComoTexto = String.valueOf(presupuestoComoEntero);
            txtPresupuesto.setText(presupuestoComoTexto);
        } else {
            txtPresupuesto.setText("");
        }
    }

    // ── Acciones del formulario ───────────────────────────────────────────

    @FXML
    void rellenaDatos() {
        boolean estabaEditando = editPanel.isVisible();
        boolean ahoraEditando  = !estabaEditando;
        boolean mostrarVista   = !ahoraEditando;

        editPanel.setVisible(ahoraEditando);
        editPanel.setManaged(ahoraEditando);
        vistaPanel.setVisible(mostrarVista);
        vistaPanel.setManaged(mostrarVista);

        boolean Editado;
        if (ahoraEditando) {
            botonToggle.setText("✕ Cancelar");
            Editado = true;
        } else {
            botonToggle.setText("✏ Editar");
            Editado = false;
        }

        lblPerfilMensaje.setText("");
        lblPerfilErr.setText("");
    }

    @FXML
    void toggleEditar() {
        rellenaDatos();
    }

    @FXML
    void guardarPerfil() {
        lblPerfilErr.setText("");
        lblPerfilMensaje.setText("");

        String nombre = eNombre.getText().trim();
        String email  = eEmail.getText().trim();

        String error = validator.validarDatosBasicos(nombre, email);
        if (error != null) {
            lblPerfilErr.setText(error);
            return;
        }

        Session sesionGuardar = Session.getInstance();
        Usuario u = sesionGuardar.getUsuarioActual();
        String telefonoEditado = eTelefono.getText().trim();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono(telefonoEditado);
        u.setFechaNacimiento(eFechaNac.getValue());
        u.setComunidad(eComunidad.getValue());

        boolean guardadoCorrecto = usuDAO.actualizarPerfil(u);
        if (guardadoCorrecto) {
            lblPerfilMensaje.setText("✓ Datos guardados correctamente.");
            cargarDatosPerfil();
            rellenaDatos();
        } else {
            lblPerfilErr.setText("Error al guardar. ¿El email ya está en uso?");
        }
    }

    @FXML
    void cambiarPassword() {
        lblPassErr.setText("");
        lblPassMsg.setText("");

        String actual   = passActual.getText();
        String nueva    = passNueva.getText();
        String confirma = passConfirm.getText();

        String error = validator.validarCambioPassword(actual, nueva, confirma);
        if (error != null) {
            lblPassErr.setText(error);
            return;
        }

        Session sesionPassword = Session.getInstance();
        Usuario usuarioPassword = sesionPassword.getUsuarioActual();
        int uid = usuarioPassword.getId();
        boolean ok = usuDAO.cambiarContrasena(uid, actual, nueva);

        if (ok) {
            lblPassMsg.setText("✓ Contraseña cambiada correctamente.");
            passActual.clear();
            passNueva.clear();
            passConfirm.clear();
        } else {
            lblPassErr.setText("La contraseña actual no es correcta.");
        }
    }

    @FXML
    void guardarFinanciera() {
        lblFinMsg.setText("");

        Session sesionFinanciera = Session.getInstance();
        Usuario u = sesionFinanciera.getUsuarioActual();
        String situacionSeleccionada = cmbSituacion.getValue();
        String objetivoSeleccionado = cmbObjetivo.getValue();
        u.setSituacionLaboral(situacionSeleccionada);
        u.setObjetivoFinanciero(objetivoSeleccionado);

        String textoIngresosBruto = txtIngresos.getText();
        String textoIngresos = textoIngresosBruto.replace(",", ".");
        if (!textoIngresos.isBlank()) {
            try {
                double ingresosParseados = Double.parseDouble(textoIngresos);
                u.setIngresosNetos(ingresosParseados);
            } catch (NumberFormatException ignorado) {
            }
        }

        String textoPresupuestoBruto = txtPresupuesto.getText();
        String textoPresupuesto = textoPresupuestoBruto.replace(",", ".");
        if (!textoPresupuesto.isBlank()) {
            try {
                double presupuestoParseado = Double.parseDouble(textoPresupuesto);
                u.setPresupuestoMensual(presupuestoParseado);
            } catch (NumberFormatException ignorado) {
            }
        }

        boolean guardadoCorrecto = usuDAO.actualizarPerfil(u);
        if (guardadoCorrecto) {
            lblFinMsg.setText("✓ Situación financiera guardada.");
        }
    }

    @FXML
    void irAjustes() {
        MainApp.irA("main");
    }

    @FXML
    void cerrarSesion() {
        Session sesionParaCerrar = Session.getInstance();
        sesionParaCerrar.cerrarSesion();
        MainApp.irA("login");
    }
}
