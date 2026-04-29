package com.finanzapp.model;

import java.time.LocalDate;

/**
 * Representa una transacción económica registrada por el usuario en Fox Wallet.
 * Puede ser un <b>gasto</b> (supermercado, alquiler, ocio…) o un <b>ingreso</b>
 * (nómina, freelance, venta…). Incluye la categoría asociada para mostrar
 * el emoji y el nombre en las listas del historial y del panel de movimientos.
 */
public class Movimiento {
    private int id;
    private int usuarioId;
    private String tipo;          // "gasto" | "ingreso"
    private String nombre;
    private double cantidad;
    private int categoriaId;
    private String categoriaNombre;
    private String categoriaEmoji;
    private String notas;
    private LocalDate fecha;

    // ── Getters & Setters ───────────────────────────────────
    public int    getId()                  { return id; }
    public void   setId(int id)           { this.id = id; }
    public int    getUsuarioId()           { return usuarioId; }
    public void   setUsuarioId(int u)     { this.usuarioId = u; }
    public String getTipo()               { return tipo; }
    public void   setTipo(String t)       { this.tipo = t; }
    public String getNombre()             { return nombre; }
    public void   setNombre(String n)     { this.nombre = n; }
    public double getCantidad()           { return cantidad; }
    public void   setCantidad(double c)   { this.cantidad = c; }
    public int    getCategoriaId()        { return categoriaId; }
    public void   setCategoriaId(int c)   { this.categoriaId = c; }
    public String getCategoriaNombre()    { return categoriaNombre; }
    public void   setCategoriaNombre(String c) { this.categoriaNombre = c; }
    public String getCategoriaEmoji()     { return categoriaEmoji; }
    public void   setCategoriaEmoji(String e) { this.categoriaEmoji = e; }
    public String getNotas()              { return notas; }
    public void   setNotas(String n)      { this.notas = n; }
    public LocalDate getFecha()           { return fecha; }
    public void   setFecha(LocalDate f)   { this.fecha = f; }

    /** Devuelve {@code true} si el movimiento es un ingreso (nómina, freelance…). */
    public boolean isIngreso() { return "ingreso".equals(tipo); }

    /**
     * Devuelve la cantidad con signo contable:
     * <ul>
     *   <li>Positiva para ingresos (suma al saldo)</li>
     *   <li>Negativa para gastos (resta al saldo)</li>
     * </ul>
     * Se usa en el cálculo del balance mensual del dashboard.
     */
    public double getCantidadConSigno() {
        return isIngreso() ? cantidad : -cantidad;
    }

    /**
     * Devuelve el emoji y nombre de la categoría formateados para mostrar
     * en listas del historial (p. ej. "🛒 Supermercado").
     */
    public String getCategoriaDisplay() {
        String emoji = categoriaEmoji != null ? categoriaEmoji : "";
        String cat   = categoriaNombre != null ? categoriaNombre : "";
        return emoji.isBlank() ? cat : emoji + " " + cat;
    }
}
