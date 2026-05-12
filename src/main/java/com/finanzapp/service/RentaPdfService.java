package com.finanzapp.service;

import com.finanzapp.model.RentaCalculo;
import com.finanzapp.util.Formateador;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera el PDF del informe IRPF orientativo de Fox Wallet.
 */
public class RentaPdfService {

    public void generarPDF(RentaCalculo calculo, String nombreUsuario, File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float W = page.getMediaBox().getWidth();
            float H = page.getMediaBox().getHeight();
            float M = 45f;
            float CW = W - 2 * M;

            PDType1Font bold    = PDType1Font.HELVETICA_BOLD;
            PDType1Font regular = PDType1Font.HELVETICA;
            PDType1Font oblique = PDType1Font.HELVETICA_OBLIQUE;

            String nombre = ascii(nombreUsuario);
            String fecha  = ascii(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
                    new Locale("es"))));

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = dibujarCabecera(cs, bold, regular, W, H, M, nombre, fecha);
                y = dibujarResumen(cs, bold, regular, M, CW, y, calculo);
                y = dibujarTramos(cs, bold, regular, M, CW, y, calculo);
                dibujarAviso(cs, bold, oblique, M, CW, y);
                dibujarPie(cs, regular, M, W, fecha);
            }

            doc.save(file);
        }
    }

    // ── Secciones ─────────────────────────────────────────────────────────

    private float dibujarCabecera(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                                   float W, float H, float M, String nombre, String fecha) throws IOException {
        float hCab = 68;
        fillRect(cs, 0, H - hCab, W, hCab, 0.094f, 0.373f, 0.647f);
        fillRect(cs, 0, H - hCab, 6, hCab, 0.114f, 0.620f, 0.459f);
        text(cs, bold,    17, 1f,    1f,    1f,    M, H - 32, "Fox Wallet - Estimacion orientativa del IRPF");
        text(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 47,
            "Solo orientativa — no sustituye a la declaracion oficial  |  Ejercicio " + LocalDate.now().getYear());
        text(cs, regular,  9, 0.75f, 0.87f, 0.97f, M, H - 60,
            "Generado el " + fecha + "   |   " + nombre);
        return H - 84;
    }

    private float dibujarResumen(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                                  float M, float CW, float y, RentaCalculo calculo) throws IOException {
        titulo(cs, bold, "RESUMEN DE LA ESTIMACION", M, y);
        y -= 14;

        String etqResultado = calculo.getResultado() >= 0 ? "A PAGAR" : "A DEVOLVER";
        double absResultado = Math.abs(calculo.getResultado());

        String[] etiquetas = {
            "Salario bruto anual",
            "Base imponible",
            "Cuota estimada",
            "Tipo efectivo",
            etqResultado
        };
        String[] valores = {
            Formateador.moneda(calculo.getSalarioBruto()) + " EUR",
            Formateador.moneda(calculo.getBaseImponible()) + " EUR",
            Formateador.moneda(calculo.getCuotaIntegra()) + " EUR",
            String.format("%.1f%%", calculo.getTipoEfectivo()),
            Formateador.moneda(absResultado) + " EUR"
        };

        float boxW = (CW - 16f) / 5f;
        float boxH = 50;
        float[] colorRes = calculo.getResultado() >= 0
            ? new float[]{1.0f, 0.894f, 0.882f}
            : new float[]{0.882f, 0.969f, 0.933f};

        float[][] fondos = {
            {0.635f, 0.831f, 0.949f},
            {0.969f, 0.965f, 0.953f},
            {1.0f,   0.894f, 0.882f},
            {0.969f, 0.965f, 0.953f},
            colorRes
        };

        for (int i = 0; i < 5; i++) {
            float bx = M + i * (boxW + 4);
            float by = y - boxH;
            fillRect(cs, bx, by, boxW, boxH, fondos[i][0], fondos[i][1], fondos[i][2]);
            strokeRect(cs, bx, by, boxW, boxH, 0.87f, 0.87f, 0.87f, 0.5f);
            text(cs, regular, 6.5f, 0.35f, 0.35f, 0.35f, bx + 4, by + boxH - 11, etiquetas[i]);
            cs.setStrokingColor(0.87f, 0.87f, 0.87f);
            cs.setLineWidth(0.4f);
            cs.moveTo(bx + 4, by + boxH - 15); cs.lineTo(bx + boxW - 4, by + boxH - 15); cs.stroke();
            float tam = (i == 4) ? 10f : 9f;
            text(cs, bold, tam, 0.1f, 0.1f, 0.1f, bx + 4, by + 9, valores[i]);
        }

        return y - boxH - 16;
    }

    private float dibujarTramos(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                                 float M, float CW, float y, RentaCalculo calculo) throws IOException {
        titulo(cs, bold, "TRAMOS IRPF APLICADOS (escala orientativa)", M, y);
        y -= 14;

        float hCab = 16;
        fillRect(cs, M, y - hCab, CW, hCab, 0.094f, 0.373f, 0.647f);
        float yHdr = y - 11;
        float[] colX = {M + 4, M + 90, M + 185, M + 280, M + 370};
        String[] cabs = {"Tramo", "Desde (EUR)", "Hasta (EUR)", "Tipo", "Cuota tramo"};
        for (int i = 0; i < cabs.length; i++) {
            text(cs, bold, 8, 1f, 1f, 1f, colX[i], yHdr, cabs[i]);
        }
        y -= hCab;

        double base = calculo.getBaseImponible();
        int num = 1;
        for (double[] t : RentaService.TRAMOS) {
            if (base <= 0 || t[0] >= calculo.getBaseImponible()) break;
            double hasta = Math.min(calculo.getBaseImponible(), t[1]);
            double aplicable = hasta - t[0];
            double cuota = aplicable * (t[2] / 100.0);
            base -= aplicable;

            if (num % 2 == 0) fillRect(cs, M, y - 14, CW, 14, 0.969f, 0.965f, 0.953f);
            float yf = y - 9;
            text(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f, colX[0], yf, "Tramo " + num);
            text(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f, colX[1], yf, Formateador.moneda(t[0]));
            text(cs, regular, 8.5f, 0.1f, 0.1f, 0.1f, colX[2], yf,
                t[1] == Double.MAX_VALUE ? "Sin limite" : Formateador.moneda(t[1]));
            text(cs, bold,    8.5f, 0.1f, 0.1f, 0.1f, colX[3], yf, String.format("%.0f%%", t[2]));
            text(cs, bold,    8.5f, 0.094f, 0.373f, 0.647f, colX[4], yf, Formateador.moneda(cuota));
            y -= 14;
            num++;
        }

        return y - 16;
    }

    private void dibujarAviso(PDPageContentStream cs, PDType1Font bold, PDType1Font oblique,
                               float M, float CW, float y) throws IOException {
        fillRect(cs, M, y - 38, CW, 38, 0.992f, 0.953f, 0.875f);
        strokeRect(cs, M, y - 38, CW, 38, 0.729f, 0.525f, 0.09f, 0.8f);
        text(cs, bold,    8.5f, 0.60f, 0.43f, 0.07f, M + 7, y - 11, "AVISO LEGAL");
        text(cs, oblique, 7.5f, 0.42f, 0.42f, 0.39f, M + 7, y - 22,
            "Esta estimacion es orientativa y no refleja deducciones autonómicas ni circunstancias personales.");
        text(cs, oblique, 7.5f, 0.42f, 0.42f, 0.39f, M + 7, y - 32,
            "Consulta a un asesor fiscal o usa el programa oficial de la AEAT para tu declaracion definitiva.");
    }

    private void dibujarPie(PDPageContentStream cs, PDType1Font regular,
                             float M, float W, String fecha) throws IOException {
        cs.setStrokingColor(0.878f, 0.867f, 0.847f);
        cs.setLineWidth(0.5f);
        cs.moveTo(M, 24); cs.lineTo(W - M, 24); cs.stroke();
        text(cs, regular, 7.5f, 0.65f, 0.65f, 0.65f, M, 14,
            "Fox Wallet - Estimacion IRPF generada el " + fecha + "  -  Pagina 1 / 1");
    }

    // ── Primitivas ────────────────────────────────────────────────────────

    private void titulo(PDPageContentStream cs, PDType1Font bold,
                        String t, float x, float y) throws IOException {
        cs.setNonStrokingColor(0.114f, 0.620f, 0.459f);
        cs.addRect(x, y - 3, 3, 12); cs.fill();
        text(cs, bold, 8, 0.42f, 0.42f, 0.39f, x + 8, y, t);
    }

    private void text(PDPageContentStream cs, PDType1Font font, float size,
                      float r, float g, float b, float x, float y, String s) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(r, g, b);
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(s);
        cs.endText();
    }

    private void fillRect(PDPageContentStream cs, float x, float y, float w, float h,
                          float r, float g, float b) throws IOException {
        cs.setNonStrokingColor(r, g, b);
        cs.addRect(x, y, w, h); cs.fill();
    }

    private void strokeRect(PDPageContentStream cs, float x, float y, float w, float h,
                             float r, float g, float b, float lw) throws IOException {
        cs.setStrokingColor(r, g, b);
        cs.setLineWidth(lw);
        cs.addRect(x, y, w, h); cs.stroke();
    }

    private String ascii(String s) {
        if (s == null) return "";
        return s
            .replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u")
            .replace("Á","A").replace("É","E").replace("Í","I").replace("Ó","O").replace("Ú","U")
            .replace("ñ","n").replace("Ñ","N").replace("ü","u")
            .replace("—","-").replace("–","-").replace("€","EUR")
            .replaceAll("[^ -ÿ]","?");
    }
}
