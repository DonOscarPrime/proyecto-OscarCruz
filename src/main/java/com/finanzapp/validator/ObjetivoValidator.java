package com.finanzapp.validator;


public class ObjetivoValidator {

    public String validarNombreYMeta(String nombre, String importeStr) {
        if (nombre == null || nombre.isBlank()) {
            return "Rellena los campos obligatorios.";
        }

        if (importeStr == null || importeStr.isBlank()) {
            return "Rellena los campos obligatorios.";
        }

        return null;
    }

    public String validarImporteMeta(String importeStr) {
        String textoSinEspacios = importeStr.trim();
        String textoLimpio = textoSinEspacios.replace(",", ".");

        double importe;
        try {
            importe = Double.parseDouble(textoLimpio);
        } catch (NumberFormatException e) {
            return "Importe no válido.";
        }

        if (importe <= 0) {
            return "La cantidad debe ser mayor que 0.";
        }

        return null;
    }

    public String validarImporteInicial(String importeStr) {
        if (importeStr == null || importeStr.isBlank()) {
            return null;
        }

        String textoSinEspaciosInicial = importeStr.trim();
        String textoLimpio = textoSinEspaciosInicial.replace(",", ".");

        double importe;
        try {
            importe = Double.parseDouble(textoLimpio);
        } catch (NumberFormatException e) {
            return "Importe inicial no válido.";
        }

        if (importe < 0) {
            return "El importe inicial no puede ser negativo.";
        }

        return null;
    }

    public String validarAporte(String aporteStr) {
        if (aporteStr == null || aporteStr.isBlank()) {
            return "Cantidad vacía.";
        }

        String textoSinEspaciosAporte = aporteStr.trim();
        String textoLimpio = textoSinEspaciosAporte.replace(",", ".");

        double importe;
        try {
            importe = Double.parseDouble(textoLimpio);
        } catch (NumberFormatException e) {
            return "Cantidad no válida.";
        }

        if (importe == 0) {
            return "La cantidad debe ser distinta de 0.";
        }

        return null;
    }

    public double parsearImporte(String importeStr) {
        String textoSinEspaciosParsear = importeStr.trim();
        String textoLimpio = textoSinEspaciosParsear.replace(",", ".");
        double importeParseado = Double.parseDouble(textoLimpio);
        return importeParseado;
    }
}
