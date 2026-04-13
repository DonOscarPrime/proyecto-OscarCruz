package com.finanzapp.dao;

import com.finanzapp.model.Notificacion;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public List<Notificacion> listarPorUsuario(int usuarioId) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE usuario_id=? ORDER BY created_at DESC LIMIT 30";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public long contarNoLeidas(int usuarioId) {
        String sql = "SELECT COUNT(*) FROM notificaciones WHERE usuario_id=? AND leida=0";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void marcarTodasLeidas(int usuarioId) {
        String sql = "UPDATE notificaciones SET leida=1 WHERE usuario_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean insertar(Notificacion n) {
        String sql = "INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n.getUsuarioId());
            ps.setString(2, n.getTitulo());
            ps.setString(3, n.getMensaje());
            ps.setString(4, n.getTipo() != null ? n.getTipo() : "info");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Notificacion mapRow(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setId(rs.getInt("id"));
        n.setUsuarioId(rs.getInt("usuario_id"));
        n.setTitulo(rs.getString("titulo"));
        n.setMensaje(rs.getString("mensaje"));
        n.setTipo(rs.getString("tipo"));
        n.setLeida(rs.getBoolean("leida"));
        Timestamp ts = rs.getTimestamp("created_at");
        n.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return n;
    }
}
