package com.finanzapp.controller;

import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 Controlador del panel de ajustes.
 Permite al usuario modificar ciertos ajustes en la aplicación como el filtro claro/oscuro, cambiar el tipo de moneda
 , la comunidad donde reside.
 */
public class AjustesController implements Initializable {

    @FXML private ComboBox<String> selectorIdioma;
    @FXML private ComboBox<String> selectMoneda;
    @FXML private ComboBox<String> selectComunidad;
    @FXML private CheckBox  checkBoxPresupuesto;
    @FXML private CheckBox  checkBoxObjetivo;
    @FXML private CheckBox  checkBoxResumen;
    @FXML private VBox      botClaro;
    @FXML private VBox      botOscuro;
    @FXML private Label     lblMsg;

    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectorIdioma.setItems(FXCollections.observableArrayList("Español", "English"));
        selectMoneda.setItems(FXCollections.observableArrayList("€ Euro", "$ Dólar", "£ Libra"));
        selectComunidad.setItems(FXCollections.observableArrayList(
            "Madrid", "Cataluña", "Andalucía", "Valencia", "País Vasco", "Galicia", "Castilla y León", "Otra"));

        Session sesionInit = Session.getInstance();
        Usuario u = sesionInit.getUsuarioActual();

        String idiomaUsuario = u.getIdioma();
        if (idiomaUsuario != null) {
            selectorIdioma.setValue(idiomaUsuario);
        } else {
            selectorIdioma.setValue("Español");
        }

        String monedaUsuario = u.getMoneda();
        if (monedaUsuario != null) {
            if (monedaUsuario.equals("€")) {
                selectMoneda.setValue("€ Euro");
            } else {
                selectMoneda.setValue(monedaUsuario);
            }
        } else {
            selectMoneda.setValue("€ Euro");
        }

        String comunidadUsuario = u.getComunidad();
        if (comunidadUsuario != null) {
            selectComunidad.setValue(comunidadUsuario);
        } else {
            selectComunidad.setValue("Madrid");
        }
    }

    @FXML
    void modoClaro(MouseEvent e) {
        botClaro.getStyleClass().removeAll("theme-btn", "theme-btn-active");
        botClaro.getStyleClass().add("theme-btn-active");
        botOscuro.getStyleClass().removeAll("theme-btn", "theme-btn-active");
        botOscuro.getStyleClass().add("theme-btn");
        Session sesionClaro = Session.getInstance();
        Usuario usuarioClaro = sesionClaro.getUsuarioActual();
        int idUsuario = usuarioClaro.getId();
        dao.guardarPreferencia(idUsuario, "claro");
    }

    @FXML
    void modoOscuro(MouseEvent e) {
        botOscuro.getStyleClass().removeAll("theme-btn", "theme-btn-active");
        botOscuro.getStyleClass().add("theme-btn-active");
        botClaro.getStyleClass().removeAll("theme-btn", "theme-btn-active");
        botClaro.getStyleClass().add("theme-btn");
        Session sesionOscuro = Session.getInstance();
        Usuario usuarioOscuro = sesionOscuro.getUsuarioActual();
        int idUsuario = usuarioOscuro.getId();
        dao.guardarPreferencia(idUsuario, "oscuro");
    }

    @FXML
    void guardar() {
        Session sesionGuardar = Session.getInstance();
        Usuario u = sesionGuardar.getUsuarioActual();
        u.setIdioma(selectorIdioma.getValue());

        String monedaSeleccionada = selectMoneda.getValue();
        boolean monedaNoNula = monedaSeleccionada != null;
        boolean esEuro = monedaNoNula && monedaSeleccionada.startsWith("€");
        boolean esDolar = monedaNoNula && monedaSeleccionada.startsWith("$");
        String simboloMoneda;
        if (esEuro) {
            simboloMoneda = "€";
        } else if (esDolar) {
            simboloMoneda = "$";
        } else {
            simboloMoneda = "£";
        }
        u.setMoneda(simboloMoneda);

        u.setComunidad(selectComunidad.getValue());
        dao.actualizarPerfil(u);
        lblMsg.setText("✓ Ajustes guardados.");
    }
}
