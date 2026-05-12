package com.finanzapp.service;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.model.Movimiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio para movimientos de Fox Wallet.
 * Gestiona cálculos estadísticos, filtrado y delega el acceso
 * a datos en MovimientoDAO.
 */
public class MovimientoService {

    private final MovimientoDAO dao;

    public MovimientoService(MovimientoDAO dao) {
        this.dao = dao;
    }

    public List<Movimiento> obtenerDeUsuario(int idUsuario) {
        return dao.obtenerMovimientosDeUsuario(idUsuario);
    }

    public List<Movimiento> obtenerPorMes(int idUsuario, int anio, int mes) {
        return dao.obtenerMovimientosMes(idUsuario, anio, mes);
    }

    public List<Movimiento> obtenerUltimos(int idUsuario, int cantidad) {
        return dao.obtenerUltimosMovimientos(idUsuario, cantidad);
    }

    public boolean registrar(Movimiento movimiento) {
        return dao.registrarMovimiento(movimiento);
    }

    public void eliminar(int idMovimiento) {
        dao.eliminarMovimiento(idMovimiento);
    }

    public double calcularTotalIngresos(List<Movimiento> movimientos) {
        double total = 0;
        for (Movimiento m : movimientos) {
            if (m.isIngreso()) {
                total = total + m.getCantidad();
            }
        }
        return total;
    }

    public double calcularTotalGastos(List<Movimiento> movimientos) {
        double total = 0;
        for (Movimiento m : movimientos) {
            if (!m.isIngreso()) {
                total = total + m.getCantidad();
            }
        }
        return total;
    }

    public double calcularBalance(double ingresos, double gastos) {
        double balance = ingresos - gastos;
        return balance;
    }

    public double calcularTasaAhorro(double ingresos, double balance) {
        if (ingresos <= 0) {
            return 0;
        }
        double cociente = balance / ingresos;
        double tasa = cociente * 100;
        return tasa;
    }

    public List<Movimiento> filtrar(List<Movimiento> todos, String filtroTipo, String textoBusqueda) {
        String busquedaMinusculas = textoBusqueda.toLowerCase();
        return todos.stream()
                .filter(m -> cumpleFiltroTipo(m, filtroTipo))
                .filter(m -> cumpleBusqueda(m, busquedaMinusculas))
                .collect(java.util.stream.Collectors.toList());
    }

    public String etiquetaImporte(double cantidad, boolean esIngreso) {
        String cantidadFormateada = com.finanzapp.util.Formateador.moneda(cantidad);
        if (esIngreso) {
            String textoIngreso = "+" + cantidadFormateada + "€";
            return textoIngreso;
        }
        String textoGasto = "-" + cantidadFormateada + "€";
        return textoGasto;
    }

    private boolean cumpleFiltroTipo(Movimiento m, String filtroTipo) {
        if ("Gastos".equals(filtroTipo) && m.isIngreso()) {
            return false;
        }
        if ("Ingresos".equals(filtroTipo) && !m.isIngreso()) {
            return false;
        }
        return true;
    }

    private boolean cumpleBusqueda(Movimiento m, String busquedaMinusculas) {
        if (busquedaMinusculas.isEmpty()) {
            return true;
        }

        String nombreMovimiento = m.getNombre();
        String nombreEnMinusculas = nombreMovimiento.toLowerCase();
        boolean contieneEnNombre = nombreEnMinusculas.contains(busquedaMinusculas);
        if (contieneEnNombre) {
            return true;
        }

        if (m.getCategoriaNombre() != null) {
            String categoriaNombre = m.getCategoriaNombre();
            String categoriaEnMinusculas = categoriaNombre.toLowerCase();
            boolean contieneEnCategoria = categoriaEnMinusculas.contains(busquedaMinusculas);
            if (contieneEnCategoria) {
                return true;
            }
        }

        return false;
    }
}
