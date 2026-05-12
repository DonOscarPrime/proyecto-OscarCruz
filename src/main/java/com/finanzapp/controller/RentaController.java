package com.finanzapp.controller;

import com.finanzapp.model.RentaCalculo;
import com.finanzapp.model.Usuario;
import com.finanzapp.renderer.RentaRenderer;
import com.finanzapp.service.RentaPdfService;
import com.finanzapp.service.RentaService;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import com.finanzapp.validator.RentaValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controlador de la calculadora IRPF orientativa de Fox Wallet.
 */
public class RentaController implements Initializable {

    // ── Campos FXML ───────────────────────────────────────────────────────
    @FXML private TextField      salariotxt;
    @FXML private TextField      retencionestxt;
    @FXML private Spinner<Integer> contadorHijos;
    @FXML private Label          lblErrorRenta;
    @FXML private VBox           resultPanel;
    @FXML private VBox           tramosBox;
    @FXML private Label          lblBase;
    @FXML private Label          lblCuota;
    @FXML private Label          lblTipoEfectivo;
    @FXML private Label          lblRetenciones;
    @FXML private Label          lblResultado;
    @FXML private Label          lblPdfStatus;

    private final RentaService    rentaService   = new RentaService();
    private final RentaRenderer   rentaRenderer  = new RentaRenderer();
    private final RentaPdfService pdfService     = new RentaPdfService();
    private final RentaValidator  rentaValidator = new RentaValidator();

    private RentaCalculo ultimoCalculo;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario usuario = Session.getInstance().getUsuarioActual();
        double ingresosMensuales = usuario.getIngresosNetos();
        if (ingresosMensuales > 0) {
            double estimadoBruto = ingresosMensuales * 12 * 1.25;
            salariotxt.setText(String.format("%.0f", estimadoBruto));
        }
    }

    // ── Cálculo ───────────────────────────────────────────────────────────

    @FXML
    void calcular() {
        if (lblErrorRenta != null) lblErrorRenta.setText("");

        RentaCalculo calculo = new RentaCalculo();

        try {
            calculo.setSalarioBruto(rentaValidator.validarSalario(salariotxt.getText()));
        } catch (IllegalArgumentException e) {
            if (lblErrorRenta != null) lblErrorRenta.setText(e.getMessage());
            return;
        }

        calculo.setRetenciones(rentaValidator.parsearCampo(retencionestxt.getText()));
        calculo.setHijos(contadorHijos.getValue());

        String error = rentaValidator.validarFormulario(calculo);
        if (error != null) {
            if (lblErrorRenta != null) lblErrorRenta.setText(error);
            return;
        }

        rentaService.calcular(calculo);
        ultimoCalculo = calculo;

        actualizarResultadosUI(calculo);
        rentaRenderer.renderizarTramos(tramosBox, calculo);

        if (lblPdfStatus != null) lblPdfStatus.setText("");
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);
    }

    private void actualizarResultadosUI(RentaCalculo calculo) {
        lblBase.setText(Formateador.moneda(calculo.getBaseImponible()) + "€");
        lblCuota.setText(Formateador.moneda(calculo.getCuotaIntegra()) + "€");
        lblTipoEfectivo.setText(String.format("%.1f%%", calculo.getTipoEfectivo()));
        lblRetenciones.setText(Formateador.moneda(calculo.getRetenciones()) + "€");

        double resultado = calculo.getResultado();
        double abs = Math.abs(resultado);
        String prefijo = resultado >= 0 ? "A pagar: " : "A devolver: ";
        lblResultado.setText(prefijo + Formateador.moneda(abs) + "€");
        String color = resultado >= 0 ? "#D85A30" : "#1D9E75";
        lblResultado.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
    }

    // ── Exportar PDF ──────────────────────────────────────────────────────

    @FXML
    void exportarPDF() {
        if (ultimoCalculo == null) {
            if (lblPdfStatus != null) lblPdfStatus.setText("Realiza el cálculo antes de exportar.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar informe IRPF");
        fc.setInitialFileName("Estimacion_IRPF_" + LocalDate.now().getYear() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));

        File home = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        fc.setInitialDirectory(desktop.exists() ? desktop : home);

        Stage stage = (Stage) resultPanel.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        String nombreUsuario = Session.getInstance().getUsuarioActual().getNombre();
        try {
            pdfService.generarPDF(ultimoCalculo, nombreUsuario, file);
            lblPdfStatus.setStyle("-fx-text-fill:-color-accent;");
            lblPdfStatus.setText("PDF guardado en: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            lblPdfStatus.setStyle("-fx-text-fill:-color-danger;");
            lblPdfStatus.setText("Error al generar el PDF: " + e.getMessage());
        }
    }
}
