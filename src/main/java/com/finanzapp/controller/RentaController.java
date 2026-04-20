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

public class RentaController implements Initializable {

    @FXML private ComboBox<String> cmbComunidad, cmbSituacion;
    @FXML private Spinner<Integer> spinHijos;
    @FXML private TextField txtSalario, txtRetenciones, txtCapMob, txtCapInm, txtGanancias;
    @FXML private VBox  resultPanel, tramosBox;
    @FXML private Label lblBase, lblCuota, lblRetenciones, lblResultado;
    @FXML private Label lblPdfStatus;

    // Tramos IRPF estatal 2024 (general)
    private static final double[][] TRAMOS_ESTATAL = {
        {0,      12450,  9.5},
        {12450,  20200,  12.0},
        {20200,  35200,  15.0},
        {35200,  60000,  18.5},
        {60000,  300000, 22.5},
        {300000, Double.MAX_VALUE, 24.5}
    };

    // Tramos autonómicos simplificados (Madrid)
    private static final double[][] TRAMOS_MADRID = {
        {0,      12450,  9.0},
        {12450,  17707,  11.2},
        {17707,  33007,  13.3},
        {33007,  53407,  17.9},
        {53407,  Double.MAX_VALUE, 21.0}
    };

    // Últimos valores calculados (para el PDF)
    private double lastSalarioBruto, lastRetenciones, lastCapMob, lastCapInm, lastGanancias;
    private double lastBaseGeneral, lastCuotaTotal, lastResultado;
    private int    lastHijos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbComunidad.setItems(FXCollections.observableArrayList(
            "Madrid","Cataluña","Andalucía","Valencia","País Vasco","Galicia","Castilla y León","Otra"));
        String com = Session.getInstance().getUsuarioActual().getComunidad();
        cmbComunidad.setValue(com != null ? com : "Madrid");

        cmbSituacion.setItems(FXCollections.observableArrayList(
            "Soltero/a sin hijos","Casado/a sin hijos","Soltero/a con hijos","Casado/a con hijos","Viudo/a"));
        cmbSituacion.setValue("Soltero/a sin hijos");

        // Pre-fill salary from user profile
        double ing = Session.getInstance().getUsuarioActual().getIngresosNetos();
        if (ing > 0) txtSalario.setText(String.format("%.0f", ing * 12 * 1.25)); // approx bruto
    }

    @FXML void calcular() {
        double salarioBruto = parseD(txtSalario.getText());
        double retenciones  = parseD(txtRetenciones.getText());
        double capMob       = parseD(txtCapMob.getText());
        double capInm       = parseD(txtCapInm.getText());
        double ganancias    = parseD(txtGanancias.getText());

        if (salarioBruto <= 0) return;

        // Reducción por rendimientos del trabajo (simplificada)
        double reduccionTrabajo;
        if (salarioBruto <= 14852)       reduccionTrabajo = 6498;
        else if (salarioBruto <= 17000)  reduccionTrabajo = 6498 - 1.14 * (salarioBruto - 14852);
        else                             reduccionTrabajo = 2000;

        double rendNeto    = Math.max(0, salarioBruto - reduccionTrabajo);
        double baseGeneral = rendNeto + capInm;
        double baseAhorro  = capMob + ganancias;

        // Mínimo personal y familiar
        double minPersonal = 5550;
        int hijos = spinHijos.getValue();
        double minFamiliar = 0;
        if (hijos == 1) minFamiliar = 2400;
        else if (hijos == 2) minFamiliar = 2400 + 2700;
        else if (hijos >= 3) minFamiliar = 2400 + 2700 + 4000;
        double minTotal = minPersonal + minFamiliar;

        double cuotaEstatal    = calcularCuota(baseGeneral, TRAMOS_ESTATAL) - calcularCuota(minTotal, TRAMOS_ESTATAL) * 0.5;
        double cuotaAutonomica = calcularCuota(baseGeneral, TRAMOS_MADRID)  - calcularCuota(minTotal, TRAMOS_MADRID)  * 0.5;
        double cuotaAhorro     = baseAhorro > 0 ? cuotaAhorro(baseAhorro) : 0;
        double cuotaTotal      = Math.max(0, cuotaEstatal + cuotaAutonomica + cuotaAhorro);
        double resultado       = cuotaTotal - retenciones;

        // Guardar para el PDF
        lastSalarioBruto = salarioBruto;
        lastRetenciones  = retenciones;
        lastCapMob       = capMob;
        lastCapInm       = capInm;
        lastGanancias    = ganancias;
        lastBaseGeneral  = baseGeneral;
        lastCuotaTotal   = cuotaTotal;
        lastResultado    = resultado;
        lastHijos        = hijos;

        lblBase.setText(fmt(baseGeneral) + "€");
        lblCuota.setText(fmt(cuotaTotal) + "€");
        lblRetenciones.setText(fmt(retenciones) + "€");
        lblResultado.setText((resultado >= 0 ? "A pagar: " : "A devolver: ") + fmt(Math.abs(resultado)) + "€");
        lblResultado.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + (resultado >= 0 ? "#D85A30" : "#1D9E75") + ";");

        renderTramos(baseGeneral);
        if (lblPdfStatus != null) lblPdfStatus.setText("");
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);
    }

    // ── Exportar PDF ─────────────────────────────────────────────

    @FXML void exportarPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar informe IRPF");
        fc.setInitialFileName("Informe_IRPF_" + LocalDate.now().getYear() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));

        // Directorio inicial: escritorio o home
        File home = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        fc.setInitialDirectory(desktop.exists() ? desktop : home);

        Stage stage = (Stage) resultPanel.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
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

            // Solo ASCII + Latin-1 para compatibilidad con PDType1Font/WinAnsiEncoding
            String nombre    = asciify(Session.getInstance().getUsuarioActual().getNombre());
            String comunidad = asciify(cmbComunidad.getValue());
            String situacion = asciify(cmbSituacion.getValue());
            String fecha     = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es")));
            fecha = asciify(fecha);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── CABECERA ──────────────────────────────────────────
                fillRect(cs, 0, H - 68, W, 68, 0.094f, 0.373f, 0.647f);
                fillRect(cs, 0, H - 68, 6, 68, 0.114f, 0.620f, 0.459f);

                drawText(cs, bold,    17, 1f, 1f, 1f, M, H - 32,
                        "FinanzApp - Informe de Declaracion de la Renta");
                drawText(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 47,
                        "Estimacion orientativa del IRPF  |  Ejercicio " + LocalDate.now().getYear());
                drawText(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 60,
                        "Generado el " + fecha + "   |   " + nombre);

                float y = H - 84;

                // ── CONTRIBUYENTE ─────────────────────────────────────
                drawSectionTitle(cs, bold, "CONTRIBUYENTE", M, y);
                y -= 16;
                fillRect(cs, M, y - 24, CW, 24, 0.969f, 0.965f, 0.953f);
                strokeRect(cs, M, y - 24, CW, 24, 0.878f, 0.867f, 0.847f, 0.5f);
                drawText(cs, bold,    9, 0.42f, 0.42f, 0.39f, M + 8,   y - 7,  "Nombre:");
                drawText(cs, regular, 9, 0.1f,  0.1f,  0.1f,  M + 56,  y - 7,  nombre);
                drawText(cs, bold,    9, 0.42f, 0.42f, 0.39f, M + 220,  y - 7,  "Comunidad:");
                drawText(cs, regular, 9, 0.1f,  0.1f,  0.1f,  M + 277,  y - 7,  comunidad);
                drawText(cs, bold,    9, 0.42f, 0.42f, 0.39f, M + 8,   y - 18, "Situacion:");
                drawText(cs, regular, 9, 0.1f,  0.1f,  0.1f,  M + 62,  y - 18, situacion);
                drawText(cs, bold,    9, 0.42f, 0.42f, 0.39f, M + 220,  y - 18, "Hijos:");
                drawText(cs, regular, 9, 0.1f,  0.1f,  0.1f,  M + 252,  y - 18, String.valueOf(lastHijos));
                y -= 36;

                // ── DATOS INTRODUCIDOS ────────────────────────────────
                drawSectionTitle(cs, bold, "DATOS INTRODUCIDOS", M, y);
                y -= 16;

                String[][] inputData = {
                    {"Salario bruto anual:",     fmt(lastSalarioBruto) + " EUR"},
                    {"Retenciones practicadas:", fmt(lastRetenciones)  + " EUR"},
                    {"Cap. mobiliario:",         fmt(lastCapMob)       + " EUR"},
                    {"Cap. inmobiliario:",       fmt(lastCapInm)       + " EUR"},
                    {"Ganancias patrimoniales:", fmt(lastGanancias)    + " EUR"},
                };

                float colW = (CW - 10) / 2;
                for (int i = 0; i < inputData.length; i++) {
                    int col   = i / 3;           // 0 = izquierda, 1 = derecha
                    int rowIdx = i % 3;
                    float cx   = M + col * (colW + 10);
                    float rowY = y - rowIdx * 17;

                    if (i % 2 == 0) fillRect(cs, cx, rowY - 13, colW, 15, 0.969f, 0.965f, 0.953f);
                    drawText(cs, regular, 9, 0.42f, 0.42f, 0.39f, cx + 6,        rowY - 7, inputData[i][0]);
                    drawTextRight(cs, bold, 9, 0.1f, 0.1f, 0.1f, cx + colW - 6,  rowY - 7, inputData[i][1]);
                }
                y -= 3 * 17 + 12;

                // ── RESULTADOS ────────────────────────────────────────
                drawSectionTitle(cs, bold, "RESULTADO DE LA ESTIMACION", M, y);
                y -= 14;

                float boxW = (CW - 12) / 4;
                float boxH = 50;
                float[][] bgColors = {
                    {0.635f, 0.831f, 0.949f},
                    {1.0f, 0.894f, 0.882f},
                    {0.882f, 0.969f, 0.933f},
                    lastResultado >= 0
                        ? new float[]{1.0f, 0.894f, 0.882f}
                        : new float[]{0.882f, 0.969f, 0.933f}
                };
                String[] boxLabels = {
                    "Base imponible general",
                    "Cuota integra estimada",
                    "Retenciones practicadas",
                    lastResultado >= 0 ? "RESULTADO: A PAGAR" : "RESULTADO: A DEVOLVER"
                };
                String[] boxValues = {
                    fmt(lastBaseGeneral)         + " EUR",
                    fmt(lastCuotaTotal)           + " EUR",
                    fmt(lastRetenciones)          + " EUR",
                    fmt(Math.abs(lastResultado))  + " EUR"
                };

                for (int i = 0; i < 4; i++) {
                    float bx = M + i * (boxW + 4);
                    float by = y - boxH;
                    float[] rgb = bgColors[i];

                    fillRect(cs, bx, by, boxW, boxH, rgb[0], rgb[1], rgb[2]);
                    strokeRect(cs, bx, by, boxW, boxH, 0.87f, 0.87f, 0.87f, 0.5f);
                    drawText(cs, regular, 7.5f, 0.35f, 0.35f, 0.35f, bx + 5, by + boxH - 12, boxLabels[i]);

                    cs.setStrokingColor(0.87f, 0.87f, 0.87f);
                    cs.setLineWidth(0.4f);
                    cs.moveTo(bx + 5, by + boxH - 17);
                    cs.lineTo(bx + boxW - 5, by + boxH - 17);
                    cs.stroke();

                    drawText(cs, bold, (i == 3 ? 13 : 11), 0.1f, 0.1f, 0.1f, bx + 5, by + 10, boxValues[i]);
                }
                y -= boxH + 18;

                // ── TRAMOS IRPF ───────────────────────────────────────
                drawSectionTitle(cs, bold, "TRAMOS IRPF ESTATAL APLICADOS", M, y);
                y -= 14;

                fillRect(cs, M, y - 17, CW, 17, 0.094f, 0.373f, 0.647f);
                float[] colX = {M + 4, M + 90, M + 190, M + 285, M + 365, M + 445};
                String[] tHeaders = {"Tramo", "Desde (EUR)", "Hasta (EUR)", "Tipo", "Base aplicable", "Cuota tramo"};
                for (int i = 0; i < tHeaders.length; i++) {
                    drawText(cs, bold, 8, 1f, 1f, 1f, colX[i], y - 12, tHeaders[i]);
                }
                y -= 17;

                double base = lastBaseGeneral;
                int tramoNum = 1;
                for (double[] t : TRAMOS_ESTATAL) {
                    if (base <= 0 || t[0] >= lastBaseGeneral) break;
                    double hasta      = Math.min(lastBaseGeneral, t[1]);
                    double aplicable  = hasta - t[0];
                    double cuotaTramo = aplicable * (t[2] / 100.0);
                    base -= aplicable;

                    if (tramoNum % 2 == 0) fillRect(cs, M, y - 15, CW, 15, 0.969f, 0.965f, 0.953f);

                    drawText(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f,  colX[0], y - 10, "Tramo " + tramoNum);
                    drawText(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f,  colX[1], y - 10, fmt(t[0]));
                    drawText(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f,  colX[2], y - 10,
                            t[1] == Double.MAX_VALUE ? "Sin limite" : fmt(t[1]));
                    drawText(cs, bold,    8.5f, 0.1f, 0.1f, 0.1f,  colX[3], y - 10, String.format("%.1f%%", t[2]));
                    drawText(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f,  colX[4], y - 10, fmt(aplicable));
                    drawText(cs, bold,    8.5f, 0.85f, 0.35f, 0.19f, colX[5], y - 10, fmt(cuotaTramo));
                    y -= 15;
                    tramoNum++;
                }

                // Borde de tabla
                float tableH = 15 * (tramoNum - 1) + 17;
                cs.setStrokingColor(0.87f, 0.87f, 0.87f);
                cs.setLineWidth(0.5f);
                cs.addRect(M, y, CW, tableH);
                cs.stroke();

                y -= 16;

                // ── AVISO LEGAL ───────────────────────────────────────
                fillRect(cs, M, y - 38, CW, 38, 0.992f, 0.953f, 0.875f);
                strokeRect(cs, M, y - 38, CW, 38, 0.729f, 0.525f, 0.09f, 0.8f);
                drawText(cs, bold,    8.5f, 0.60f, 0.43f, 0.07f, M + 7, y - 11,
                        "AVISO LEGAL");
                drawText(cs, oblique, 7.5f, 0.42f, 0.42f, 0.39f, M + 7, y - 22,
                        "Esta calculadora es orientativa y no tiene en cuenta todas las deducciones ni circunstancias personales.");
                drawText(cs, oblique, 7.5f, 0.42f, 0.42f, 0.39f, M + 7, y - 32,
                        "Consulta a un asesor fiscal o usa el programa oficial de la AEAT para tu declaracion definitiva.");

                // ── PIE ───────────────────────────────────────────────
                cs.setStrokingColor(0.878f, 0.867f, 0.847f);
                cs.setLineWidth(0.5f);
                cs.moveTo(M, 24);
                cs.lineTo(W - M, 24);
                cs.stroke();
                drawText(cs, regular, 7.5f, 0.65f, 0.65f, 0.65f, M, 14,
                        "FinanzApp - Informe generado el " + fecha + "  -  Pagina 1 / 1");
            }

            doc.save(file);
        }
    }

    /** Sustituye caracteres fuera de WinAnsiEncoding por equivalentes ASCII seguros. */
    private String asciify(String s) {
        if (s == null) return "";
        return s
            .replace("\u00e1", "a").replace("\u00e9", "e").replace("\u00ed", "i")
            .replace("\u00f3", "o").replace("\u00fa", "u")
            .replace("\u00c1", "A").replace("\u00c9", "E").replace("\u00cd", "I")
            .replace("\u00d3", "O").replace("\u00da", "U")
            .replace("\u00f1", "n").replace("\u00d1", "N")
            .replace("\u00fc", "u").replace("\u00dc", "U")
            .replace("\u2014", "-").replace("\u2013", "-")
            .replace("\u00b7", ".")
            .replace("\u00bf", "?").replace("\u00a1", "!")
            .replace("\u20ac", "EUR")
            .replace("\u26a0", "(!)")
            .replace("\u2019", "'").replace("\u201c", "\"").replace("\u201d", "\"")
            .replaceAll("[^\u0000-\u00ff]", "?");
    }

    // ── Helpers PDF ──────────────────────────────────────────────

    private void drawText(PDPageContentStream cs, PDType1Font font, float size,
                          float r, float g, float b, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(r, g, b);
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /** Dibuja texto alineado a la derecha respecto al punto x dado. */
    private void drawTextRight(PDPageContentStream cs, PDType1Font font, float size,
                               float r, float g, float b, float xRight, float y, String text) throws IOException {
        float textW = font.getStringWidth(text) / 1000 * size;
        drawText(cs, font, size, r, g, b, xRight - textW, y, text);
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
        // Línea de acento
        cs.setNonStrokingColor(0.114f, 0.620f, 0.459f);
        cs.addRect(x, y - 3, 3, 12);
        cs.fill();
        // Texto
        drawText(cs, bold, 8, 0.42f, 0.42f, 0.39f, x + 8, y, title);
    }

    // ── Helpers de cálculo ────────────────────────────────────────

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

    private double cuotaAhorro(double base) {
        double cuota = 0;
        double[][] tramos = {{0,6000,19},{6000,50000,21},{50000,200000,23},{200000,Double.MAX_VALUE,27}};
        for (double[] t : tramos) {
            if (base <= 0) break;
            double ap = Math.min(base, t[1] - t[0]);
            cuota += ap * (t[2] / 100.0);
            base  -= ap;
        }
        return cuota;
    }

    private void renderTramos(double base) {
        tramosBox.getChildren().clear();
        for (double[] t : TRAMOS_ESTATAL) {
            if (base <= 0 || t[0] >= base) break;
            double desde = t[0], hasta = Math.min(base, t[1]);
            double aplicable = hasta - desde;
            double cuota = aplicable * (t[2] / 100.0);

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 8, 6, 8));
            row.setStyle("-fx-background-color:#F7F6F3;-fx-background-radius:6;");

            Label rango  = new Label(fmt(desde) + "€ – " + (t[1] == Double.MAX_VALUE ? "+" : fmt(t[1]) + "€"));
            rango.setStyle("-fx-font-size:12px;-fx-text-fill:#6B6A65;"); HBox.setHgrow(rango, Priority.ALWAYS);
            Label tipo   = new Label(String.format("%.1f%%", t[2])); tipo.setStyle("-fx-font-size:12px;-fx-font-weight:600;");
            Label cuotaL = new Label(fmt(cuota) + "€"); cuotaL.setStyle("-fx-font-size:12px;-fx-font-family:monospace;-fx-text-fill:#D85A30;");
            row.getChildren().addAll(rango, tipo, cuotaL);
            tramosBox.getChildren().add(row);
        }
    }

    private double parseD(String s) {
        try { return Double.parseDouble(s.trim().replace(",",".")); } catch (Exception e) { return 0; }
    }

    private String fmt(double v) { return String.format("%,.0f", v).replace(",","."); }
}
