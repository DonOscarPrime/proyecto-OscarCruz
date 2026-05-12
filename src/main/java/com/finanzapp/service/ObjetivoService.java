package com.finanzapp.service;

import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.model.Objetivo;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio para objetivos de ahorro de Fox Wallet.
 * Calcula aportes, filtra pendientes y delega el acceso
 * a datos en ObjetivoDAO.
 */
public class ObjetivoService {

    private final ObjetivoDAO dao;

    public ObjetivoService() {
        this.dao = new ObjetivoDAO();
    }

    public List<Objetivo> obtenerDeUsuario(int idUsuario) {
        return dao.obtenerObjetivosUsuario(idUsuario);
    }

    public boolean crear(Objetivo objetivo) {
        return dao.crearObjetivo(objetivo);
    }

    public void eliminar(int idObjetivo) {
        dao.eliminarObjetivo(idObjetivo);
    }

    public void aplicarAporte(Objetivo objetivo, double importeAporte) {
        System.out.println("[DEBUG] aplicarAporte id=" + objetivo.getId() + " importe=" + importeAporte);
        double totalActualizado = objetivo.getActual() + importeAporte;

        if (totalActualizado < 0) {
            totalActualizado = 0;
        }

        if (totalActualizado > objetivo.getObjetivo()) {
            totalActualizado = objetivo.getObjetivo();
        }

        int idObjetivo = objetivo.getId();
        dao.registrarAporte(idObjetivo, totalActualizado);
    }

    public List<Objetivo> obtenerPendientes(List<Objetivo> todos) {
        List<Objetivo> listaAbiertos = new ArrayList<>();
        for (Objetivo objetivo : todos) {
            if (!objetivo.iscompletado()) {
                listaAbiertos.add(objetivo);
            }
        }
        return listaAbiertos;
    }

    public List<Objetivo> obtenerPrimerosPendientes(List<Objetivo> todos, int max) {
        List<Objetivo> listaAbiertos = obtenerPendientes(todos);
        List<Objetivo> recortados = new ArrayList<>();
        int cuenta = 0;

        for (Objetivo objetivo : listaAbiertos) {
            if (cuenta >= max) {
                break;
            }
            recortados.add(objetivo);
            cuenta++;
        }

        return recortados;
    }

    public int contarPendientes(List<Objetivo> todos) {
        int cuenta = 0;
        for (Objetivo objetivo : todos) {
            if (!objetivo.iscompletado()) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
