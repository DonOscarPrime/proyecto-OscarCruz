package com.finanzapp.model;

/**
 * Datos de entrada y resultados de la estimación orientativa del IRPF.
 */
public class RentaCalculo {

    // ── Inputs ────────────────────────────────────────────────────────────
    private double salarioBruto;
    private double retenciones;
    private int    hijos;

    // ── Outputs ───────────────────────────────────────────────────────────
    private double baseImponible;
    private double cuotaIntegra;
    private double resultado;
    private double tipoEfectivo;

    // ── Getters / Setters ────────────────────────────────────────────────

    public double getSalarioBruto() { return salarioBruto; }
    public void   setSalarioBruto(double v) { this.salarioBruto = v; }

    public double getRetenciones() { return retenciones; }
    public void   setRetenciones(double v) { this.retenciones = v; }

    public int  getHijos() { return hijos; }
    public void setHijos(int v) { this.hijos = v; }

    public double getBaseImponible() { return baseImponible; }
    public void   setBaseImponible(double v) { this.baseImponible = v; }

    public double getCuotaIntegra() { return cuotaIntegra; }
    public void   setCuotaIntegra(double v) { this.cuotaIntegra = v; }

    public double getResultado() { return resultado; }
    public void   setResultado(double v) { this.resultado = v; }

    public double getTipoEfectivo() { return tipoEfectivo; }
    public void   setTipoEfectivo(double v) { this.tipoEfectivo = v; }
}
