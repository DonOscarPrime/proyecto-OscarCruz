package com.finanzapp.service;

import com.finanzapp.model.Movimiento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepara los datos del panel principal (dashboard) de Fox Wallet.
 * Agrupa gastos por categoría para el donut y calcula las series
 * de ingresos/gastos mensuales para el gráfico de barras.
 */
public class DashboardService {

    public Map<String, Double> agruparPorCategoria(List<Movimiento> movimientos) {
        Map<String, Double> gastosCategoria = new LinkedHashMap<>();

        for (Movimiento mov : movimientos) {
            if (mov.isIngreso()) {
                continue;
            }

            String categoriaNombreRaw = mov.getCategoriaNombre();
            String nombreCategoria;
            if (categoriaNombreRaw != null) {
                nombreCategoria = categoriaNombreRaw;
            } else {
                nombreCategoria = "Otro";
            }

            double suma = 0;
            if (gastosCategoria.containsKey(nombreCategoria)) {
                suma = gastosCategoria.get(nombreCategoria);
            }
            double cantidadMovimiento = mov.getCantidad();
            double nuevoAcumulado = suma + cantidadMovimiento;
            gastosCategoria.put(nombreCategoria, nuevoAcumulado);
        }

        return ordenarDescendente(gastosCategoria);
    }

    private Map<String, Double> ordenarDescendente(Map<String, Double> mapa) {
        List<Map.Entry<String, Double>> entradas = new ArrayList<>(mapa.entrySet());
        java.util.Collections.sort(entradas, new java.util.Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                if (b.getValue() > a.getValue()) {
                    return 1;
                } else if (b.getValue() < a.getValue()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        Map<String, Double> ordenado = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entrada : entradas) {
            ordenado.put(entrada.getKey(), entrada.getValue());
        }
        return ordenado;
    }

    public double calcularGastos(Map<String, Double> gastosCategoria) {
        double importe = 0;
        for (double valor : gastosCategoria.values()) {
            importe = importe + valor;
        }
        return importe;
    }

    public double calcularTotalIngreso(List<Movimiento> movimientos) {
        double acum = 0;
        for (Movimiento item : movimientos) {
            if (item.isIngreso()) {
                double cantidad = item.getCantidad();
                acum = acum + cantidad;
            }
        }
        return acum;
    }

    public double calcularTotalGastosMes(List<Movimiento> movimientos) {
        double total = 0;
        for (Movimiento item : movimientos) {
            if (!item.isIngreso()) {
                double cantidad = item.getCantidad();
                total = total + cantidad;
            }
        }
        return total;
    }

    public double calcularTasaAhorro(double ingresos, double balance) {
        if (ingresos <= 0) {
            return 0;
        }
        double cociente = balance / ingresos;
        double tasa = cociente * 100;
        return tasa;
    }
}
