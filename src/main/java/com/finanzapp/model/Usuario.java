package com.finanzapp.model;

import java.time.LocalDate;

/**
 * Almacena datos personales y la información financiera que personaliza la app.
 */
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getComunidad() {
        return comunidad;
    }

    public void setComunidad(String comunidad) {
        this.comunidad = comunidad;
    }

    public String getSituacionLaboral() {
        return situacionLaboral;
    }

    public void setSituacionLaboral(String situacionLaboral) {
        this.situacionLaboral = situacionLaboral;
    }

    public double getIngresosNetos() {
        return ingresosNetos;
    }

    public void setIngresosNetos(double ingresosNetos) {
        this.ingresosNetos = ingresosNetos;
    }

    public String getObjetivoFinanciero() {
        return objetivoFinanciero;
    }

    public void setObjetivoFinanciero(String objetivoFinanciero) {
        this.objetivoFinanciero = objetivoFinanciero;
    }

    public double getPresupuestoMensual() {
        return presupuestoMensual;
    }

    public void setPresupuestoMensual(double presupuestoMensual) {
        this.presupuestoMensual = presupuestoMensual;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    /**
     * Muestra las iniciales del nombre del usuario
     */
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "??";
        }

        String nombreSinEspaciosExternos = nombre.trim();
        String[] partes = nombreSinEspaciosExternos.split("\\s+");

        if (partes.length == 1) {
            int longitudMaxima = 2;
            int longitudReal = partes[0].length();
            int caracteresACoger;
            if (longitudReal < longitudMaxima) {
                caracteresACoger = longitudReal;
            } else {
                caracteresACoger = longitudMaxima;
            }
            String primeras2Letras = partes[0].substring(0, caracteresACoger);
            return primeras2Letras.toUpperCase();
        }

        char primeraLetra = partes[0].charAt(0);
        char segundaLetra = partes[1].charAt(0);
        String iniciales = String.valueOf(primeraLetra) + segundaLetra;
        return iniciales.toUpperCase();
    }
}
