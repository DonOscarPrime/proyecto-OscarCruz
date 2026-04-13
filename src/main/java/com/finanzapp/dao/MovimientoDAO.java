package com.finanzapp.dao;

import com.finanzapp.model.Movimiento;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    public List<Movimiento> listarPorUsuario(int usuarioId) {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                     "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                     "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /** Movimientos del mes y año indicados. */
    public List<Movimiento> listarPorMes(int usuarioId, int año, int mes) {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                     "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                     "WHERE m.usuario_id = ? AND YEAR(m.fecha)=? AND MONTH(m.fecha)=? " +
                     "ORDER BY m.fecha DESC, m.id DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, año);
            ps.setInt(3, mes);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /** Últimos N movimientos. */
    public List<Movimiento> listarUltimos(int usuarioId, int limit) {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT m.*, c.nombre AS cat_nombre, c.emoji AS cat_emoji " +
                     "FROM movimientos m LEFT JOIN categorias c ON m.categoria_id = c.id " +
                     "WHERE m.usuario_id = ? ORDER BY m.fecha DESC, m.id DESC LIMIT ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean insertar(Movimiento m) {
        String sql = "INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getUsuarioId());
            ps.setString(2, m.getTipo());
            ps.setString(3, m.getNombre());
            ps.setDouble(4, m.getCantidad());
            if (m.getCategoriaId() > 0) ps.setInt(5, m.getCategoriaId());
            else                        ps.setNull(5, Types.INTEGER);
            ps.setString(6, m.getNotas());
            ps.setDate(7, Date.valueOf(m.getFecha() != null ? m.getFecha() : LocalDate.now()));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) m.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM movimientos WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Movimiento mapRow(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setId(rs.getInt("id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setTipo(rs.getString("tipo"));
        m.setNombre(rs.getString("nombre"));
        m.setCantidad(rs.getDouble("cantidad"));
        m.setCategoriaId(rs.getInt("categoria_id"));
        m.setCategoriaNombre(rs.getString("cat_nombre"));
        m.setCategoriaEmoji(rs.getString("cat_emoji"));
        m.setNotas(rs.getString("notas"));
        Date d = rs.getDate("fecha");
        m.setFecha(d != null ? d.toLocalDate() : null);
        return m;
    }
}
