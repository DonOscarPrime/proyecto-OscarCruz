package com.finanzapp.model;

import java.time.LocalDate;

public class Movimiento {

    private int id;
    private int usuarioId;
    private String tipo;
    private String nombre;
    private double cantidad;
    private int categoriaId;
    private String categoriaNombre;
    private String categoriaEmoji;
    private String notas;
    private LocalDate fecha;

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaEmoji() {
        return categoriaEmoji;
    }

    public void setCategoriaEmoji(String categoriaEmoji) {
        this.categoriaEmoji = categoriaEmoji;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public boolean isIngreso() {
        if ("ingreso".equals(tipo)) {
            return true;
        } else {
            return false;
        }
    }

    public double getCantidadConSigno() {
        if (isIngreso()) {
            return cantidad;
        } else {
            return -cantidad;
        }
    }

    /**
     * Devuelve el emoji y nombre de la categoría forma de listas.
     */
    public String getCategoriaDisplay() {
        String emoji;
        if (categoriaEmoji != null) {
            emoji = categoriaEmoji;
        } else {
            emoji = "";
        }

        String nombreCategoria;
        if (categoriaNombre != null) {
            nombreCategoria = categoriaNombre;
        } else {
            nombreCategoria = "";
        }

        boolean emojiEstaVacio = emoji.isBlank();
        if (emojiEstaVacio) {
            return nombreCategoria;
        } else {
            String textoConEmoji = emoji + " " + nombreCategoria;
            return textoConEmoji;
        }
    }
}
