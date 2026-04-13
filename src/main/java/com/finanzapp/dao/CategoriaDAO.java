package com.finanzapp.dao;

import com.finanzapp.model.Categoria;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY id";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Categoria> listarPorTipo(String tipo) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias WHERE tipo=? OR tipo='ambos' ORDER BY id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Categoria mapRow(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria();
        cat.setId(rs.getInt("id"));
        cat.setNombre(rs.getString("nombre"));
        cat.setEmoji(rs.getString("emoji"));
        cat.setTipo(rs.getString("tipo"));
        cat.setColor(rs.getString("color"));
        return cat;
    }
}
