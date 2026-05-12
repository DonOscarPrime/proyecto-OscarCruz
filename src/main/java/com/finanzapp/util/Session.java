package com.finanzapp.util;

import com.finanzapp.model.Usuario;

public class Session {

    private static Session instancia;
    private Usuario usuarioEnSesion;
    private boolean modoOscuroActivo = false;

    private Session() {
    }

    public static Session getInstance() {
        if (instancia == null) {
            instancia = new Session();
        }
        return instancia;
    }


    public Usuario getUsuarioActual() {
        if (usuarioEnSesion == null) {
            return null;
        }
        return usuarioEnSesion;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioEnSesion = usuario;
    }

    public void cerrarSesion() {
        usuarioEnSesion = null;
        modoOscuroActivo = false;
    }

    public boolean isLoggedIn() {
        if (usuarioEnSesion != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDarkMode() {
        return modoOscuroActivo;
    }

    public void setDarkMode(boolean activar) {
        this.modoOscuroActivo = activar;
    }
}
