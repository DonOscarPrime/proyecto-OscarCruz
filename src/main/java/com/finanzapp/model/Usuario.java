package com.finanzapp.model;

import java.time.LocalDate;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String passwordHash;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String comunidad;
    private String situacionLaboral;
    private double ingresosNetos;
    private String objetivoFinanciero;
    private double presupuestoMensual;
    private String tema;
    private String moneda;
    private String idioma;

    // ── Getters & Setters ───────────────────────────────────
    public int    getId()                    { return id; }
    public void   setId(int id)             { this.id = id; }
    public String getNombre()               { return nombre; }
    public void   setNombre(String n)       { this.nombre = n; }
    public String getEmail()                { return email; }
    public void   setEmail(String e)        { this.email = e; }
    public String getPasswordHash()         { return passwordHash; }
    public void   setPasswordHash(String h) { this.passwordHash = h; }
    public String getTelefono()             { return telefono; }
    public void   setTelefono(String t)     { this.telefono = t; }
    public LocalDate getFechaNacimiento()   { return fechaNacimiento; }
    public void   setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }
    public String getComunidad()            { return comunidad; }
    public void   setComunidad(String c)    { this.comunidad = c; }
    public String getSituacionLaboral()     { return situacionLaboral; }
    public void   setSituacionLaboral(String s) { this.situacionLaboral = s; }
    public double getIngresosNetos()        { return ingresosNetos; }
    public void   setIngresosNetos(double i){ this.ingresosNetos = i; }
    public String getObjetivoFinanciero()   { return objetivoFinanciero; }
    public void   setObjetivoFinanciero(String o) { this.objetivoFinanciero = o; }
    public double getPresupuestoMensual()   { return presupuestoMensual; }
    public void   setPresupuestoMensual(double p) { this.presupuestoMensual = p; }
    public String getTema()                 { return tema; }
    public void   setTema(String t)         { this.tema = t; }
    public String getMoneda()               { return moneda; }
    public void   setMoneda(String m)       { this.moneda = m; }
    public String getIdioma()               { return idioma; }
    public void   setIdioma(String i)       { this.idioma = i; }

    /** Devuelve las iniciales del nombre (máx. 2 caracteres). */
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) return "??";
        String[] parts = nombre.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)).toUpperCase();
    }
}
