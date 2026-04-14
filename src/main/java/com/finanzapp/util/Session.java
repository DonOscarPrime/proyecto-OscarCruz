package com.finanzapp.util;

import com.finanzapp.model.Usuario;

/** Almacena el usuario autenticado y preferencias de la sesión activa. */
public class Session {

    private static Session instance;
    private Usuario usuarioActual;
    private boolean darkMode = false;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public Usuario getUsuarioActual() { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { this.usuarioActual = u; }
    public void    cerrarSesion()  { usuarioActual = null; darkMode = false; }
    public boolean isLoggedIn()    { return usuarioActual != null; }

    public boolean isDarkMode()           { return darkMode; }
    public void    setDarkMode(boolean d) { this.darkMode = d; }
}
