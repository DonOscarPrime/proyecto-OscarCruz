package com.finanzapp.model;

import java.time.LocalDate;

/**
 * Representa un objetivo de ahorro del usuario
 */
public class Objetivo {

    private int id;
    private int usuarioId;
    private String nombre;
    private double objetivo;
    private double actual;
    private String emoji;
    private LocalDate fechaLimite;
    private boolean completado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(double objetivo) {
        this.objetivo = objetivo;
    }

    public double getActual() {
        return actual;
    }

    public void setActual(double actual) {
        this.actual = actual;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public boolean iscompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }


    public double getProgreso() {
        if (objetivo <= 0) {
            return 0;
        }

        double cociente = actual / objetivo;
        double pct = cociente * 100.0;

        if (pct > 100.0) {
            return 100.0;
        } else {
            return pct;
        }
    }

    public double getImporteRestante() {
        double margen = objetivo - actual;

        if (margen < 0) {
            return 0;
        } else {
            return margen;
        }
    }
}
