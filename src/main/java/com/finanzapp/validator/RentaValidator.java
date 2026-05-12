package com.finanzapp.validator;

import com.finanzapp.model.RentaCalculo;

public class RentaValidator {

    public String validarFormulario(RentaCalculo calculo) {
        if (calculo.getSalarioBruto() <= 0) {
            return "Introduce el salario bruto anual.";
        }
        if (calculo.getRetenciones() < 0) {
            return "Las retenciones no pueden ser negativas.";
        }
        return null;
    }

    public double parsearCampo(String texto) {
        if (texto == null || texto.isBlank()) return 0;
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double validarSalario(String texto) throws IllegalArgumentException {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Introduce el salario bruto anual.");
        }
        double salario;
        try {
            salario = Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Salario no válido.");
        }
        if (salario <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }
        return salario;
    }
}
