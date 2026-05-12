package com.finanzapp.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginMonitorService {

    public static final int MAX_INTENTOS    = 5;
    public static final int BLOQUEO_MINUTOS = 15;

    private static final Map<String, int[]>         intentos = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> bloqueos = new ConcurrentHashMap<>();

    private LoginMonitorService() {
    }

    public static void registrarFallo(String email) {
        String emailEnMinusculas = email.toLowerCase();

        int[] contadorActual = intentos.get(emailEnMinusculas);
        if (contadorActual == null) {
            contadorActual = new int[]{0};
            intentos.put(emailEnMinusculas, contadorActual);
        }

        contadorActual[0] = contadorActual[0] + 1;

        if (contadorActual[0] >= MAX_INTENTOS) {
            bloqueos.put(emailEnMinusculas, LocalDateTime.now());
        }
    }

    public static void registrarExito(String email) {
        String emailEnMinusculas = email.toLowerCase();
        intentos.remove(emailEnMinusculas);
        bloqueos.remove(emailEnMinusculas);
    }

    public static boolean estaBloqueado(String email) {
        String emailEnMinusculas = email.toLowerCase();
        LocalDateTime momentoBloqueo = bloqueos.get(emailEnMinusculas);

        if (momentoBloqueo == null) {
            return false;
        }

        LocalDateTime momentoDesbloqueo = momentoBloqueo.plusMinutes(BLOQUEO_MINUTOS);
        LocalDateTime ahora = LocalDateTime.now();
        boolean bloqueoExpirado = ahora.isAfter(momentoDesbloqueo);

        if (bloqueoExpirado) {
            bloqueos.remove(emailEnMinusculas);
            intentos.remove(emailEnMinusculas);
            return false;
        }

        return true;
    }

    public static int getIntentos(String email) {
        String emailEnMinusculas = email.toLowerCase();
        int[] contadorActual = intentos.get(emailEnMinusculas);

        if (contadorActual == null) {
            return 0;
        } else {
            return contadorActual[0];
        }
    }

    public static void reset() {
        intentos.clear();
        bloqueos.clear();
    }
}
