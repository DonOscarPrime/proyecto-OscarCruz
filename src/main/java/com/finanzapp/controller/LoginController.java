package com.finanzapp.controller;

import com.finanzapp.MainApp;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.LoginAttemptService;
import com.finanzapp.util.Session;
import com.finanzapp.validator.UsuarioValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controlador de la pantalla de login y registro de Fox Wallet.
 * Recive los datos de registro, los delega a UsuarioValidador y la autentificacion la delega
 * a UsuarioDAO.
 *
 */
public class LoginController {

    @FXML private Button tabLoginBtn;
    @FXML private Button tabRegBtn;
    @FXML private VBox   panelEmail;
    @FXML private VBox   PanelError;

    @FXML private TextField     loginEmail;
    @FXML private PasswordField loginPass;
    @FXML private Label         loginError;

    @FXML private TextField     registroNombre;
    @FXML private TextField     registroEmail;
    @FXML private PasswordField RegistroPass;
    @FXML private PasswordField registroPass2;
    @FXML private Label         regError;

    private final UsuarioDAO       dao       = new UsuarioDAO();
    private final UsuarioValidator validator = new UsuarioValidator();

    //Navegación

    @FXML
    public void mostrarLogin() {
        panelEmail.setVisible(true);
        panelEmail.setManaged(true);
        PanelError.setVisible(false);
        PanelError.setManaged(false);
        tabLoginBtn.getStyleClass().add("login-tab-active");
        tabRegBtn.getStyleClass().remove("login-tab-active");
        loginError.setText("");
    }

    @FXML
    public void showLogin() {
        mostrarLogin();
    }

    @FXML
    public void mostrarRegister() {
        PanelError.setVisible(true);
        PanelError.setManaged(true);
        panelEmail.setVisible(false);
        panelEmail.setManaged(false);
        tabRegBtn.getStyleClass().add("login-tab-active");
        tabLoginBtn.getStyleClass().remove("login-tab-active");
        regError.setText("");
    }

    @FXML
    public void showRegister() {
        mostrarRegister();
    }

    @FXML
    public void hacerLogin() {
        String email = loginEmail.getText().trim();
        String pass  = loginPass.getText();

        boolean emailVacio = email.length() == 0;
        boolean passVacio = pass.length() == 0;
        boolean camposVacios = emailVacio || passVacio;
        if (camposVacios) {
            loginError.setText("Rellena los campos de acceso.");
            return;
        }

        // Bloqueo por intentos fallidos
        if (LoginAttemptService.estaBloqueado(email)) {
            loginError.setText("Acceso bloqueado por demasiados intentos. Espera " +
                    LoginAttemptService.BLOQUEO_MINUTOS + " minutos.");
            return;
        }

        String error = validator.validarLogin(email, pass);
        if (error != null) {
            loginError.setText(error);
            return;
        }

        Usuario usuario = dao.autenticarUsuario(email, pass);
        if (usuario == null) {
            LoginAttemptService.registrarFallo(email);
            if (LoginAttemptService.estaBloqueado(email)) {
                loginError.setText("Demasiados intentos fallidos. Acceso bloqueado " +
                        LoginAttemptService.BLOQUEO_MINUTOS + " minutos.");
            } else {
                int restantes = LoginAttemptService.MAX_INTENTOS - LoginAttemptService.getIntentos(email);
                loginError.setText("Email o contraseña incorrectos. " +
                        "Intentos restantes: " + restantes + ".");
            }
            return;
        }

        LoginAttemptService.registrarExito(email);
        Session sesionLogin = Session.getInstance();
        sesionLogin.setUsuarioActual(usuario);
        MainApp.irA("main");
    }

    @FXML
    public void doLogin() {
        hacerLogin();
    }

    @FXML
    public void hacerRegister() {
        String nombre = registroNombre.getText().trim();
        String email  = registroEmail.getText().trim();
        String pass   = RegistroPass.getText();
        String pass2  = registroPass2.getText();

        String error = validator.validarRegistro(nombre, email, pass, pass2);
        if (error != null) {
            regError.setText(error);
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setComunidad("Madrid");

        boolean registroCorrecto;
        try {
            registroCorrecto = dao.registrarUsuario(nuevoUsuario, pass);
            if (!registroCorrecto) {
                regError.setText("Error al crear la cuenta. Inténtalo de nuevo.");
                return;
            }
        } catch (RuntimeException ex) {
            String mensajeError = ex.getMessage();
            boolean esEmailDuplicado = "EMAIL_DUPLICADO".equals(mensajeError);
            if (esEmailDuplicado) {
                regError.setText("Ese email ya está registrado. Inicia sesión o usa otro.");
            } else {
                regError.setText("Error inesperado. Inténtalo de nuevo.");
            }
            return;
        }

        Session sesionRegistro = Session.getInstance();
        sesionRegistro.setUsuarioActual(nuevoUsuario);
        MainApp.irA("main");
    }

    @FXML
    public void doRegister() {
        hacerRegister();
    }
}
