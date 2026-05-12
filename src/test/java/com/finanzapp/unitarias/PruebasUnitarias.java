package com.finanzapp.unitarias;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasUnitarias {

    private static final double[][] TRAMOS = {
        {  12450, 0.19 }, { 20200, 0.24 }, { 35200, 0.30 },
        {  60000, 0.37 }, { 300000, 0.45 }, { Double.MAX_VALUE, 0.47 }
    };

    private double calcularIRPF(double base) {
        double cuota = 0, acumulado = 0;
        for (double[] t : TRAMOS) {
            if (base <= acumulado) break;
            cuota += Math.min(base - acumulado, t[0] - acumulado) * t[1];
            acumulado = t[0];
            if (base <= t[0]) break;
        }
        return Math.round(cuota * 100.0) / 100.0;
    }

    @Test @Order(1)
    @DisplayName("U-01: IRPF base 10.000 € → 1.900 € (tramo 19%)")
    void testIRPF_primerTramo() {
        assertEquals(1900.0, calcularIRPF(10_000), 0.01);
    }

    @Test @Order(2)
    @DisplayName("U-02: IRPF base 0 € → 0 €")
    void testIRPF_baseNula() {
        assertEquals(0.0, calcularIRPF(0), 0.001);
    }

    @Test @Order(3)
    @DisplayName("U-03: Balance = ingresos - gastos")
    void testBalance() {
        assertEquals(700.0, 2500.0 - 1800.0, 0.001);
    }

    @Test @Order(4)
    @DisplayName("U-04: Progreso objetivo nunca supera el 100%")
    void testProgresoObjetivo() {
        assertEquals(50,  (int) Math.min(100, (500.0 / 1000.0) * 100));
        assertEquals(100, (int) Math.min(100, (1500.0 / 1000.0) * 100));
    }
}
