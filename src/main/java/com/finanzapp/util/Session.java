package com.finanzapp.util;

import com.finanzapp.model.Usuario;

/**
 * Singleton que mantiene el estado de la sesión activa en Fox Wallet.
 * <p>
 * Almacena el {@link Usuario} autenticado y la preferencia de tema visual
 * (claro/oscuro) durante toda la vida de la aplicación. Al cerrar sesión
 * o al salir de la app, el estado se resetea completamente.
 */
public class Session {

    private static Session instancia;
    private Usuario usuarioEnSesion;
    private boolean modoOscuroActivo = false;

    private Session() {}

    /**
     * Devuelve la instancia única de la sesión de Fox Wallet.
     * La crea si es la primera vez que se llama.
     */
    public static Session getInstance() {
        if (instancia == null) instancia = new Session();
        return instancia;
    }

    /** Devuelve el usuario actualmente autenticado en Fox Wallet. */
    public Usuario getUsuarioActual() { return usuarioEnSesion; }

    /** Establece el usuario autenticado al iniciar sesión en Fox Wallet. */
    public void setUsuarioActual(Usuario usuario) { this.usuarioEnSesion = usuario; }

    /** Cierra la sesión actual reseteando el usuario y el tema visual. */
    public void cerrarSesion() { usuarioEnSesion = null; modoOscuroActivo = false; }

    /** Devuelve {@code true} si hay un usuario autenticado en Fox Wallet. */
    public boolean isLoggedIn() { return usuarioEnSesion != null; }

    /** Devuelve {@code true} si el tema oscuro está activo. */
    public boolean isDarkMode() { return modoOscuroActivo; }

    /** Activa o desactiva el modo oscuro en la sesión actual. */
    public void setDarkMode(boolean activar) { this.modoOscuroActivo = activar; }
}
