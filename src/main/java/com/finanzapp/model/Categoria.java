package com.finanzapp.model;

/**
 * Representan categorías, usada para clasificar los movimientos del usuario
 * como gastos o ingresos
 */
public class Categoria {

    private int id;
    private String nombre;
    private String emoji;
    private String tipo;
    private String color;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Devuelve categoría
     */
    @Override
    public String toString() {
        if (emoji != null) {
            String textoConEmoji = emoji + " " + nombre;
            return textoConEmoji;
        } else {
            return nombre;
        }
    }
}
