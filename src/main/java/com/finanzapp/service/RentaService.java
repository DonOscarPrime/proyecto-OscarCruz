package com.finanzapp.service;

import com.finanzapp.model.RentaCalculo;

/**
 * Estimación orientativa del IRPF de Fox Wallet.
 * Usa una escala unificada simplificada; no refleja
 * deducciones autonómicas ni circunstancias personales complejas.
 */
public class RentaService {

    /**
     * Escala unificada aproximada (estatal + autonómico medio).
     * Solo orientativa.
     */
    public static final double[][] TRAMOS = {
        {0,      12_450,  19.0},
        {12_450, 20_200,  24.0},
        {20_200, 35_200,  30.0},
        {35_200, 60_000,  37.0},
        {60_000, Double.MAX_VALUE, 45.0}
    };

    /** Deducción fija por gastos de trabajo (simplificada). */
    private static final double DEDUCCION_TRABAJO = 2_000.0;

    /** Deducción plana por hijo a cargo. */
    private static final double DEDUCCION_POR_HIJO = 600.0;

    public void calcular(RentaCalculo calculo) {
        double bruto = calculo.getSalarioBruto();

        // Base imponible
        double base = Math.max(0, bruto - DEDUCCION_TRABAJO);
        calculo.setBaseImponible(base);

        // Cuota según escala
        double cuota = aplicarEscala(base);

        // Reducción por hijos (plana orientativa)
        int hijos = calculo.getHijos();
        double dedHijos = hijos * DEDUCCION_POR_HIJO;
        cuota = Math.max(0, cuota - dedHijos);
        calculo.setCuotaIntegra(cuota);

        // Tipo efectivo sobre bruto
        double tipoEfectivo = bruto > 0 ? (cuota / bruto) * 100.0 : 0;
        calculo.setTipoEfectivo(tipoEfectivo);

        // Resultado
        double resultado = cuota - calculo.getRetenciones();
        calculo.setResultado(resultado);
    }

    /** Aplica la escala progresiva por tramos. */
    public double aplicarEscala(double base) {
        double cuota = 0;
        for (double[] t : TRAMOS) {
            if (base <= 0) break;
            double tamTramo = t[1] - t[0];
            double aplicable = Math.min(base, tamTramo);
            cuota += aplicable * (t[2] / 100.0);
            base -= aplicable;
        }
        return cuota;
    }
}
