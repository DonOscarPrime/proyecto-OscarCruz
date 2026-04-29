package com.finanzapp.model;

import java.time.LocalDate;

/**
 * Representa un objetivo de ahorro personal del usuario en Fox Wallet.
 * <p>
 * Ejemplos: "Fondo de emergencia 3.000 €", "Viaje a Japón", "Entrada del coche".
 * El progreso se muestra visualmente con una barra de porcentaje en el panel de objetivos.
 * Cuando el importe {@code actual} alcanza el importe {@code objetivo},
 * el campo {@code completado} se actualiza automáticamente mediante el
 * procedimiento almacenado {@code actualizarProgresoObjetivo}.
 */
public class Objetivo {
    private int id;
    private int usuarioId;
    private String nombre;
    private double objetivo;      // importe meta en euros
    private double actual;        // importe acumulado hasta ahora
    private String emoji;
    private LocalDate fechaLimite;
    private boolean completado;

    public int    getId()                  { return id; }
    public void   setId(int id)           { this.id = id; }
    public int    getUsuarioId()           { return usuarioId; }
    public void   setUsuarioId(int u)     { this.usuarioId = u; }
    public String getNombre()             { return nombre; }
    public void   setNombre(String n)     { this.nombre = n; }
    public double getObjetivo()           { return objetivo; }
    public void   setObjetivo(double o)   { this.objetivo = o; }
    public double getActual()             { return actual; }
    public void   setActual(double a)     { this.actual = a; }
    public String getEmoji()              { return emoji; }
    public void   setEmoji(String e)      { this.emoji = e; }
    public LocalDate getFechaLimite()     { return fechaLimite; }
    public void   setFechaLimite(LocalDate f) { this.fechaLimite = f; }
    public boolean isCompletado()         { return completado; }
    public void   setCompletado(boolean c){ this.completado = c; }

    /**
     * Calcula el porcentaje de progreso del objetivo de ahorro (0–100 %).
     * Nunca supera el 100 % aunque el importe actual exceda la meta.
     */
    public double getPorcentajeProgreso() {
        if (objetivo <= 0) return 0;
        return Math.min(100.0, (actual / objetivo) * 100.0);
    }

    /**
     * Calcula el importe que falta por ahorrar para alcanzar la meta.
     * Devuelve 0 si el objetivo ya está completado.
     */
    public double getImporteRestante() {
        return Math.max(0, objetivo - actual);
    }

    /**
     * @deprecated Usar {@link #getPorcentajeProgreso()} para mayor claridad.
     */
    @Deprecated
    public double getPorcentaje() { return getPorcentajeProgreso(); }

    /**
     * @deprecated Usar {@link #getImporteRestante()} para mayor claridad.
     */
    @Deprecated
    public double getRestante() { return getImporteRestante(); }
}
