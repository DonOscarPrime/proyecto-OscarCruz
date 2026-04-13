package com.finanzapp.util;

import com.finanzapp.model.Usuario;

/** Almacena el usuario autenticado durante la sesión activa. */
public class Session {

    private static Session instance;
    private Usuario usuarioActual;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public Usuario getUsuarioActual() { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { this.usuarioActual = u; }
    public void    cerrarSesion()  { usuarioActual = null; }
    public boolean isLoggedIn()    { return usuarioActual != null; }
}
