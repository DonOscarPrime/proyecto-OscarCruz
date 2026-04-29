package com.finanzapp.model;

/**
 * Representa un hábito de gasto recurrente del usuario en Fox Wallet.
 * <p>
 * Un hábito es cualquier consumo periódico (café diario, gimnasio, Netflix…)
 * que el simulador de ahorro usa para calcular cuánto podría ahorrar
 * el usuario si redujera su frecuencia de consumo hacia la frecuencia objetivo.
 * El factor 4,3 convierte frecuencias semanales a mensuales.
 */
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

    /**
     * Calcula el gasto mensual real del usuario para este hábito,
     * según su frecuencia de consumo actual.
     * Las frecuencias semanales se convierten a mensuales multiplicando por 4,3.
     */
    public double getGastoMensualActual() {
        double factorSemanalAMensual = "semana".equals(unidad) ? 4.3 : 1.0;
        return frecuenciaActual * coste * factorSemanalAMensual;
    }

    /**
     * Calcula el gasto mensual que tendría el usuario si alcanzara
     * la frecuencia objetivo marcada en el simulador de ahorro.
     */
    public double getGastoMensualObjetivo() {
        double factorSemanalAMensual = "semana".equals(unidad) ? 4.3 : 1.0;
        return frecuenciaObj * coste * factorSemanalAMensual;
    }

    /**
     * Calcula el ahorro mensual potencial si el usuario reduce este hábito
     * hasta la frecuencia objetivo. Nunca devuelve valores negativos.
     */
    public double getAhorroMensualPotencial() {
        return Math.max(0, getGastoMensualActual() - getGastoMensualObjetivo());
    }

    /**
     * @deprecated Usar {@link #getGastoMensualObjetivo()} para mayor claridad.
     */
    @Deprecated
    public double getGastoMensualObj() { return getGastoMensualObjetivo(); }

    /**
     * @deprecated Usar {@link #getAhorroMensualPotencial()} para mayor claridad.
     */
    @Deprecated
    public double getAhorroMensual() { return getAhorroMensualPotencial(); }
}
