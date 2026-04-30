package com.finanzapp.controller;

import com.finanzapp.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Calculadora de la declaración de la Renta (IRPF) de Fox Wallet.
 * <p>
 * Implementa el cálculo del IRPF 2024 según el Manual Práctico AEAT:
 * <ul>
 *   <li>Deducción de cotizaciones SS antes de aplicar la reducción por rendimientos del trabajo.</li>
 *   <li>Reducción por rendimientos del trabajo aplicada sobre el rendimiento neto (no bruto).</li>
 *   <li>Mínimo personal y familiar aplicado íntegramente a cada escala (estatal y autonómica).</li>
 *   <li>Escalas autonómicas 2024 para 8 comunidades + régimen foral de País Vasco (Bizkaia).</li>
 *   <li>Deducciones en cuota: alquiler, vivienda habitual y maternidad.</li>
 * </ul>
 */
public class RentaController implements Initializable {

    // ── Campos FXML ───────────────────────────────────────────────────────
    @FXML private ComboBox<String>  cmbComunidad, cmbSituacion;
    @FXML private Spinner<Integer>  spinHijos, spinHijosMenores3;
    @FXML private TextField         txtSalario, txtCotizacionSS, txtRetenciones;
    @FXML private TextField         txtPlanPensiones, txtOtrosGastosTrabajo;
    @FXML private TextField         txtCapMob, txtCapInm, txtGastosInm, txtGanancias;
    @FXML private CheckBox          chkAlquiler, chkVivienda;
    @FXML private VBox              boxAlquiler, boxVivienda;
    @FXML private TextField         txtAlquilerAnual, txtViviendaAnual;
    @FXML private VBox              resultPanel, tramosBox;
    @FXML private Label             lblBase, lblCuota, lblDeducciones, lblRetenciones, lblResultado;
    @FXML private Label             lblPdfStatus;

    // ── Escala IRPF estatal 2024 (AEAT Manual Práctico IRPF 2024) ────────
    private static final double[][] TRAMOS_ESTATAL = {
        {0,       12450,  9.5},
        {12450,   20200,  12.0},
        {20200,   35200,  15.0},
        {35200,   60000,  18.5},
        {60000,   300000, 22.5},
        {300000,  Double.MAX_VALUE, 24.5}
    };

    /**
     * Escalas autonómicas 2024 (parte cedida del IRPF).
     * País Vasco usa régimen foral (Bizkaia): tarifa única que sustituye a estatal + autonómica.
     */
    private static final Map<String, double[][]> ESCALAS_AUTONOMICAS = new LinkedHashMap<>();
    private static final String PAIS_VASCO_FORAL = "País Vasco";

    static {
        // Madrid — Ley 6/2018 CCAA Madrid, vigente 2024
        ESCALAS_AUTONOMICAS.put("Madrid", new double[][]{
            {0,      12450,  9.0},
            {12450,  17707,  11.2},
            {17707,  33007,  13.3},
            {33007,  53407,  17.9},
            {53407,  Double.MAX_VALUE, 21.0}
        });
        // Cataluña — Llei 5/2020 + modificaciones, vigente 2024
        ESCALAS_AUTONOMICAS.put("Cataluña", new double[][]{
            {0,       12450,  10.5},
            {12450,   17707,  12.0},
            {17707,   21000,  14.0},
            {21000,   33007,  15.0},
            {33007,   53407,  19.0},
            {53407,   90000,  21.5},
            {90000,  120000,  23.5},
            {120000, 175000,  24.5},
            {175000, Double.MAX_VALUE, 25.5}
        });
        // Andalucía — Decreto-ley 7/2021 Junta de Andalucía, vigente 2024
        ESCALAS_AUTONOMICAS.put("Andalucía", new double[][]{
            {0,      12450,  9.5},
            {12450,  20200,  12.0},
            {20200,  28000,  14.0},
            {28000,  35200,  15.5},
            {35200,  50000,  16.5},
            {50000,  60000,  18.5},
            {60000,  Double.MAX_VALUE, 19.5}
        });
        // Comunitat Valenciana — Ley 13/1997 CV + modificaciones 2024
        ESCALAS_AUTONOMICAS.put("Valencia", new double[][]{
            {0,       12450,  10.0},
            {12450,   17707,  11.0},
            {17707,   33007,  13.9},
            {33007,   53407,  18.0},
            {53407,  120000,  23.5},
            {120000, 166000,  24.5},
            {166000, 200000,  25.5},
            {200000, Double.MAX_VALUE, 29.5}
        });
        // País Vasco — Tarifa foral Bizkaia 2024 (Norma Foral 13/2013 + mod.)
        // TARIFA COMPLETA: sustituye a estatal + autonómica del régimen común.
        ESCALAS_AUTONOMICAS.put("País Vasco", new double[][]{
            {0,       17423,  23.0},
            {17423,   33007,  28.0},
            {33007,   53407,  35.0},
            {53407,   90000,  40.0},
            {90000,  180000,  45.0},
            {180000, Double.MAX_VALUE, 49.0}
        });
        // Galicia — Lei 15/2010 autonómica, vigente 2024
        ESCALAS_AUTONOMICAS.put("Galicia", new double[][]{
            {0,      12450,  9.0},
            {12450,  20200,  11.65},
            {20200,  35200,  14.9},
            {35200,  60000,  18.5},
            {60000,  Double.MAX_VALUE, 22.5}
        });
        // Castilla y León — Decreto Legislativo 1/2013 CyL, vigente 2024
        ESCALAS_AUTONOMICAS.put("Castilla y León", new double[][]{
            {0,      12450,  9.0},
            {12450,  20200,  11.2},
            {20200,  35200,  14.0},
            {35200,  53407,  18.5},
            {53407,  Double.MAX_VALUE, 21.5}
        });
        // Otra — media aproximada régimen común (Aragón, Murcia, Extremadura…)
        ESCALAS_AUTONOMICAS.put("Otra", new double[][]{
            {0,      12450,  9.5},
            {12450,  20200,  12.0},
            {20200,  35200,  15.0},
            {35200,  60000,  18.5},
            {60000,  Double.MAX_VALUE, 22.5}
        });
    }

    // ── Estado del último cálculo (para el PDF) ───────────────────────────
    private double lastSalarioBruto, lastCotizacionSS, lastPlanPensiones;
    private double lastOtrosGastosTrabajo, lastGastosInm;
    private double lastRetenciones, lastCapMob, lastCapInm, lastGanancias;
    private double lastBaseGeneral, lastCuotaIntegra, lastTotalDeducciones, lastCuotaTotal, lastResultado;
    private double lastDedAlquiler, lastDedVivienda, lastDedMaternidad;
    private int    lastHijos, lastHijosMenores3;
    private double[][] lastTramosAut;
    private boolean    lastEsForal;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbComunidad.setItems(FXCollections.observableArrayList(
            "Madrid","Cataluña","Andalucía","Valencia","País Vasco","Galicia","Castilla y León","Otra"));
        String com = Session.getInstance().getUsuarioActual().getComunidad();
        cmbComunidad.setValue(com != null ? com : "Madrid");

        cmbSituacion.setItems(FXCollections.observableArrayList(
            "Soltero/a sin hijos","Casado/a sin hijos","Soltero/a con hijos","Casado/a con hijos","Viudo/a"));
        cmbSituacion.setValue("Soltero/a sin hijos");

        double ing = Session.getInstance().getUsuarioActual().getIngresosNetos();
        if (ing > 0) txtSalario.setText(String.format("%.0f", ing * 12 * 1.25));
    }

    // ── Handlers de UI ────────────────────────────────────────────────────

    /** Rellena automáticamente el campo SS con el 6,35 % del salario bruto. */
    @FXML void autoSS() {
        double bruto = parseD(txtSalario.getText());
        if (bruto > 0) txtCotizacionSS.setText(String.format("%.0f", bruto * 0.0635));
    }

    @FXML void toggleAlquiler() {
        boolean sel = chkAlquiler.isSelected();
        boxAlquiler.setVisible(sel);
        boxAlquiler.setManaged(sel);
    }

    @FXML void toggleVivienda() {
        boolean sel = chkVivienda.isSelected();
        boxVivienda.setVisible(sel);
        boxVivienda.setManaged(sel);
    }

    // ── Cálculo principal ─────────────────────────────────────────────────

    @FXML void calcular() {
        double salarioBruto = parseD(txtSalario.getText());
        double retenciones  = parseD(txtRetenciones.getText());
        double capMob       = parseD(txtCapMob.getText());
        double capInm       = parseD(txtCapInm.getText());
        double ganancias    = parseD(txtGanancias.getText());

        if (salarioBruto <= 0) return;

        // ── 1. Cotizaciones SS del trabajador ─────────────────────────────
        // Si el campo está vacío, se calcula automáticamente al 6,35 %:
        // 4,70 % contingencias comunes + 1,55 % desempleo + 0,10 % FP.
        double cotizacionSS = parseD(txtCotizacionSS.getText());
        if (cotizacionSS <= 0) cotizacionSS = salarioBruto * 0.0635;

        // ── 2. Plan de pensiones (reduce base imponible) ──────────────────
        // Límite legal 2024: el menor de 1.500 € o el 30 % de rendimientos netos.
        double planPensiones = parseD(txtPlanPensiones.getText());
        planPensiones = Math.min(planPensiones, Math.min(1500.0, salarioBruto * 0.30));

        // ── 3. Otros gastos deducibles del trabajo ────────────────────────
        double otrosGastosTrabajo = parseD(txtOtrosGastosTrabajo.getText());

        // ── 4. Rendimiento neto previo a la reducción ─────────────────────
        // Rendimiento íntegro − cotizaciones SS − otros gastos deducibles.
        double rendNetoPrevio = Math.max(0, salarioBruto - cotizacionSS - otrosGastosTrabajo);

        // ── 5. Reducción por rendimientos del trabajo (AEAT IRPF 2024) ────
        // Se aplica sobre el rendimiento neto, no sobre el bruto.
        // Tramos: ≤ 14.852 € → 6.498 €; 14.852–17.000 € → reducción lineal; > 17.000 € → 2.000 €.
        double reduccionTrabajo;
        if (rendNetoPrevio <= 14852)
            reduccionTrabajo = 6498;
        else if (rendNetoPrevio <= 17000)
            reduccionTrabajo = Math.max(2000, 6498 - 1.14 * (rendNetoPrevio - 14852));
        else
            reduccionTrabajo = 2000;

        // ── 6. Rendimiento neto del trabajo ───────────────────────────────
        double rendNetoTrabajo = Math.max(0, rendNetoPrevio - reduccionTrabajo);

        // ── 7. Capital inmobiliario neto ──────────────────────────────────
        double gastosInm  = parseD(txtGastosInm.getText());
        double capInmNeto = Math.max(0, capInm - gastosInm);

        // ── 8. Base imponible general ─────────────────────────────────────
        double baseGeneral = Math.max(0, rendNetoTrabajo - planPensiones + capInmNeto);
        double baseAhorro  = capMob + ganancias;

        // ── 9. Mínimo personal y familiar (AEAT 2024) ─────────────────────
        double minPersonal = 5550;
        int hijos = spinHijos.getValue();
        double minFamiliar = 0;
        if (hijos >= 1) minFamiliar += 2400;              // 1.er hijo
        if (hijos >= 2) minFamiliar += 2700;              // 2.º hijo
        if (hijos >= 3) minFamiliar += 4000;              // 3.er hijo
        if (hijos >= 4) minFamiliar += 4500 * (hijos - 3); // 4.º y siguientes: 4.500 € cada uno
        // Mínimo adicional para hijos menores de 3 años: 2.800 € por hijo
        int hijosMenores3  = spinHijosMenores3.getValue();
        double minTotal    = minPersonal + minFamiliar + (hijosMenores3 * 2800.0);

        // ── 10. Cuotas íntegras ───────────────────────────────────────────
        // El mínimo personal y familiar se aplica ÍNTEGRAMENTE a cada escala por separado
        // (no dividido a la mitad), conforme al método del Manual Práctico AEAT.
        String    comunidad = cmbComunidad.getValue();
        boolean   esForal   = PAIS_VASCO_FORAL.equals(comunidad);
        double[][] tramosAut = ESCALAS_AUTONOMICAS.getOrDefault(comunidad, ESCALAS_AUTONOMICAS.get("Otra"));

        double cuotaEstatal, cuotaAutonomica;
        if (esForal) {
            // Régimen foral Bizkaia: tarifa única, mínimo personal completo deducido sin división.
            cuotaEstatal    = Math.max(0, calcularCuota(baseGeneral, tramosAut) - calcularCuota(minTotal, tramosAut));
            cuotaAutonomica = 0;
        } else {
            // Régimen común: escala estatal y escala autonómica; mínimo aplicado a cada una íntegramente.
            cuotaEstatal    = Math.max(0, calcularCuota(baseGeneral, TRAMOS_ESTATAL) - calcularCuota(minTotal, TRAMOS_ESTATAL));
            cuotaAutonomica = Math.max(0, calcularCuota(baseGeneral, tramosAut)       - calcularCuota(minTotal, tramosAut));
        }

        double cuotaAhorro  = baseAhorro > 0 ? cuotaAhorro(baseAhorro) : 0;
        double cuotaIntegra = cuotaEstatal + cuotaAutonomica + cuotaAhorro;

        // ── 11. Deducciones sobre la cuota íntegra ────────────────────────

        // Deducción por alquiler de vivienda habitual (contratos anteriores al 01/01/2015)
        // Art. 68.7 LIRPF: 10,05 % de las cantidades pagadas, base máxima 9.040 €.
        double dedAlquiler = 0;
        if (chkAlquiler.isSelected()) {
            double cuotas = parseD(txtAlquilerAnual.getText());
            dedAlquiler = Math.min(cuotas, 9040.0) * 0.1005;
        }

        // Deducción por inversión en vivienda habitual (adquisición antes del 01/01/2013)
        // D.T. 18.ª LIRPF: 15 % del capital + intereses pagados, base máxima 9.040 €.
        double dedVivienda = 0;
        if (chkVivienda.isSelected()) {
            double cuotas = parseD(txtViviendaAnual.getText());
            dedVivienda = Math.min(cuotas, 9040.0) * 0.15;
        }

        // Deducción por maternidad: 1.200 €/año por hijo menor de 3 años (art. 81 LIRPF).
        double dedMaternidad = hijosMenores3 * 1200.0;

        double totalDeducciones = dedAlquiler + dedVivienda + dedMaternidad;
        double cuotaTotal       = Math.max(0, cuotaIntegra - totalDeducciones);
        double resultado        = cuotaTotal - retenciones;

        // ── 12. Guardar estado para el PDF ────────────────────────────────
        lastSalarioBruto       = salarioBruto;
        lastCotizacionSS       = cotizacionSS;
        lastPlanPensiones      = planPensiones;
        lastOtrosGastosTrabajo = otrosGastosTrabajo;
        lastGastosInm          = gastosInm;
        lastRetenciones        = retenciones;
        lastCapMob             = capMob;
        lastCapInm             = capInm;
        lastGanancias          = ganancias;
        lastBaseGeneral        = baseGeneral;
        lastCuotaIntegra       = cuotaIntegra;
        lastTotalDeducciones   = totalDeducciones;
        lastCuotaTotal         = cuotaTotal;
        lastResultado          = resultado;
        lastHijos              = hijos;
        lastHijosMenores3      = hijosMenores3;
        lastDedAlquiler        = dedAlquiler;
        lastDedVivienda        = dedVivienda;
        lastDedMaternidad      = dedMaternidad;
        lastTramosAut          = tramosAut;
        lastEsForal            = esForal;

        // ── 13. Actualizar panel de resultados ────────────────────────────
        lblBase.setText(fmt(baseGeneral) + "€");
        lblCuota.setText(fmt(cuotaIntegra) + "€");
        lblDeducciones.setText(totalDeducciones > 0 ? "-" + fmt(totalDeducciones) + "€" : "0€");
        lblRetenciones.setText(fmt(retenciones) + "€");
        lblResultado.setText((resultado >= 0 ? "A pagar: " : "A devolver: ") + fmt(Math.abs(resultado)) + "€");
        lblResultado.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" +
            (resultado >= 0 ? "#D85A30" : "#1D9E75") + ";");

        renderTramos(baseGeneral, tramosAut, esForal, comunidad);
        if (lblPdfStatus != null) lblPdfStatus.setText("");
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);
    }

    // ── Exportar PDF ──────────────────────────────────────────────────────

    @FXML void exportarPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar informe IRPF");
        fc.setInitialFileName("Informe_IRPF_" + LocalDate.now().getYear() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File home    = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        fc.setInitialDirectory(desktop.exists() ? desktop : home);

        Stage stage = (Stage) resultPanel.getScene().getWindow();
        File  file  = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            generarPDF(file);
            lblPdfStatus.setStyle("-fx-text-fill:-color-accent;");
            lblPdfStatus.setText("PDF guardado en: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            lblPdfStatus.setStyle("-fx-text-fill:-color-danger;");
            lblPdfStatus.setText("Error al generar el PDF: " + e.getMessage());
        }
    }

    private void generarPDF(File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float W  = page.getMediaBox().getWidth();   // 595
            float H  = page.getMediaBox().getHeight();  // 842
            float M  = 45f;
            float CW = W - 2 * M;

            PDType1Font bold    = PDType1Font.HELVETICA_BOLD;
            PDType1Font regular = PDType1Font.HELVETICA;
            PDType1Font oblique = PDType1Font.HELVETICA_OBLIQUE;

            String nombre    = asciify(Session.getInstance().getUsuarioActual().getNombre());
            String comunidad = asciify(cmbComunidad.getValue());
            String situacion = asciify(cmbSituacion.getValue());
            String fecha     = asciify(LocalDate.now().format(
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"))));

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── CABECERA ──────────────────────────────────────────────
                fillRect(cs, 0, H - 68, W, 68, 0.094f, 0.373f, 0.647f);
                fillRect(cs, 0, H - 68, 6, 68, 0.114f, 0.620f, 0.459f);
                drawText(cs, bold,    17, 1f, 1f, 1f, M, H - 32,
                    "Fox Wallet - Informe de Declaracion de la Renta");
                drawText(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 47,
                    "Estimacion orientativa del IRPF  |  Ejercicio " + LocalDate.now().getYear());
                drawText(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 60,
                    "Generado el " + fecha + "   |   " + nombre);

                float y = H - 84;

                // ── CONTRIBUYENTE ─────────────────────────────────────────
                drawSectionTitle(cs, bold, "CONTRIBUYENTE", M, y); y -= 16;
                fillRect(cs, M, y - 24, CW, 24, 0.969f, 0.965f, 0.953f);
                strokeRect(cs, M, y - 24, CW, 24, 0.878f, 0.867f, 0.847f, 0.5f);
                drawText(cs, bold,    9, 0.42f,0.42f,0.39f, M+8,   y-7,  "Nombre:");
                drawText(cs, regular, 9, 0.1f, 0.1f, 0.1f,  M+56,  y-7,  nombre);
                drawText(cs, bold,    9, 0.42f,0.42f,0.39f, M+220, y-7,  "Comunidad:");
                drawText(cs, regular, 9, 0.1f, 0.1f, 0.1f,  M+277, y-7,  comunidad);
                drawText(cs, bold,    9, 0.42f,0.42f,0.39f, M+8,   y-18, "Situacion:");
                drawText(cs, regular, 9, 0.1f, 0.1f, 0.1f,  M+62,  y-18, situacion);
                drawText(cs, bold,    9, 0.42f,0.42f,0.39f, M+220, y-18, "Hijos:");
                drawText(cs, regular, 9, 0.1f, 0.1f, 0.1f,  M+252, y-18,
                    lastHijos + "  (" + lastHijosMenores3 + " menores de 3 anos)");
                y -= 36;

                // ── DATOS INTRODUCIDOS ────────────────────────────────────
                drawSectionTitle(cs, bold, "DATOS INTRODUCIDOS", M, y); y -= 16;
                String[][] inputData = {
                    {"Salario bruto anual:",       fmt(lastSalarioBruto)        + " EUR"},
                    {"Cotizacion SS trabajador:",  fmt(lastCotizacionSS)        + " EUR"},
                    {"Plan de pensiones:",         fmt(lastPlanPensiones)       + " EUR"},
                    {"Otros gastos trabajo:",      fmt(lastOtrosGastosTrabajo)  + " EUR"},
                    {"Retenciones IRPF:",          fmt(lastRetenciones)         + " EUR"},
                    {"Cap. mobiliario:",           fmt(lastCapMob)              + " EUR"},
                    {"Cap. inmobiliario (bruto):", fmt(lastCapInm)              + " EUR"},
                    {"Gastos deducibles inmueble:",fmt(lastGastosInm)           + " EUR"},
                    {"Ganancias patrimoniales:",   fmt(lastGanancias)           + " EUR"},
                };
                float colW = (CW - 10) / 2;
                int rows   = (int) Math.ceil(inputData.length / 2.0); // 5
                for (int i = 0; i < inputData.length; i++) {
                    int   col  = i / rows;
                    int   rowI = i % rows;
                    float cx   = M + col * (colW + 10);
                    float rowY = y - rowI * 15;
                    if (rowI % 2 == 0) fillRect(cs, cx, rowY - 12, colW, 14, 0.969f,0.965f,0.953f);
                    drawText(cs, regular, 8.5f, 0.42f,0.42f,0.39f, cx+5,       rowY-6, inputData[i][0]);
                    drawTextRight(cs, bold, 8.5f, 0.1f,0.1f,0.1f, cx+colW-5,   rowY-6, inputData[i][1]);
                }
                y -= rows * 15 + 12;

                // ── RESULTADOS ────────────────────────────────────────────
                drawSectionTitle(cs, bold, "RESULTADO DE LA ESTIMACION", M, y); y -= 14;

                float boxW = (CW - 16) / 5;
                float boxH = 48;
                String[] boxLabels = {
                    "Base imponible general",
                    "Cuota integra estimada",
                    "Deducciones aplicadas",
                    "Retenciones practicadas",
                    lastResultado >= 0 ? "A PAGAR" : "A DEVOLVER"
                };
                String[] boxValues = {
                    fmt(lastBaseGeneral)             + " EUR",
                    fmt(lastCuotaIntegra)            + " EUR",
                    "-" + fmt(lastTotalDeducciones)  + " EUR",
                    fmt(lastRetenciones)             + " EUR",
                    fmt(Math.abs(lastResultado))     + " EUR"
                };
                float[][] bgColors = {
                    {0.635f, 0.831f, 0.949f},
                    {1.0f,   0.894f, 0.882f},
                    {0.882f, 0.969f, 0.933f},
                    {0.882f, 0.969f, 0.933f},
                    lastResultado >= 0
                        ? new float[]{1.0f, 0.894f, 0.882f}
                        : new float[]{0.882f, 0.969f, 0.933f}
                };
                for (int i = 0; i < 5; i++) {
                    float bx = M + i * (boxW + 4);
                    float by = y - boxH;
                    float[] rgb = bgColors[i];
                    fillRect(cs, bx, by, boxW, boxH, rgb[0], rgb[1], rgb[2]);
                    strokeRect(cs, bx, by, boxW, boxH, 0.87f,0.87f,0.87f, 0.5f);
                    drawText(cs, regular, 6.5f, 0.35f,0.35f,0.35f, bx+4, by+boxH-11, boxLabels[i]);
                    cs.setStrokingColor(0.87f,0.87f,0.87f); cs.setLineWidth(0.4f);
                    cs.moveTo(bx+4, by+boxH-15); cs.lineTo(bx+boxW-4, by+boxH-15); cs.stroke();
                    drawText(cs, bold, (i == 4 ? 11f : 9.5f), 0.1f,0.1f,0.1f, bx+4, by+9, boxValues[i]);
                }
                y -= boxH + 16;

                // ── TRAMOS IRPF ───────────────────────────────────────────
                float[] colX = {M+4, M+90, M+185, M+280, M+360, M+445};
                String[] tHdr = {"Tramo","Desde (EUR)","Hasta (EUR)","Tipo","Base aplicable","Cuota tramo"};

                if (lastEsForal) {
                    drawSectionTitle(cs, bold, "TARIFA FORAL IRPF — BIZKAIA (sustituye estatal + autonomico)", M, y);
                    y -= 14;
                    fillRect(cs, M, y-17, CW, 17, 0.349f,0.361f,0.961f);
                    for (int i = 0; i < tHdr.length; i++) drawText(cs, bold, 8, 1f,1f,1f, colX[i], y-12, tHdr[i]);
                    y -= 17;
                    y = dibujarFilasTramo(cs, regular, bold, y, lastBaseGeneral, lastTramosAut,
                        colX, CW, M, 0.54f,0.36f,0.96f);
                    y -= 6;
                    drawText(cs, oblique, 7f, 0.42f,0.42f,0.39f, M, y-8,
                        "Nota: Alava y Gipuzkoa tienen tarifas ligeramente distintas. Estimacion basada en Bizkaia.");
                    y -= 18;

                } else {
                    // Tabla estatal
                    drawSectionTitle(cs, bold, "TRAMOS IRPF ESTATAL APLICADOS", M, y); y -= 14;
                    fillRect(cs, M, y-17, CW, 17, 0.094f,0.373f,0.647f);
                    for (int i = 0; i < tHdr.length; i++) drawText(cs, bold, 8, 1f,1f,1f, colX[i], y-12, tHdr[i]);
                    y -= 17;
                    y = dibujarFilasTramo(cs, regular, bold, y, lastBaseGeneral, TRAMOS_ESTATAL,
                        colX, CW, M, 0.85f,0.35f,0.19f);
                    y -= 14;
                    // Tabla autonómica
                    drawSectionTitle(cs, bold, "TRAMOS IRPF AUTONOMICO — " +
                        asciify(cmbComunidad.getValue()).toUpperCase(), M, y); y -= 14;
                    fillRect(cs, M, y-17, CW, 17, 0.114f,0.620f,0.459f);
                    for (int i = 0; i < tHdr.length; i++) drawText(cs, bold, 8, 1f,1f,1f, colX[i], y-12, tHdr[i]);
                    y -= 17;
                    y = dibujarFilasTramo(cs, regular, bold, y, lastBaseGeneral, lastTramosAut,
                        colX, CW, M, 0.114f,0.620f,0.459f);
                    y -= 16;
                }

                // ── DEDUCCIONES EN CUOTA (solo si hay alguna) ─────────────
                if (lastTotalDeducciones > 0) {
                    drawSectionTitle(cs, bold, "DEDUCCIONES EN CUOTA APLICADAS", M, y); y -= 14;
                    // Cabecera de la tabla
                    fillRect(cs, M, y-14, CW, 14, 0.969f,0.965f,0.953f);
                    strokeRect(cs, M, y-14, CW, 14, 0.878f,0.867f,0.847f, 0.5f);
                    drawText(cs, bold, 8, 0.42f,0.42f,0.39f, M+6, y-9, "Concepto");
                    drawTextRight(cs, bold, 8, 0.42f,0.42f,0.39f, M+CW-6, y-9, "Importe (EUR)");
                    y -= 14;
                    int di = 0;
                    if (lastDedAlquiler > 0) {
                        if (di % 2 == 0) fillRect(cs, M, y-13, CW, 13, 0.985f,0.982f,0.976f);
                        drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, M+6, y-8,
                            "Deduccion por alquiler vivienda habitual (art. 68.7 LIRPF, 10,05 %)");
                        drawTextRight(cs, bold, 8.5f, 0.114f,0.620f,0.459f, M+CW-6, y-8, fmt(lastDedAlquiler));
                        y -= 13; di++;
                    }
                    if (lastDedVivienda > 0) {
                        if (di % 2 == 0) fillRect(cs, M, y-13, CW, 13, 0.985f,0.982f,0.976f);
                        drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, M+6, y-8,
                            "Deduccion por inversion en vivienda habitual (D.T. 18 LIRPF, 15 %)");
                        drawTextRight(cs, bold, 8.5f, 0.114f,0.620f,0.459f, M+CW-6, y-8, fmt(lastDedVivienda));
                        y -= 13; di++;
                    }
                    if (lastDedMaternidad > 0) {
                        if (di % 2 == 0) fillRect(cs, M, y-13, CW, 13, 0.985f,0.982f,0.976f);
                        drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, M+6, y-8,
                            "Deduccion por maternidad (art. 81 LIRPF, 1.200 EUR/hijo < 3 anos)");
                        drawTextRight(cs, bold, 8.5f, 0.114f,0.620f,0.459f, M+CW-6, y-8, fmt(lastDedMaternidad));
                        y -= 13; di++;
                    }
                    // Total deducciones
                    fillRect(cs, M, y-14, CW, 14, 0.882f,0.969f,0.933f);
                    strokeRect(cs, M, y-14, CW, 14, 0.878f,0.867f,0.847f, 0.5f);
                    drawText(cs, bold, 8.5f, 0.1f,0.1f,0.1f, M+6, y-9, "TOTAL DEDUCCIONES");
                    drawTextRight(cs, bold, 8.5f, 0.114f,0.620f,0.459f, M+CW-6, y-9, "-" + fmt(lastTotalDeducciones) + " EUR");
                    y -= 18;
                }

                // ── AVISO LEGAL ───────────────────────────────────────────
                if (y < 70) { /* podría desbordarse, pero dado el tamaño típico es suficiente */ }
                fillRect(cs, M, y-38, CW, 38, 0.992f,0.953f,0.875f);
                strokeRect(cs, M, y-38, CW, 38, 0.729f,0.525f,0.09f, 0.8f);
                drawText(cs, bold,    8.5f, 0.60f,0.43f,0.07f, M+7, y-11, "AVISO LEGAL");
                drawText(cs, oblique, 7.5f, 0.42f,0.42f,0.39f, M+7, y-22,
                    "Esta calculadora es orientativa y no tiene en cuenta todas las deducciones ni circunstancias personales.");
                drawText(cs, oblique, 7.5f, 0.42f,0.42f,0.39f, M+7, y-32,
                    "Consulta a un asesor fiscal o usa el programa oficial de la AEAT para tu declaracion definitiva.");

                // ── PIE ───────────────────────────────────────────────────
                cs.setStrokingColor(0.878f,0.867f,0.847f); cs.setLineWidth(0.5f);
                cs.moveTo(M, 24); cs.lineTo(W-M, 24); cs.stroke();
                drawText(cs, regular, 7.5f, 0.65f,0.65f,0.65f, M, 14,
                    "Fox Wallet - Informe generado el " + fecha + "  -  Pagina 1 / 1");
            }
            doc.save(file);
        }
    }

    /**
     * Dibuja las filas de datos de una tabla de tramos IRPF en el PDF.
     *
     * @param totalBase base imponible total (para calcular el tope de cada tramo)
     * @param cr,cg,cb  color de la columna de cuota por tramo
     * @return nueva posición y tras las filas dibujadas
     */
    private float dibujarFilasTramo(PDPageContentStream cs, PDType1Font regular, PDType1Font bold,
                                    float y, double totalBase, double[][] tramos, float[] colX,
                                    float CW, float M, float cr, float cg, float cb) throws IOException {
        double base    = totalBase;
        int tramoNum   = 1;
        for (double[] t : tramos) {
            if (base <= 0 || t[0] >= totalBase) break;
            double hasta  = Math.min(totalBase, t[1]);
            double aplic  = hasta - t[0];
            double cuotaT = aplic * (t[2] / 100.0);
            base -= aplic;
            if (tramoNum % 2 == 0) fillRect(cs, M, y-15, CW, 15, 0.969f,0.965f,0.953f);
            drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, colX[0], y-10, "Tramo " + tramoNum);
            drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, colX[1], y-10, fmt(t[0]));
            drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, colX[2], y-10, t[1] == Double.MAX_VALUE ? "Sin limite" : fmt(t[1]));
            drawText(cs, bold,    8.5f, 0.1f,0.1f,0.1f, colX[3], y-10, String.format("%.2f%%", t[2]));
            drawText(cs, regular, 8.5f, 0.1f,0.1f,0.1f, colX[4], y-10, fmt(aplic));
            drawText(cs, bold,    8.5f, cr, cg, cb,      colX[5], y-10, fmt(cuotaT));
            y -= 15;
            tramoNum++;
        }
        float tableH = 15 * (tramoNum - 1) + 17;
        cs.setStrokingColor(0.87f,0.87f,0.87f); cs.setLineWidth(0.5f);
        cs.addRect(M, y, CW, tableH); cs.stroke();
        return y;
    }

    // ── Helpers cálculo ───────────────────────────────────────────────────

    private double calcularCuota(double base, double[][] tramos) {
        double cuota = 0;
        for (double[] t : tramos) {
            if (base <= 0) break;
            double aplicable = Math.min(base, t[1] - t[0]);
            cuota += aplicable * (t[2] / 100.0);
            base  -= aplicable;
        }
        return cuota;
    }

    /** Cuota sobre la base del ahorro (tarifa del ahorro IRPF 2024). */
    private double cuotaAhorro(double base) {
        double[][] tramos = {{0,6000,19},{6000,50000,21},{50000,200000,23},{200000,Double.MAX_VALUE,27}};
        double cuota = 0;
        for (double[] t : tramos) {
            if (base <= 0) break;
            double ap = Math.min(base, t[1] - t[0]);
            cuota += ap * (t[2] / 100.0);
            base  -= ap;
        }
        return cuota;
    }

    private void renderTramos(double base, double[][] tramosAut, boolean esForal, String comunidad) {
        tramosBox.getChildren().clear();
        if (esForal) {
            Label hdr = new Label("TARIFA FORAL — " + comunidad.toUpperCase() + " (Bizkaia, ref.)");
            hdr.setStyle("-fx-font-size:10px;-fx-font-weight:600;-fx-text-fill:-color-text3;-fx-padding:4 0 2 0;");
            tramosBox.getChildren().add(hdr);
            agregarFilasTramo(base, tramosAut, "#8B5CF6");
            Label nota = new Label("ℹ País Vasco y Navarra tienen régimen foral propio (Concierto/Convenio Económico). " +
                "Esta estimación usa la tarifa de Bizkaia; Álava y Gipuzkoa tienen leves variaciones.");
            nota.setWrapText(true);
            nota.setStyle("-fx-font-size:11px;-fx-text-fill:-color-text3;-fx-padding:6 0 0 0;");
            tramosBox.getChildren().add(nota);
        } else {
            Label hdrE = new Label("TRAMO ESTATAL");
            hdrE.setStyle("-fx-font-size:10px;-fx-font-weight:600;-fx-text-fill:-color-text3;-fx-padding:4 0 2 0;");
            tramosBox.getChildren().add(hdrE);
            agregarFilasTramo(base, TRAMOS_ESTATAL, "#185FA5");

            Label hdrA = new Label("TRAMO AUTONÓMICO — " + comunidad.toUpperCase());
            hdrA.setStyle("-fx-font-size:10px;-fx-font-weight:600;-fx-text-fill:-color-text3;-fx-padding:8 0 2 0;");
            tramosBox.getChildren().add(hdrA);
            agregarFilasTramo(base, tramosAut, "#1D9E75");
        }
    }

    private void agregarFilasTramo(double base, double[][] tramos, String colorCuota) {
        for (double[] t : tramos) {
            if (base <= 0 || t[0] >= base) break;
            double desde     = t[0];
            double hasta     = Math.min(base, t[1]);
            double aplicable = hasta - desde;
            double cuota     = aplicable * (t[2] / 100.0);

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 8, 5, 8));
            row.setStyle("-fx-background-color:-color-surface2;-fx-background-radius:6;");

            Label rango = new Label(fmt(desde) + "€ – " + (t[1] == Double.MAX_VALUE ? "+" : fmt(t[1]) + "€"));
            rango.setStyle("-fx-font-size:12px;-fx-text-fill:-color-text2;");
            HBox.setHgrow(rango, Priority.ALWAYS);

            Label tipo = new Label(String.format("%.2f%%", t[2]));
            tipo.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:-color-text;");

            Label cuotaLbl = new Label(fmt(cuota) + "€");
            cuotaLbl.setStyle("-fx-font-size:12px;-fx-font-family:monospace;-fx-text-fill:" + colorCuota + ";");

            row.getChildren().addAll(rango, tipo, cuotaLbl);
            tramosBox.getChildren().add(row);
        }
    }

    // ── Helpers PDF ───────────────────────────────────────────────────────

    private void drawText(PDPageContentStream cs, PDType1Font font, float size,
                          float r, float g, float b, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(r, g, b);
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawTextRight(PDPageContentStream cs, PDType1Font font, float size,
                               float r, float g, float b, float xRight, float y, String text) throws IOException {
        float tw = font.getStringWidth(text) / 1000 * size;
        drawText(cs, font, size, r, g, b, xRight - tw, y, text);
    }

    private void fillRect(PDPageContentStream cs, float x, float y, float w, float h,
                          float r, float g, float b) throws IOException {
        cs.setNonStrokingColor(r, g, b);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void strokeRect(PDPageContentStream cs, float x, float y, float w, float h,
                             float r, float g, float b, float lw) throws IOException {
        cs.setStrokingColor(r, g, b);
        cs.setLineWidth(lw);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private void drawSectionTitle(PDPageContentStream cs, PDType1Font bold,
                                  String title, float x, float y) throws IOException {
        cs.setNonStrokingColor(0.114f, 0.620f, 0.459f);
        cs.addRect(x, y - 3, 3, 12);
        cs.fill();
        drawText(cs, bold, 8, 0.42f, 0.42f, 0.39f, x + 8, y, title);
    }

    /** Sustituye caracteres fuera de WinAnsiEncoding por equivalentes ASCII seguros. */
    private String asciify(String s) {
        if (s == null) return "";
        return s
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u")
            .replace("Á","A").replace("É","E").replace("Í","I")
            .replace("Ó","O").replace("Ú","U")
            .replace("ñ","n").replace("Ñ","N")
            .replace("ü","u").replace("Ü","U")
            .replace("—","-").replace("–","-")
            .replace("·",".").replace("¿","?").replace("¡","!")
            .replace("€","EUR").replace("⚠","(!)")
            .replace("’","'").replace("“","\"").replace("”","\"")
            .replaceAll("[^ -ÿ]","?");
    }

    // ── Helpers de formato ────────────────────────────────────────────────

    private double parseD(String s) {
        try { return Double.parseDouble(s.trim().replace(",",".")); }
        catch (Exception e) { return 0; }
    }

    private String fmt(double v) {
        return String.format("%,.0f", v).replace(",",".");
    }
}
