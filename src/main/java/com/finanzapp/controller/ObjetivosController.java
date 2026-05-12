package com.finanzapp.controller;

import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.model.Objetivo;
import com.finanzapp.model.Usuario;
import com.finanzapp.renderer.ObjetivoRenderer;
import com.finanzapp.service.ObjetivoService;
import com.finanzapp.util.Session;
import com.finanzapp.validator.ObjetivoValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de objetivos de ahorro.
 * Se encarga de objetivos de la validación y de la visualización
 */
public class ObjetivosController implements Initializable {

    @FXML private VBox     formPanel;
    @FXML private FlowPane objetivosGrid;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmoji;
    @FXML private TextField txtObjetivo;
    @FXML private TextField txtActual;
    @FXML private DatePicker FechaLimite;
    @FXML private Label txtError;

    private final ObjetivoDAO       objDAO       = new ObjetivoDAO();
    private final ObjetivoService   objService   = new ObjetivoService();
    private final ObjetivoRenderer  objRenderer  = new ObjetivoRenderer();
    private final ObjetivoValidator objValidator = new ObjetivoValidator();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarObjetivosAhorro();
    }

    // ── Carga y renderizado ───────────────────────────────────────────────

    private void cargarObjetivosAhorro() {
        Session sesionActual = Session.getInstance();
        Usuario usuarioEnSesion = sesionActual.getUsuarioActual();
        int uid = usuarioEnSesion.getId();
        List<Objetivo> objetivos = objService.obtenerDeUsuario(uid);

        objetivosGrid.getChildren().clear();
        for (Objetivo objetivo : objetivos) {
            VBox tarjeta = objRenderer.crearTarjeta(
                objetivo,
                importe -> aplicarAporte(objetivo, importe),
                () -> eliminarObjetivo(objetivo.getId())
            );
            objetivosGrid.getChildren().add(tarjeta);
        }
    }

    // ── Aporte y eliminación ──────────────────────────────────────────────

    private void aplicarAporte(Objetivo objetivo, double importe) {
        objService.aplicarAporte(objetivo, importe);
        cargarObjetivosAhorro();
    }

    private void eliminarObjetivo(int id) {
        objService.eliminar(id);
        cargarObjetivosAhorro();
    }

    // ── Formulario de nuevo objetivo ──────────────────────────────────────

    @FXML
    void toggleForm() {
        boolean eraVisible   = formPanel.isVisible();
        boolean visible      = !eraVisible;
        formPanel.setVisible(visible);
        formPanel.setManaged(visible);

        if (!visible) {
            txtNombre.clear();
            txtEmoji.clear();
            txtObjetivo.clear();
            txtActual.clear();
            txtError.setText("");
        }
    }

    @FXML
    void guardar() {
        txtError.setText("");

        String nombre    = txtNombre.getText().trim();
        String objStr    = txtObjetivo.getText().trim();

        if (objStr.isEmpty()) {
            txtError.setText("Indica el importe objetivo.");
            return;
        }

        String errorNombreMeta = objValidator.validarNombreYMeta(nombre, objStr);
        if (errorNombreMeta != null) {
            txtError.setText(errorNombreMeta);
            return;
        }

        String errorImporteMeta = objValidator.validarImporteMeta(objStr);
        if (errorImporteMeta != null) {
            txtError.setText(errorImporteMeta);
            return;
        }

        String errorImporteInicial = objValidator.validarImporteInicial(txtActual.getText());
        if (errorImporteInicial != null) {
            txtError.setText(errorImporteInicial);
            return;
        }

        double importeMeta = objValidator.parsearImporte(objStr);

        double importeInicial = 0;
        if (!txtActual.getText().isBlank()) {
            importeInicial = objValidator.parsearImporte(txtActual.getText());
        }

        Objetivo nuevoObjetivo = new Objetivo();
        Session sesionGuardar = Session.getInstance();
        Usuario usuarioGuardar = sesionGuardar.getUsuarioActual();
        int uidGuardar = usuarioGuardar.getId();
        nuevoObjetivo.setUsuarioId(uidGuardar);
        nuevoObjetivo.setNombre(nombre);
        nuevoObjetivo.setObjetivo(importeMeta);
        nuevoObjetivo.setActual(importeInicial);

        String textoEmoji = txtEmoji.getText();
        if (textoEmoji.isBlank()) {
            nuevoObjetivo.setEmoji("🎯");
        } else {
            String textoEmojiLimpio = textoEmoji.trim();
            nuevoObjetivo.setEmoji(textoEmojiLimpio);
        }

        nuevoObjetivo.setFechaLimite(FechaLimite.getValue());

        boolean guardadoExitoso = objService.crear(nuevoObjetivo);
        if (guardadoExitoso) {
            toggleForm();
            cargarObjetivosAhorro();
        } else {
            txtError.setText("Error al guardar. Inténtalo de nuevo.");
        }
    }
}
