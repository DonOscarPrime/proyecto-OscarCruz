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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del panel de ajustes de Fox Wallet.
 * <p>
 * Permite al usuario cambiar sus preferencias de idioma, moneda y comunidad
 * autónoma, alternar entre tema claro y oscuro, y configurar qué tipos
 * de notificaciones automáticas desea recibir.
 */
public class AjustesController implements Initializable {

    @FXML private ComboBox<String> cmbIdioma, cmbMoneda, cmbComunidad;
    @FXML private CheckBox  chkPresupuesto, chkObjetivos, chkResumen;
    @FXML private VBox      btnClaro, btnOscuro;
    @FXML private Label     lblMsg;

    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbIdioma.setItems(FXCollections.observableArrayList("Español", "English"));
        cmbMoneda.setItems(FXCollections.observableArrayList("€ Euro", "$ Dólar", "£ Libra"));
        cmbComunidad.setItems(FXCollections.observableArrayList(
            "Madrid","Cataluña","Andalucía","Valencia","País Vasco","Galicia","Castilla y León","Otra"));

        Usuario u = Session.getInstance().getUsuarioActual();
        cmbIdioma.setValue(u.getIdioma() != null ? u.getIdioma() : "Español");
        cmbMoneda.setValue(u.getMoneda() != null ? (u.getMoneda().equals("€") ? "€ Euro" : u.getMoneda()) : "€ Euro");
        cmbComunidad.setValue(u.getComunidad() != null ? u.getComunidad() : "Madrid");
    }

    @FXML void temaClaro(MouseEvent e) {
        btnClaro.getStyleClass().removeAll("theme-btn","theme-btn-active");
        btnClaro.getStyleClass().add("theme-btn-active");
        btnOscuro.getStyleClass().removeAll("theme-btn","theme-btn-active");
        btnOscuro.getStyleClass().add("theme-btn");
        dao.guardarPreferenciaTema(Session.getInstance().getUsuarioActual().getId(), "claro");
    }

    @FXML void temaOscuro(MouseEvent e) {
        btnOscuro.getStyleClass().removeAll("theme-btn","theme-btn-active");
        btnOscuro.getStyleClass().add("theme-btn-active");
        btnClaro.getStyleClass().removeAll("theme-btn","theme-btn-active");
        btnClaro.getStyleClass().add("theme-btn");
        dao.guardarPreferenciaTema(Session.getInstance().getUsuarioActual().getId(), "oscuro");
    }

    @FXML void guardar() {
        Usuario u = Session.getInstance().getUsuarioActual();
        u.setIdioma(cmbIdioma.getValue());
        String moneda = cmbMoneda.getValue();
        u.setMoneda(moneda != null && moneda.startsWith("€") ? "€" : moneda != null && moneda.startsWith("$") ? "$" : "£");
        u.setComunidad(cmbComunidad.getValue());
        dao.actualizarPerfilUsuario(u);
        lblMsg.setText("✓ Ajustes guardados.");
    }
}
