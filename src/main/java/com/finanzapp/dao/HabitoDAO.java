package com.finanzapp.dao;

import com.finanzapp.model.Habito;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitoDAO {

    public List<Habito> listarPorUsuario(int usuarioId) {
        List<Habito> lista = new ArrayList<>();
        String sql = "SELECT * FROM habitos WHERE usuario_id=? ORDER BY id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean insertar(Habito h) {
        String sql = "INSERT INTO habitos (usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getUsuarioId());
            ps.setString(2, h.getEmoji());
            ps.setString(3, h.getNombre());
            ps.setInt(4, h.getFrecuenciaActual());
            ps.setInt(5, h.getFrecuenciaObj());
            ps.setString(6, h.getUnidad());
            ps.setDouble(7, h.getCoste());
            ps.setString(8, h.getDescripcion());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) h.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean actualizarFrecuencias(Habito h) {
        String sql = "UPDATE habitos SET frecuencia_actual=?, frecuencia_obj=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, h.getFrecuenciaActual());
            ps.setInt(2, h.getFrecuenciaObj());
            ps.setInt(3, h.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM habitos WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Habito mapRow(ResultSet rs) throws SQLException {
        Habito h = new Habito();
        h.setId(rs.getInt("id"));
        h.setUsuarioId(rs.getInt("usuario_id"));
        h.setEmoji(rs.getString("emoji"));
        h.setNombre(rs.getString("nombre"));
        h.setFrecuenciaActual(rs.getInt("frecuencia_actual"));
        h.setFrecuenciaObj(rs.getInt("frecuencia_obj"));
        h.setUnidad(rs.getString("unidad"));
        h.setCoste(rs.getDouble("coste"));
        h.setDescripcion(rs.getString("descripcion"));
        return h;
    }
}
