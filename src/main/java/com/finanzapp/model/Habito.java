package com.finanzapp.model;

/**
 * Representa un hábito de gasto recurrente.
 */
public class Habito {

    private int id;
    private int usuarioId;
    private String emoji;
    private String nombre;
    private int frecuenciaActual;
    private int frecuenciaObj;
    private String unidad;
    private double coste;
    private String descripcion;

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

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getFrecuenciaActual() {
        return frecuenciaActual;
    }

    public void setFrecuenciaActual(int frecuenciaActual) {
        this.frecuenciaActual = frecuenciaActual;
    }

    public int getFrecuenciaObj() {
        return frecuenciaObj;
    }

    public void setFrecuenciaObj(int frecuenciaObj) {
        this.frecuenciaObj = frecuenciaObj;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getCoste() {
        return coste;
    }

    public void setCoste(double coste) {
        this.coste = coste;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Calcula el gasto mensual
     */
    public double getGastoActual() {
        double factor;
        if ("semana".equals(unidad)) {
            factor = 4.3;
        } else {
            factor = 1.0;
        }
        double vecesAlMes = frecuenciaActual * factor;
        double gastoMensual = vecesAlMes * coste;
        return gastoMensual;
    }

    /**
     * Calcula el gasto mensual en el supuesto caso de lograr el objetivo
     */
    public double getGastoObjetivo() {
        double factor;
        if ("semana".equals(unidad)) {
            factor = 4.3;
        } else {
            factor = 1.0;
        }
        double vecesAlMesObjetivo = frecuenciaObj * factor;
        double gastoMensualObjetivo = vecesAlMesObjetivo * coste;
        return gastoMensualObjetivo;
    }

    public double getAhorroPotencial() {
        double resto = getGastoActual() - getGastoObjetivo();
        if (resto < 0) {
            return 0;
        } else {
            return resto;
        }
    }

}
