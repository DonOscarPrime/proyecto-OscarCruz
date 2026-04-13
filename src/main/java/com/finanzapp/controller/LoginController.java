package com.finanzapp.controller;

import com.finanzapp.MainApp;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private Button tabLoginBtn, tabRegBtn;
    @FXML private VBox   loginPanel, registerPanel;

    // Login
    @FXML private TextField     loginEmail;
    @FXML private PasswordField loginPass;
    @FXML private Label         loginError;

    // Registro
    @FXML private TextField     regNombre, regEmail;
    @FXML private PasswordField regPass, regPass2;
    @FXML private Label         regError;

    private final UsuarioDAO dao = new UsuarioDAO();

    @FXML public void showLogin() {
        loginPanel.setVisible(true);   loginPanel.setManaged(true);
        registerPanel.setVisible(false); registerPanel.setManaged(false);
        tabLoginBtn.getStyleClass().add("login-tab-active");
        tabRegBtn.getStyleClass().remove("login-tab-active");
        loginError.setText("");
    }

    @FXML public void showRegister() {
        registerPanel.setVisible(true);  registerPanel.setManaged(true);
        loginPanel.setVisible(false);    loginPanel.setManaged(false);
        tabRegBtn.getStyleClass().add("login-tab-active");
        tabLoginBtn.getStyleClass().remove("login-tab-active");
        regError.setText("");
    }

    @FXML public void doLogin() {
        String email = loginEmail.getText().trim();
        String pass  = loginPass.getText();
        if (email.isEmpty() || pass.isEmpty()) {
            loginError.setText("Por favor rellena todos los campos.");
            return;
        }
        Usuario u = dao.login(email, pass);
        if (u == null) {
            loginError.setText("Email o contraseña incorrectos.");
            return;
        }
        Session.getInstance().setUsuarioActual(u);
        MainApp.navigateTo("main");
    }

    @FXML public void doRegister() {
        String nombre = regNombre.getText().trim();
        String email  = regEmail.getText().trim();
        String pass   = regPass.getText();
        String pass2  = regPass2.getText();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            regError.setText("Rellena todos los campos obligatorios.");
            return;
        }
        if (!pass.equals(pass2)) {
            regError.setText("Las contraseñas no coinciden.");
            return;
        }
        if (pass.length() < 8) {
            regError.setText("La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setComunidad("Madrid");

        try {
            boolean ok = dao.registrar(u, pass);
            if (!ok) {
                regError.setText("Error al crear la cuenta. Inténtalo de nuevo.");
                return;
            }
        } catch (RuntimeException ex) {
            if ("EMAIL_DUPLICADO".equals(ex.getMessage())) {
                regError.setText("Ese email ya está registrado. Inicia sesión o usa otro.");
            } else {
                regError.setText("Error inesperado. Inténtalo de nuevo.");
            }
            return;
        }
        Session.getInstance().setUsuarioActual(u);
        MainApp.navigateTo("main");
    }
}
