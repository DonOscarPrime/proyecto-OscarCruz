package com.finanzapp.controller;

import com.finanzapp.dao.HabitoDAO;
import com.finanzapp.model.Habito;
import com.finanzapp.renderer.HabitoRenderer;
import com.finanzapp.service.HabitoService;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del simulador de ahorro de hábitos de Fox Wallet.
 Se encarga de los cálculos y la visualización de resultados.
 */
public class SimuladorController implements Initializable {

    @FXML private Label lblActual;
    @FXML private Label txtObj;
    @FXML private Label lblMes;
    @FXML private Label lblAnio;
    @FXML private Label Tip;
    @FXML private VBox     formPanel;
    @FXML private FlowPane habitosGrid;
    @FXML private TextField Emoji;
    @FXML private TextField Nombre;
    @FXML private TextField Coste;
    @FXML private TextField FrecuenciaAhora;
    @FXML private TextField frecuenciaObjetivo;
    @FXML private ComboBox<String> selectUnidad;

    private final HabitoDAO      habitoDAO      = new HabitoDAO();
    private final HabitoService  habitoService  = new HabitoService(habitoDAO);
    private final HabitoRenderer habitoRenderer = new HabitoRenderer();

    private List<Habito> habitos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectUnidad.setItems(FXCollections.observableArrayList("semana", "mes"));
        selectUnidad.setValue("semana");
        cargarHabitosResumen();
    }

    // ── Carga y resumen ───────────────────────────────────────────────────

    private void cargarHabitosResumen() {
        Session sesionActual = Session.getInstance();
        com.finanzapp.model.Usuario usuarioEnSesion = sesionActual.getUsuarioActual();
        int uid = usuarioEnSesion.getId();
        habitos = habitoService.obtenerDeUsuario(uid);
        actualizarResumen();
        MostrarTarjetas();
    }

    private void actualizarResumen() {
        double gastoActual   = habitoService.calcularGastoMensualTotal(habitos);
        double gastoObjetivo = habitoService.calcularObjetivoMensualTotal(habitos);
        double ahorroMes     = habitoService.calcularAhorroMensual(gastoActual, gastoObjetivo);
        double ahorroAnio    = habitoService.calcularAhorroAnual(ahorroMes);

        String gastoActualFormateado = Formateador.moneda(gastoActual);
        lblActual.setText(gastoActualFormateado + "€/mes");
        String gastoObjetivoFormateado = Formateador.moneda(gastoObjetivo);
        txtObj.setText(gastoObjetivoFormateado + "€/mes");
        String ahorroMesFormateado = Formateador.moneda(ahorroMes);
        lblMes.setText(ahorroMesFormateado + "€");
        String ahorroAnioFormateado = Formateador.moneda(ahorroAnio);
        lblAnio.setText(ahorroAnioFormateado + "€");

        if (ahorroMes > 0) {
            String ahorroMesTip = Formateador.moneda(ahorroMes);
            String ahorroAnioTip = Formateador.moneda(ahorroAnio);
            String parteTipInicio = "💡 Podrías ahorrar " + ahorroMesTip;
            String parteTipFin = "€ al mes (" + ahorroAnioTip + "€/año) reduciendo tus hábitos según los objetivos.";
            String textoTip = parteTipInicio + parteTipFin;
            Tip.setText(textoTip);
            Tip.setStyle("-fx-text-fill:#1D9E75;-fx-font-weight:600;");
        } else {
            Tip.setText("Ajusta las frecuencias de tus hábitos para ver cómo cambia tu ahorro mensual.");
            Tip.setStyle("-fx-text-fill:-color-text2;");
        }
    }

    // ── Renderizado de tarjetas ───────────────────────────────────────────

    private void MostrarTarjetas() {
        habitosGrid.getChildren().clear();

        for (Habito habi : habitos) {
            VBox tarjeta = habitoRenderer.crearTarjeta(
                habi,
                nuevaFreq -> {
                    habi.setFrecuenciaActual(nuevaFreq);
                    habitoService.actualizarFrecuencias(habi);
                    actualizarResumen();
                },
                nuevaFreqObj -> {
                    habi.setFrecuenciaObj(nuevaFreqObj);
                    habitoService.actualizarFrecuencias(habi);
                    actualizarResumen();
                },
                () -> {
                    habitoService.eliminar(habi.getId());
                    cargarHabitosResumen();
                }
            );
            habitosGrid.getChildren().add(tarjeta);
        }
    }

    // ── Formulario de nuevo hábito ────────────────────────────────────────

    @FXML
    void toggleForm() {
        boolean eraVisible   = formPanel.isVisible();
        boolean nuevaVisible = !eraVisible;
        formPanel.setVisible(nuevaVisible);
        formPanel.setManaged(nuevaVisible);
    }

    @FXML
    void guardarHabito() {
        String nombreBruto = Nombre.getText();
        String nombre = nombreBruto.trim();
        String costeBruto = Coste.getText();
        String costeSinEspacios = costeBruto.trim();
        String costeStr = costeSinEspacios.replace(",", ".");

        if (nombre.isEmpty() || costeStr.isEmpty()) {
            return;
        }

        double coste;
        try {
            coste = Double.parseDouble(costeStr);
        } catch (NumberFormatException ex) {
            return;
        }

        Habito habi = new Habito();
        Session sesionGuardar = Session.getInstance();
        com.finanzapp.model.Usuario usuarioGuardar = sesionGuardar.getUsuarioActual();
        int uidGuardar = usuarioGuardar.getId();
        habi.setUsuarioId(uidGuardar);

        String textoEmoji = Emoji.getText();
        if (textoEmoji.isBlank()) {
            habi.setEmoji("💡");
        } else {
            String textoEmojiLimpio = textoEmoji.trim();
            habi.setEmoji(textoEmojiLimpio);
        }

        habi.setNombre(nombre);
        habi.setCoste(coste);

        String textoFreqActual = FrecuenciaAhora.getText();
        if (textoFreqActual.isBlank()) {
            habi.setFrecuenciaActual(0);
        } else {
            try {
                String textoFreqActualLimpio = textoFreqActual.trim();
                int frecuenciaActualParseada = Integer.parseInt(textoFreqActualLimpio);
                habi.setFrecuenciaActual(frecuenciaActualParseada);
            } catch (NumberFormatException ex) {
                habi.setFrecuenciaActual(0);
            }
        }

        String textoFreqObj = frecuenciaObjetivo.getText();
        if (textoFreqObj.isBlank()) {
            habi.setFrecuenciaObj(0);
        } else {
            try {
                String textoFreqObjLimpio = textoFreqObj.trim();
                int frecuenciaObjParseada = Integer.parseInt(textoFreqObjLimpio);
                habi.setFrecuenciaObj(frecuenciaObjParseada);
            } catch (NumberFormatException ex) {
                habi.setFrecuenciaObj(0);
            }
        }

        habi.setUnidad(selectUnidad.getValue());
        habi.setDescripcion("por vez");

        habitoService.registrar(habi);
        toggleForm();
        Emoji.clear();
        Nombre.clear();
        Coste.clear();
        FrecuenciaAhora.clear();
        frecuenciaObjetivo.clear();
        cargarHabitosResumen();
    }
}
