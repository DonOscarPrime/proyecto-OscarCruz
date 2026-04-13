package com.finanzapp.controller;

import com.finanzapp.MainApp;
import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    // Header
    @FXML private Label avatarLabel, lblNombre, lblEmail, lblSituacion, lblComunidad;
    @FXML private Label statMovs, statObjs;

    // Datos personales – vista
    @FXML private Label  vNombre, vEmail, vTelefono, vFechaNac, vComunidad;
    @FXML private javafx.scene.layout.VBox vistaPanel, editPanel;
    @FXML private Button btnEditToggle;

    // Datos personales – edición
    @FXML private TextField     eNombre, eEmail, eTelefono;
    @FXML private DatePicker    eFechaNac;
    @FXML private ComboBox<String> eComunidad;
    @FXML private Label lblPerfilMsg, lblPerfilErr;

    // Seguridad
    @FXML private PasswordField passActual, passNueva, passConfirm;
    @FXML private Label lblPassMsg, lblPassErr;

    // Financiera
    @FXML private ComboBox<String> cmbSituacion, cmbObjetivo;
    @FXML private TextField        txtIngresos, txtPresupuesto;
    @FXML private Label            lblFinMsg;

    private final UsuarioDAO    usuDAO = new UsuarioDAO();
    private final MovimientoDAO movDAO = new MovimientoDAO();
    private final ObjetivoDAO   objDAO = new ObjetivoDAO();

    private static final List<String> COMUNIDADES = List.of(
        "Madrid","Cataluña","Andalucía","Valencia","País Vasco","Galicia","Castilla y León","Otra");
    private static final List<String> SITUACIONES = List.of(
        "Estudiante","Empleado","Autónomo","Funcionario","Desempleado");
    private static final List<String> OBJETIVOS = List.of(
        "Ahorrar más cada mes","Controlar mis gastos","Iniciarme en inversión",
        "Saldar deudas","Comprar vivienda");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eComunidad.setItems(FXCollections.observableArrayList(COMUNIDADES));
        cmbSituacion.setItems(FXCollections.observableArrayList(SITUACIONES));
        cmbObjetivo.setItems(FXCollections.observableArrayList(OBJETIVOS));
        cargar();
    }

    private void cargar() {
        Usuario u = Session.getInstance().getUsuarioActual();

        // Header
        avatarLabel.setText(u.getIniciales());
        lblNombre.setText(u.getNombre());
        lblEmail.setText(u.getEmail());
        lblSituacion.setText(u.getSituacionLaboral() != null ? u.getSituacionLaboral() : "—");
        lblComunidad.setText(u.getComunidad() != null ? u.getComunidad() : "—");

        // Stats
        long movCount = movDAO.listarPorUsuario(u.getId()).size();
        long objCount = objDAO.listarPorUsuario(u.getId()).stream().filter(o -> !o.isCompletado()).count();
        statMovs.setText(String.valueOf(movCount));
        statObjs.setText(String.valueOf(objCount));

        // Vista datos
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"));
        vNombre.setText(u.getNombre());
        vEmail.setText(u.getEmail());
        vTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "—");
        vFechaNac.setText(u.getFechaNacimiento() != null ? u.getFechaNacimiento().format(dtf) : "—");
        vComunidad.setText(u.getComunidad() != null ? u.getComunidad() : "—");

        // Edición prefill
        eNombre.setText(u.getNombre());
        eEmail.setText(u.getEmail());
        eTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "");
        eFechaNac.setValue(u.getFechaNacimiento());
        eComunidad.setValue(u.getComunidad() != null ? u.getComunidad() : COMUNIDADES.get(0));

        // Financiera
        cmbSituacion.setValue(u.getSituacionLaboral() != null ? u.getSituacionLaboral() : SITUACIONES.get(0));
        txtIngresos.setText(u.getIngresosNetos() > 0 ? String.valueOf((int)u.getIngresosNetos()) : "");
        cmbObjetivo.setValue(u.getObjetivoFinanciero() != null ? u.getObjetivoFinanciero() : OBJETIVOS.get(0));
        txtPresupuesto.setText(u.getPresupuestoMensual() > 0 ? String.valueOf((int)u.getPresupuestoMensual()) : "");
    }

    @FXML void toggleEditar() {
        boolean editando = !editPanel.isVisible();
        editPanel.setVisible(editando);  editPanel.setManaged(editando);
        vistaPanel.setVisible(!editando); vistaPanel.setManaged(!editando);
        btnEditToggle.setText(editando ? "✕ Cancelar" : "✏ Editar");
        lblPerfilMsg.setText(""); lblPerfilErr.setText("");
    }

    @FXML void guardarPerfil() {
        lblPerfilErr.setText(""); lblPerfilMsg.setText("");
        String nombre = eNombre.getText().trim();
        String email  = eEmail.getText().trim();
        if (nombre.isEmpty() || email.isEmpty()) {
            lblPerfilErr.setText("Nombre y email son obligatorios.");
            return;
        }
        Usuario u = Session.getInstance().getUsuarioActual();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono(eTelefono.getText().trim());
        u.setFechaNacimiento(eFechaNac.getValue());
        u.setComunidad(eComunidad.getValue());

        if (usuDAO.actualizarPerfil(u)) {
            lblPerfilMsg.setText("✓ Datos guardados correctamente.");
            cargar();
            toggleEditar();
        } else {
            lblPerfilErr.setText("Error al guardar. ¿El email ya está en uso?");
        }
    }

    @FXML void cambiarPassword() {
        lblPassErr.setText(""); lblPassMsg.setText("");
        String actual   = passActual.getText();
        String nueva    = passNueva.getText();
        String confirma = passConfirm.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirma.isEmpty()) {
            lblPassErr.setText("Rellena todos los campos.");
            return;
        }
        if (!nueva.equals(confirma)) {
            lblPassErr.setText("Las contraseñas no coinciden.");
            return;
        }
        if (nueva.length() < 8) {
            lblPassErr.setText("La nueva contraseña debe tener al menos 8 caracteres.");
            return;
        }
        int uid = Session.getInstance().getUsuarioActual().getId();
        if (usuDAO.cambiarPassword(uid, actual, nueva)) {
            lblPassMsg.setText("✓ Contraseña cambiada correctamente.");
            passActual.clear(); passNueva.clear(); passConfirm.clear();
        } else {
            lblPassErr.setText("La contraseña actual no es correcta.");
        }
    }

    @FXML void guardarFinanciera() {
        lblFinMsg.setText("");
        Usuario u = Session.getInstance().getUsuarioActual();
        u.setSituacionLaboral(cmbSituacion.getValue());
        u.setObjetivoFinanciero(cmbObjetivo.getValue());
        try { u.setIngresosNetos(Double.parseDouble(txtIngresos.getText().replace(",","."))); } catch (Exception ignored) {}
        try { u.setPresupuestoMensual(Double.parseDouble(txtPresupuesto.getText().replace(",","."))); } catch (Exception ignored) {}

        if (usuDAO.actualizarPerfil(u)) {
            lblFinMsg.setText("✓ Situación financiera guardada.");
        }
    }

    @FXML void irAjustes()     { MainApp.navigateTo("main"); /* MainController handles routing */ }
    @FXML void cerrarSesion()  {
        Session.getInstance().cerrarSesion();
        MainApp.navigateTo("login");
    }
}
