package com.finanzapp.model;

import java.time.LocalDate;

/**
 * Representa la cuenta y el perfil financiero de un usuario de Fox Wallet.
 * <p>
 * Almacena tanto los datos personales (nombre, email, teléfono, comunidad autónoma)
 * como la información financiera que personaliza la experiencia de la app:
 * situación laboral, ingresos netos mensuales, presupuesto máximo y objetivo financiero.
 * La contraseña se almacena siempre como hash BCrypt, nunca en texto plano.
 */
public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String passwordHash;      // hash BCrypt, nunca texto plano
    private String telefono;
    private LocalDate fechaNacimiento;
    private String comunidad;         // comunidad autónoma española
    private String situacionLaboral;  // Estudiante, Empleado, Autónomo…
    private double ingresosNetos;     // ingresos netos mensuales en euros
    private String objetivoFinanciero;
    private double presupuestoMensual;
    private String tema;              // "claro" | "oscuro"
    private String moneda;            // símbolo: "€", "$", "£"
    private String idioma;            // "Español" | "English"

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

    /**
     * Genera las iniciales del nombre completo del usuario para mostrarlas
     * en el botón de avatar de la barra de navegación (máx. 2 caracteres).
     * <p>
     * Ejemplos: "Oswaldo Cruz" → "OC", "María" → "MA", "" → "??"
     */
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) return "??";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1)
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (String.valueOf(partes[0].charAt(0)) + partes[1].charAt(0)).toUpperCase();
    }
}
