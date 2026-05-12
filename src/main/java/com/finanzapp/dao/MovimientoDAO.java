package com.finanzapp.dao;

import com.finanzapp.model.Movimiento;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el acceso a los movimientos financieros del usuario.
 * Permite consultar, registrar y eliminar gastos e ingresos.
 */
public class MovimientoDAO {

    /**
     * Obtiene el historial completo de movimientos del usuario,
     * ordenado de más reciente a más antiguo.
     */
    public List<Movimiento> obtenerMovimientosDeUsuario(int usuarioId) {
        List<Movimiento> lista = new ArrayList<>();

        String consultaMovimientos =
                "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaMovimientos)) {

            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Movimiento mov = mapearMovimiento(resultados);
                lista.add(mov);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    /**
     Los movimientos correspondientes a un mes concreto.
     */
    public List<Movimiento> obtenerMovimientosMes(int usuarioId, int año, int mes) {
        List<Movimiento> lista = new ArrayList<>();

        String consultaMovimientosMensuales =
                "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                "WHERE m.usuario_id = ? AND YEAR(m.fecha)=? AND MONTH(m.fecha)=? " +
                "ORDER BY m.fecha DESC, m.id DESC";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaMovimientosMensuales)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, año);
            stmt.setInt(3, mes);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Movimiento mov = mapearMovimiento(resultados);
                lista.add(mov);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    /**
     * Devuelve los movimientos más recientes.
     */
    public List<Movimiento> obtenerUltimosMovimientos(int usuarioId, int maxResultados) {
        List<Movimiento> lista = new ArrayList<>();

        String consultaUltimoMovimiento =
                "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC LIMIT ?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaUltimoMovimiento)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, maxResultados);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Movimiento mov = mapearMovimiento(resultados);
                lista.add(mov);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    public boolean registrarMovimiento(Movimiento movimiento) {
        String insertarMovimiento =
                "INSERT INTO movimientos " +
                "(usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) " +
                "VALUES (?,?,?,?,?,?,?)";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertarMovimiento, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, movimiento.getUsuarioId());
            stmt.setString(2, movimiento.getTipo());
            stmt.setString(3, movimiento.getNombre());
            stmt.setDouble(4, movimiento.getCantidad());

            if (movimiento.getCategoriaId() > 0) {
                stmt.setInt(5, movimiento.getCategoriaId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, movimiento.getNotas());

            LocalDate fechaMovimiento = movimiento.getFecha();
            if (fechaMovimiento != null) {
                Date fecha = Date.valueOf(fechaMovimiento);
                stmt.setDate(7, fecha);
            } else {
                LocalDate fechaHoy = LocalDate.now();
                Date fechaHoySQL = Date.valueOf(fechaHoy);
                stmt.setDate(7, fechaHoySQL);
            }

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                ResultSet clave = stmt.getGeneratedKeys();
                if (clave.next()) {
                    movimiento.setId(clave.getInt(1));
                }
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean eliminarMovimiento(int movimientoId) {
        String eliminarMovimiento = "DELETE FROM movimientos WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarMovimiento)) {

            stmt.setInt(1, movimientoId);
            int borradas = stmt.executeUpdate();

            if (borradas > 0) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public double obtenerTotalPorTipoYMes(int usuarioId, int anio, int mes, String tipo) {
        String consulta = "SELECT COALESCE(SUM(cantidad), 0) FROM movimientos " +
                "WHERE usuario_id=? AND YEAR(fecha)=? AND MONTH(fecha)=? AND tipo=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consulta)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, anio);
            stmt.setInt(3, mes);
            stmt.setString(4, tipo);
            ResultSet resultados = stmt.executeQuery();
            if (resultados.next()) {
                double totalObtenido = resultados.getDouble(1);
                return totalObtenido;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Construye un objeto Movimiento a partir de la fila actual del ResultSet. */
    private Movimiento mapearMovimiento(ResultSet resultados) throws SQLException {
        Movimiento movimiento = new Movimiento();

        movimiento.setId(resultados.getInt("id"));
        movimiento.setUsuarioId(resultados.getInt("usuario_id"));
        movimiento.setTipo(resultados.getString("tipo"));
        movimiento.setNombre(resultados.getString("nombre"));
        movimiento.setCantidad(resultados.getDouble("cantidad"));
        movimiento.setCategoriaId(resultados.getInt("categoria_id"));
        movimiento.setCategoriaNombre(resultados.getString("cat_nombre"));
        movimiento.setCategoriaEmoji(resultados.getString("cat_emoji"));
        movimiento.setNotas(resultados.getString("notas"));

        Date fecha = resultados.getDate("fecha");
        if (fecha != null) {
            LocalDate fechaLocal = fecha.toLocalDate();
            movimiento.setFecha(fechaLocal);
        } else {
            movimiento.setFecha(null);
        }

        return movimiento;
    }
}
