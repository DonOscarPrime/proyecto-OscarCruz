package com.finanzapp.service;

import com.finanzapp.dao.HabitoDAO;
import com.finanzapp.model.Habito;

import java.util.List;

/**
 * Lógica de negocio para hábitos de gasto de Fox Wallet.
 * Calcula el ahorro potencial mensual/anual y delega el acceso
 * a datos en HabitoDAO.
 */
public class HabitoService {

    private final HabitoDAO dao;

    public HabitoService(HabitoDAO dao) {
        this.dao = dao;
    }

    public List<Habito> obtenerDeUsuario(int idUsuario) {
        return dao.obtenerHabitos(idUsuario);
    }

    public boolean registrar(Habito habito) {
        return dao.registrarHabito(habito);
    }

    public void eliminar(int idHabito) {
        dao.eliminarHabito(idHabito);
    }

    public void actualizarFrecuencias(Habito habito) {
        dao.actualizarFrecuencia(habito);
    }

    public double calcularGastoMensualTotal(List<Habito> habitos) {
        double suma = 0;
        for (Habito hab : habitos) {
            double gastoMensualHabito = hab.getGastoActual();
            suma = suma + gastoMensualHabito;
        }
        return suma;
    }

    public double calcularObjetivoMensualTotal(List<Habito> habitos) {
        double acum = 0;
        for (Habito hab : habitos) {
            double gastoObjetivoHabito = hab.getGastoObjetivo();
            acum = acum + gastoObjetivoHabito;
        }
        return acum;
    }

    public double calcularAhorroMensual(double gastoActual, double gastoObjetivo) {
        double ahorroMensual = gastoActual - gastoObjetivo;
        return ahorroMensual;
    }

    public double calcularAhorroAnual(double ahorroMensual) {
        double ahorroAnual = ahorroMensual * 12;
        return ahorroAnual;
    }
}
