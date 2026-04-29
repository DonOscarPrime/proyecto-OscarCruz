package com.finanzapp.model;

import java.time.LocalDateTime;

/**
 * Representa un aviso del sistema generado automáticamente por Fox Wallet.
 * <p>
 * Fox Wallet crea notificaciones en situaciones como:
 * superar el presupuesto mensual, alcanzar un objetivo de ahorro o
 * detectar un patrón de gasto inusual. El campo {@code tipo} controla
 * el color del indicador visual: {@code info} (azul), {@code success} (verde),
 * {@code warning} (naranja) o {@code danger} (rojo).
 */
public class Notificacion {
    private int id;
    private int usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo;          // "info" | "success" | "warning" | "danger"
    private boolean leida;
    private LocalDateTime createdAt;

    public int    getId()                   { return id; }
    public void   setId(int id)            { this.id = id; }
    public int    getUsuarioId()            { return usuarioId; }
    public void   setUsuarioId(int u)      { this.usuarioId = u; }
    public String getTitulo()              { return titulo; }
    public void   setTitulo(String t)      { this.titulo = t; }
    public String getMensaje()             { return mensaje; }
    public void   setMensaje(String m)     { this.mensaje = m; }
    public String getTipo()                { return tipo; }
    public void   setTipo(String t)        { this.tipo = t; }
    public boolean isLeida()               { return leida; }
    public void   setLeida(boolean l)      { this.leida = l; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public void   setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
