package com.finanzapp.controller;

import com.finanzapp.dao.NotificacionDAO;
import com.finanzapp.dao.UsuarioDAO;
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
 * Controlador raíz de la vista principal de Fox Wallet.
 * <p>
 * Gestiona la barra de navegación lateral, la carga dinámica de paneles
 * en el área de contenido central, el toggle de tema claro/oscuro y el
 * badge de notificaciones no leídas. Actúa como hub de navegación entre
 * todos los módulos de la aplicación.
 */
public class MainController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private StackPane  contentArea;
    @FXML private Button navDashboard, navGastos, navSimulador,
                         navObjetivos, navHistorial, navRenta, notifBtn, avatarBtn, themeBtn;
    @FXML private Label  notifBadge;

    private final NotificacionDAO notifDAO  = new NotificacionDAO();
    private final UsuarioDAO      usuarioDAO = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Iniciales del usuario en el botón de avatar
        String ini = Session.getInstance().getUsuarioActual().getIniciales();
        avatarBtn.setText(ini);

        // Restaurar tema guardado del usuario
        String temaGuardado = Session.getInstance().getUsuarioActual().getTema();
        boolean dark = "oscuro".equals(temaGuardado);
        Session.getInstance().setDarkMode(dark);
        aplicarTemaVisual(dark);

        actualizarBadgeNotificaciones();
        showDashboard();
    }

    // ── Navegación ────────────────────────────────────────
    @FXML public void showDashboard()       { cargarPanelFxml("dashboard");       marcarNavegacionActiva(navDashboard); }
    @FXML public void showGastos()          { cargarPanelFxml("gastos");          marcarNavegacionActiva(navGastos); }
    @FXML public void showSimulador()       { cargarPanelFxml("simulador");       marcarNavegacionActiva(navSimulador); }
@FXML public void showObjetivos()       { cargarPanelFxml("objetivos");       marcarNavegacionActiva(navObjetivos); }
    @FXML public void showHistorial()       { cargarPanelFxml("historial");       marcarNavegacionActiva(navHistorial); }
    @FXML public void showRenta()           { cargarPanelFxml("renta");           marcarNavegacionActiva(navRenta); }
    @FXML public void showNotificaciones()  { cargarPanelFxml("notificaciones");  marcarNavegacionActiva(notifBtn);
                                              notifDAO.marcarTodasLasNotificacionesComoLeidas(Session.getInstance().getUsuarioActual().getId());
                                              notifBadge.setVisible(false); }
    @FXML public void showPerfil()          { cargarPanelFxml("perfil");          marcarNavegacionActiva(avatarBtn); }

    /** Carga un panel FXML en el área de contenido central de Fox Wallet. */
    private void cargarPanelFxml(String nombreFxml) {
        try {
            URL url = getClass().getResource("/com/finanzapp/fxml/" + nombreFxml + ".fxml");
            if (url == null) { showPlaceholder(nombreFxml); return; }
            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();
            // Pasa referencia al controlador raíz si el panel hijo lo necesita
            Object ctrl = loader.getController();
            if (ctrl instanceof ChildController) ((ChildController) ctrl).setMain(this);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder(nombreFxml);
        }
    }

    private void showPlaceholder(String nombreFxml) {
        Label lbl = new Label("Vista '" + nombreFxml + "' en construcción");
        lbl.setStyle("-fx-font-size:18px;-fx-text-fill:-color-text2;");
        contentArea.getChildren().setAll(lbl);
    }

    /** Marca el botón de navegación activo y desactiva el anterior. */
    private void marcarNavegacionActiva(Button botonActivo) {
        for (Button b : new Button[]{navDashboard, navGastos, navSimulador,
                                      navObjetivos, navHistorial, navRenta, notifBtn, avatarBtn}) {
            if (b != null) b.getStyleClass().remove("nav-tab-active");
        }
        if (botonActivo != null) botonActivo.getStyleClass().add("nav-tab-active");
    }

    // ── Modo oscuro ───────────────────────────────────────
    @FXML public void toggleDarkMode() {
        boolean dark = !Session.getInstance().isDarkMode();
        Session.getInstance().setDarkMode(dark);
        aplicarTemaVisual(dark);
        // Persistir preferencia de tema en la base de datos
        int uid = Session.getInstance().getUsuarioActual().getId();
        usuarioDAO.guardarPreferenciaTema(uid, dark ? "oscuro" : "claro");
    }

    /** Aplica el tema visual (claro/oscuro) a la escena actual de Fox Wallet. */
    private void aplicarTemaVisual(boolean modoOscuro) {
        if (modoOscuro) {
            if (!rootPane.getStyleClass().contains("dark"))
                rootPane.getStyleClass().add("dark");
            themeBtn.setText("☀️");
        } else {
            rootPane.getStyleClass().remove("dark");
            themeBtn.setText("🌙");
        }
    }

    /** Actualiza el badge de notificaciones no leídas en la barra de navegación. */
    public void actualizarBadgeNotificaciones() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        long count = notifDAO.contarNotificacionesNoLeidas(uid);
        notifBadge.setVisible(count > 0);
        notifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
    }

    /** Interfaz para paneles hijos que necesiten acceso al controlador principal. */
    public interface ChildController {
        void setMain(MainController main);
    }
}
