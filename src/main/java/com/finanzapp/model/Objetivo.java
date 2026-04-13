package com.finanzapp.model;

import java.time.LocalDate;

public class Objetivo {
    private int id;
    private int usuarioId;
    private String nombre;
    private double objetivo;
    private double actual;
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

    public double getPorcentaje() {
        if (objetivo <= 0) return 0;
        return Math.min(100.0, (actual / objetivo) * 100.0);
    }

    public double getRestante() {
        return Math.max(0, objetivo - actual);
    }
}
