package com.finanzapp.validator;

/**
 * Valida los datos de entrada del formulario de movimientos.
 */
public class MovimientoValidator {

    public String validarCantidad(String cantStr) {
        if (cantStr == null || cantStr.isBlank()) {
            return "Introduce una cantidad.";
        }

        String cantidadSinEspacios = cantStr.trim();
        String cantidadLimpia = cantidadSinEspacios.replace(",", ".");

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantidadLimpia);
        } catch (NumberFormatException e) {
            return "Cantidad no válida.";
        }

        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que 0.";
        }

        return null;
    }

    public String validarDescripcion(String desc) {
        if (desc == null || desc.isBlank()) {
            return "Introduce una descripción.";
        }

        return null;
    }

    public double parsearCantidad(String cantStr) {
        String cantidadSinEspacios = cantStr.trim();
        String cantidadLimpia = cantidadSinEspacios.replace(",", ".");
        double cantidadParseada = Double.parseDouble(cantidadLimpia);
        return cantidadParseada;
    }
}
