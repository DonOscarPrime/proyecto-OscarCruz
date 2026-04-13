package com.finanzapp.dao;

import com.finanzapp.model.Objetivo;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjetivoDAO {

    public List<Objetivo> listarPorUsuario(int usuarioId) {
        List<Objetivo> lista = new ArrayList<>();
        String sql = "SELECT * FROM objetivos WHERE usuario_id=? ORDER BY completado ASC, created_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean insertar(Objetivo o) {
        String sql = "INSERT INTO objetivos (usuario_id, nombre, objetivo, actual, emoji, fecha_limite) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getUsuarioId());
            ps.setString(2, o.getNombre());
            ps.setDouble(3, o.getObjetivo());
            ps.setDouble(4, o.getActual());
            ps.setString(5, o.getEmoji() != null ? o.getEmoji() : "🎯");
            ps.setObject(6, o.getFechaLimite());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) o.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean actualizarAporte(int id, double nuevoActual) {
        String sql = "UPDATE objetivos SET actual=?, completado=IF(actual>=objetivo,1,0) WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, nuevoActual);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM objetivos WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Objetivo mapRow(ResultSet rs) throws SQLException {
        Objetivo o = new Objetivo();
        o.setId(rs.getInt("id"));
        o.setUsuarioId(rs.getInt("usuario_id"));
        o.setNombre(rs.getString("nombre"));
        o.setObjetivo(rs.getDouble("objetivo"));
        o.setActual(rs.getDouble("actual"));
        o.setEmoji(rs.getString("emoji"));
        Date fl = rs.getDate("fecha_limite");
        o.setFechaLimite(fl != null ? fl.toLocalDate() : null);
        o.setCompletado(rs.getBoolean("completado"));
        return o;
    }
}
