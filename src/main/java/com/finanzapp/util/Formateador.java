package com.finanzapp.util;

/**
 * Convierte importes y porcentajes a formato legible para la interfaz.
 */
public class Formateador {

    public static String moneda(double valor) {
        String formateadoConComas = String.format("%,.0f", valor);
        String formateadoConPuntos = formateadoConComas.replace(",", ".");
        return formateadoConPuntos;
    }

    public static String porcentaje(double valor) {
        String textoPorcentaje = String.format("%.0f%%", valor);
        return textoPorcentaje;
    }
}
