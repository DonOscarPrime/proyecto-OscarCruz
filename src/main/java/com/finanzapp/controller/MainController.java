package com.finanzapp.controller;

import com.finanzapp.dao.NotificacionDAO;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button navDashboard, navGastos, navSimulador, navPrestamos,
                         navObjetivos, navHistorial, navRenta, notifBtn, avatarBtn;
    @FXML private Label  notifBadge;

    private final NotificacionDAO notifDAO = new NotificacionDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Avatar initials
        String ini = Session.getInstance().getUsuarioActual().getIniciales();
        avatarBtn.setText(ini);
        refreshNotifBadge();
        showDashboard();
    }

    // ── Navigation ────────────────────────────────────────
    @FXML public void showDashboard()       { load("dashboard");       setActive(navDashboard); }
    @FXML public void showGastos()          { load("gastos");          setActive(navGastos); }
    @FXML public void showSimulador()       { load("simulador");       setActive(navSimulador); }
    @FXML public void showPrestamos()       { load("prestamos");       setActive(navPrestamos); }
    @FXML public void showObjetivos()       { load("objetivos");       setActive(navObjetivos); }
    @FXML public void showHistorial()       { load("historial");       setActive(navHistorial); }
    @FXML public void showRenta()           { load("renta");           setActive(navRenta); }
    @FXML public void showNotificaciones()  { load("notificaciones");  setActive(notifBtn);
                                              notifDAO.marcarTodasLeidas(Session.getInstance().getUsuarioActual().getId());
                                              notifBadge.setVisible(false); }
    @FXML public void showPerfil()          { load("perfil");          setActive(avatarBtn); }

    private void load(String name) {
        try {
            URL url = getClass().getResource("/com/finanzapp/fxml/" + name + ".fxml");
            if (url == null) { showPlaceholder(name); return; }
            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();
            // Pass reference to main controller if needed
            Object ctrl = loader.getController();
            if (ctrl instanceof ChildController) ((ChildController) ctrl).setMain(this);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder(name);
        }
    }

    private void showPlaceholder(String name) {
        Label lbl = new Label("Vista '" + name + "' en construcción");
        lbl.setStyle("-fx-font-size:18px;-fx-text-fill:#6B6A65;");
        contentArea.getChildren().setAll(lbl);
    }

    private void setActive(Button btn) {
        for (Button b : new Button[]{navDashboard, navGastos, navSimulador, navPrestamos,
                                      navObjetivos, navHistorial, navRenta, notifBtn, avatarBtn}) {
            if (b != null) b.getStyleClass().remove("nav-tab-active");
        }
        if (btn != null) btn.getStyleClass().add("nav-tab-active");
    }

    public void refreshNotifBadge() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        long count = notifDAO.contarNoLeidas(uid);
        notifBadge.setVisible(count > 0);
        notifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
    }

    /** Interface for child controllers that need a reference back to MainController. */
    public interface ChildController {
        void setMain(MainController main);
    }
}
