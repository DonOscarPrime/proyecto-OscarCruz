package com.finanzapp.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio que limita los intentos de login fallidos por email.
 * Bloquea la cuenta durante {@link #BLOQUEO_MINUTOS} minutos
 * tras {@link #MAX_INTENTOS} fallos consecutivos.
 */
public class LoginAttemptService {

    public static final int MAX_INTENTOS    = 5;
    public static final int BLOQUEO_MINUTOS = 15;

    /** Mapa email → [nº intentos fallidos, momento del primer fallo] */
    private static final Map<String, int[]>          intentos  = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime>  bloqueos  = new ConcurrentHashMap<>();

    private LoginAttemptService() { }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Registra un fallo de login para el email dado.
     * Si se supera el límite, bloquea la cuenta.
     */
    public static void registrarFallo(String email) {
        String key = email.toLowerCase();
        int[] cont = intentos.computeIfAbsent(key, k -> new int[]{0});
        cont[0]++;
        if (cont[0] >= MAX_INTENTOS) {
            bloqueos.put(key, LocalDateTime.now());
        }
    }

    /**
     * Registra un login correcto: resetea el contador del email.
     */
    public static void registrarExito(String email) {
        String key = email.toLowerCase();
        intentos.remove(key);
        bloqueos.remove(key);
    }

    /**
     * Devuelve {@code true} si el email está bloqueado temporalmente.
     */
    public static boolean estaBloqueado(String email) {
        String key = email.toLowerCase();
        LocalDateTime desde = bloqueos.get(key);
        if (desde == null) return false;
        if (LocalDateTime.now().isAfter(desde.plusMinutes(BLOQUEO_MINUTOS))) {
            // Bloqueo expirado: limpiar
            bloqueos.remove(key);
            intentos.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Devuelve el número de intentos fallidos acumulados para el email.
     */
    public static int getIntentos(String email) {
        int[] cont = intentos.get(email.toLowerCase());
        return (cont == null) ? 0 : cont[0];
    }

    /** Resetea todo el estado (útil para tests). */
    public static void reset() {
        intentos.clear();
        bloqueos.clear();
    }
}
