package com.finanzapp.util;

public class LoginAttemptService {
    public static final int MAX_INTENTOS    = LoginMonitorService.MAX_INTENTOS;
    public static final int BLOQUEO_MINUTOS = LoginMonitorService.BLOQUEO_MINUTOS;
    public static void registrarFallo(String email) { LoginMonitorService.registrarFallo(email); }
    public static void registrarExito(String email) { LoginMonitorService.registrarExito(email); }
    public static boolean estaBloqueado(String email) { return LoginMonitorService.estaBloqueado(email); }
    public static int getIntentos(String email) { return LoginMonitorService.getIntentos(email); }
    public static void reset() { LoginMonitorService.reset(); }
}
