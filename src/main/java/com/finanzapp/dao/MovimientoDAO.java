package com.finanzapp.dao;

import com.finanzapp.model.Movimiento;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code movimientos} de Fox Wallet.
 * <p>
 * Un movimiento representa cualquier transacción económica del usuario:
 * un gasto (p. ej. supermercado, transporte) o un ingreso (nómina, freelance).
 * Incluye JOIN con {@code categorias} para obtener el nombre y emoji de la categoría.
 */
public class MovimientoDAO {

    /**
     * Devuelve todos los movimientos del usuario ordenados del más reciente al más antiguo.
     * Se usa en el historial completo de transacciones.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public List<Movimiento> obtenerMovimientosDeUsuario(int usuarioId) {
        List<Movimiento> movimientos = new ArrayList<>();
        String consultaTodosLosMovimientos =
                "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaTodosLosMovimientos)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) movimientos.add(mapearMovimiento(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return movimientos;
    }

    /**
     * Devuelve los movimientos de un mes y año concretos.
     * Se usa en el dashboard para calcular ingresos, gastos y saldo del mes actual.
     *
     * @param usuarioId identificador del usuario en sesión
     * @param año       año del periodo a consultar (p. ej. 2025)
     * @param mes       mes del periodo (1–12)
     */
    public List<Movimiento> obtenerMovimientosPorMes(int usuarioId, int año, int mes) {
        List<Movimiento> movimientos = new ArrayList<>();
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
            while (resultados.next()) movimientos.add(mapearMovimiento(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return movimientos;
    }

    /**
     * Devuelve los últimos N movimientos del usuario.
     * Se usa en el dashboard para mostrar la lista de transacciones recientes.
     *
     * @param usuarioId  identificador del usuario en sesión
     * @param maxResultados número máximo de movimientos a devolver
     */
    public List<Movimiento> obtenerUltimosMovimientos(int usuarioId, int maxResultados) {
        List<Movimiento> movimientos = new ArrayList<>();
        String consultaUltimosMovimientos =
                "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC LIMIT ?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaUltimosMovimientos)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, maxResultados);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) movimientos.add(mapearMovimiento(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return movimientos;
    }

    /**
     * Persiste un nuevo movimiento (gasto o ingreso) en la base de datos
     * y asigna el id generado al objeto recibido.
     *
     * @param movimiento movimiento con todos sus campos rellenos
     * @return {@code true} si la inserción fue exitosa
     */
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
            if (movimiento.getCategoriaId() > 0) stmt.setInt(5, movimiento.getCategoriaId());
            else                                  stmt.setNull(5, Types.INTEGER);
            stmt.setString(6, movimiento.getNotas());
            stmt.setDate(7, Date.valueOf(
                    movimiento.getFecha() != null ? movimiento.getFecha() : LocalDate.now()));
            int filasInsertadas = stmt.executeUpdate();
            if (filasInsertadas > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) movimiento.setId(idGenerado.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Elimina un movimiento del historial financiero por su id.
     *
     * @param movimientoId id del movimiento a borrar
     * @return {@code true} si se eliminó correctamente
     */
    public boolean eliminarMovimiento(int movimientoId) {
        String eliminarMovimiento = "DELETE FROM movimientos WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarMovimiento)) {
            stmt.setInt(1, movimientoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Construye un objeto {@link Movimiento} a partir de la fila actual del {@link ResultSet}. */
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
        Date fechaSQL = resultados.getDate("fecha");
        movimiento.setFecha(fechaSQL != null ? fechaSQL.toLocalDate() : null);
        return movimiento;
    }
}
