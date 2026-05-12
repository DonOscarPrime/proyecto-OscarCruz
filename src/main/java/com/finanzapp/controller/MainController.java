package com.finanzapp.controller;

import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador raíz de la vista principal.
 * Gestiona la barra de navegación, la carga dinámica de paneles ,
 * el toggle de tema claro/oscuro.
 */
public class MainController implements Initializable {

    @FXML private BorderPane panelRoot;
    @FXML private StackPane  areaDeContenido;
    @FXML private Button navegaDashboard;
    @FXML private Button navegaGastos;
    @FXML private Button navegaSimulador;
    @FXML private Button navegaObjetivos;
    @FXML private Button navegaHistorial;
    @FXML private Button navegaRenta;
    @FXML private Button notBtn;
    @FXML private Button avatarBtn;
    @FXML private Button temaBtn;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Session sesionInit = Session.getInstance();
        Usuario usuarioInit = sesionInit.getUsuarioActual();
        String iniciales = usuarioInit.getIniciales();
        avatarBtn.setText(iniciales);

        String temaGuardado = usuarioInit.getTema();
        boolean modoOscuro = "oscuro".equals(temaGuardado);
        sesionInit.setDarkMode(modoOscuro);
        aplicarVisual(modoOscuro);

        showDashboard();
    }
    @FXML
    public void mostrarDashboard() {
        cargar("dashboard");
        marcarNavegacionActiva(navegaDashboard);
    }

    @FXML
    public void showDashboard() {
        mostrarDashboard();
    }

    @FXML
    public void mostrarGasto() {
        cargar("gastos");
        marcarNavegacionActiva(navegaGastos);
    }

    @FXML
    public void showGastos() {
        mostrarGasto();
    }

    @FXML
    public void mostrarSimulador() {
        cargar("simulador");
        marcarNavegacionActiva(navegaSimulador);
    }

    @FXML
    public void showSimulador() {
        mostrarSimulador();
    }

    @FXML
    public void mostrarObjetivos() {
        cargar("objetivos");
        marcarNavegacionActiva(navegaObjetivos);
    }

    @FXML
    public void showObjetivos() {
        mostrarObjetivos();
    }

    @FXML
    public void mostrarHistorial() {
        cargar("historial");
        marcarNavegacionActiva(navegaHistorial);
    }

    @FXML
    public void showHistorial() {
        mostrarHistorial();
    }

    @FXML
    public void mostrarRenta() {
        cargar("renta");
        marcarNavegacionActiva(navegaRenta);
    }

    @FXML
    public void showRenta() {
        mostrarRenta();
    }

    @FXML
    public void mostrarNotificaciones() {
        cargar("notificaciones");
        marcarNavegacionActiva(notBtn);
    }

    @FXML
    public void showNotificaciones() {
        mostrarNotificaciones();
    }

    @FXML
    public void mostrarPerfil() {
        cargar("perfil");
        marcarNavegacionActiva(avatarBtn);
    }

    @FXML
    public void showPerfil() {
        mostrarPerfil();
    }

    /** Carga un panel FXML en el área de contenido central de Fox Wallet. */
    private void cargar(String nombreFxml) {
        try {
            String rutaFxml = "/com/finanzapp/fxml/" + nombreFxml + ".fxml";
            URL url = getClass().getResource(rutaFxml);

            if (url == null) {
                showPlaceholder(nombreFxml);
                return;
            }

            FXMLLoader cargador = new FXMLLoader(url);
            Node node = cargador.load();

            Object controlador = cargador.getController();
            if (controlador instanceof ControladorSecundario) {
                ControladorSecundario contieneOscuro = (ControladorSecundario) controlador;
                contieneOscuro.setMain(this);
            }

            areaDeContenido.getChildren().setAll(node);

        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder(nombreFxml);
        }
    }

    private void showPlaceholder(String nombreFxml) {
        String parteInicio = "Vista '";
        String parteFin = "' en construcción";
        String textoPlaceholder = parteInicio + nombreFxml + parteFin;
        Label etiqueta = new Label(textoPlaceholder);
        etiqueta.setStyle("-fx-font-size:18px;-fx-text-fill:-color-text2;");
        areaDeContenido.getChildren().setAll(etiqueta);
    }

    private void marcarNavegacionActiva(Button botonActivo) {
        Button[] todosLosBotones = {
            navegaDashboard, navegaGastos, navegaSimulador,
            navegaObjetivos, navegaHistorial, navegaRenta, notBtn, avatarBtn
        };

        for (Button boton : todosLosBotones) {
            if (boton != null) {
                boton.getStyleClass().remove("nav-tab-active");
            }
        }

        if (botonActivo != null) {
            botonActivo.getStyleClass().add("nav-tab-active");
        }
    }

    //Modo oscuro

    @FXML
    public void toggleModoOscuro() {
        Session sesionTema = Session.getInstance();
        boolean modoOscuroActual = sesionTema.isDarkMode();
        boolean nuevoModo = !modoOscuroActual;

        sesionTema.setDarkMode(nuevoModo);
        aplicarVisual(nuevoModo);

        Usuario usuarioTema = sesionTema.getUsuarioActual();
        int idUsuario = usuarioTema.getId();

        if (nuevoModo) {
            usuarioDAO.guardarPreferencia(idUsuario, "oscuro");
        } else {
            usuarioDAO.guardarPreferencia(idUsuario, "claro");
        }
    }

    @FXML
    public void toggleDarkMode() {
        toggleModoOscuro();
    }

    /** Aplica el claro/oscuro. */
    private void aplicarVisual(boolean modoOscuro) {
        if (modoOscuro) {
            boolean yaContiene = panelRoot.getStyleClass().contains("dark");
            if (!yaContiene) {
                panelRoot.getStyleClass().add("dark");
            }
            temaBtn.setText("☀️");
        } else {
            panelRoot.getStyleClass().remove("dark");
            temaBtn.setText("🌙");
        }
    }

    public interface ControladorSecundario {
        void setMain(MainController main);
    }
}
