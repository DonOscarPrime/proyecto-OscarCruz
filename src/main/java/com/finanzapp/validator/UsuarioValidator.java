package com.finanzapp.validator;

public class UsuarioValidator {

    public String validarLogin(String email, String password) {
        if (email == null || email.isBlank()) {
            return "Por favor rellena todos los campos.";
        }

        if (password == null || password.isEmpty()) {
            return "Por favor rellena todos los campos.";
        }

        return null;
    }

    public String validarRegistro(String nombre, String email, String pass, String pass2) {
        if (nombre == null || nombre.trim().length() == 0) {
            return "Rellena todos los campos obligatorios.";
        }

        if (email == null || email.isBlank()) {
            return "Rellena todos los campos obligatorios.";
        }

        if (pass == null || pass.length() == 0) {
            return "Rellena todos los campos obligatorios.";
        }

        if (!pass.equals(pass2)) {
            return "Las contraseñas no coinciden.";
        }

        if (pass.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }

        return null;
    }

    public String validarDatosBasicos(String nombre, String email) {
        if (nombre == null || nombre.trim().equals("")) {
            return "Nombre y email son obligatorios.";
        }

        if (email == null || email.trim().equals("")) {
            return "Nombre y email son obligatorios.";
        }

        return null;
    }

    public String validarCambioPassword(String actual, String nueva, String confirma) {
        if (actual == null || actual.isEmpty()) {
            return "Rellena todos los campos.";
        }

        if (nueva == null || nueva.isEmpty()) {
            return "Rellena todos los campos.";
        }

        if (confirma == null || confirma.isEmpty()) {
            return "Rellena todos los campos.";
        }

        if (!nueva.equals(confirma)) {
            return "Las contraseñas no coinciden.";
        }

        if (nueva.length() < 8) {
            return "La nueva contraseña debe tener al menos 8 caracteres.";
        }

        return null;
    }
}
