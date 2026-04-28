package com.finanzapp.model;

/**
 * Representa una categoría del catálogo de Fox Wallet para clasificar
 * los movimientos del usuario (p. ej. 🛒 Supermercado, 🚗 Transporte, 💼 Nómina).
 * <p>
 * El campo {@code tipo} indica a qué tipo de movimiento aplica:
 * {@code "gasto"}, {@code "ingreso"} o {@code "ambos"} (p. ej. "Otros").
 */
public class Categoria {
    private int id;
    private String nombre;
    private String emoji;
    private String tipo;   // "gasto" | "ingreso" | "ambos"
    private String color;

    public int    getId()             { return id; }
    public void   setId(int id)      { this.id = id; }
    public String getNombre()         { return nombre; }
    public void   setNombre(String n) { this.nombre = n; }
    public String getEmoji()          { return emoji; }
    public void   setEmoji(String e)  { this.emoji = e; }
    public String getTipo()           { return tipo; }
    public void   setTipo(String t)   { this.tipo = t; }
    public String getColor()          { return color; }
    public void   setColor(String c)  { this.color = c; }

    /**
     * Devuelve la representación visual de la categoría para los ComboBox del formulario
     * de movimientos (p. ej. "🛒 Supermercado").
     */
    @Override
    public String toString() {
        return (emoji != null ? emoji + " " : "") + nombre;
    }
}
