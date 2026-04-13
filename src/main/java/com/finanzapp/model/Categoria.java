package com.finanzapp.model;

public class Categoria {
    private int id;
    private String nombre;
    private String emoji;
    private String tipo;
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

    @Override
    public String toString() {
        return (emoji != null ? emoji + " " : "") + nombre;
    }
}
