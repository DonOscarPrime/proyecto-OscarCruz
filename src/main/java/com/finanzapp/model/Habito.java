package com.finanzapp.model;

public class Habito {
    private int id;
    private int usuarioId;
    private String emoji;
    private String nombre;
    private int frecuenciaActual;
    private int frecuenciaObj;
    private String unidad;   // "semana" | "mes"
    private double coste;
    private String descripcion;

    public int    getId()                   { return id; }
    public void   setId(int id)            { this.id = id; }
    public int    getUsuarioId()            { return usuarioId; }
    public void   setUsuarioId(int u)      { this.usuarioId = u; }
    public String getEmoji()               { return emoji; }
    public void   setEmoji(String e)       { this.emoji = e; }
    public String getNombre()              { return nombre; }
    public void   setNombre(String n)      { this.nombre = n; }
    public int    getFrecuenciaActual()    { return frecuenciaActual; }
    public void   setFrecuenciaActual(int f){ this.frecuenciaActual = f; }
    public int    getFrecuenciaObj()       { return frecuenciaObj; }
    public void   setFrecuenciaObj(int f)  { this.frecuenciaObj = f; }
    public String getUnidad()              { return unidad; }
    public void   setUnidad(String u)      { this.unidad = u; }
    public double getCoste()               { return coste; }
    public void   setCoste(double c)       { this.coste = c; }
    public String getDescripcion()         { return descripcion; }
    public void   setDescripcion(String d) { this.descripcion = d; }

    /** Gasto mensual actual según frecuencia. */
    public double getGastoMensualActual() {
        double factor = "semana".equals(unidad) ? 4.3 : 1.0;
        return frecuenciaActual * coste * factor;
    }

    /** Gasto mensual objetivo. */
    public double getGastoMensualObj() {
        double factor = "semana".equals(unidad) ? 4.3 : 1.0;
        return frecuenciaObj * coste * factor;
    }

    /** Ahorro mensual posible. */
    public double getAhorroMensual() {
        return Math.max(0, getGastoMensualActual() - getGastoMensualObj());
    }
}
