package com.finanzapp.dao;

import com.finanzapp.model.Categoria;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla categorias de Fox Wallet
 * Permite obtener y filtrar las categorías de gastos e ingresos
 */
public class CategoriaDAO {

    /**
     * Muestra todas las categorías disponibles ordenadas por id.
     */
    public List<Categoria> obtenerTodasLasCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Categoria categoria = mapearCategoria(rs);
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorias;
    }

    public List<Categoria> obtenerCategoriasPorTipo(String tipoMovimiento) {
        List<Categoria> categorias = new ArrayList<>();
        String consultaCategoriasPorTipo =
                "SELECT * FROM categorias WHERE tipo=? OR tipo='ambos' ORDER BY id";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaCategoriasPorTipo)) {

            stmt.setString(1, tipoMovimiento);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Categoria categoria = mapearCategoria(resultados);
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorias;
    }

    /** Construye un objeto Categoria a partir de la fila actual del ResultSet. */
    private Categoria mapearCategoria(ResultSet resultados) throws SQLException {
        Categoria categoria = new Categoria();

        categoria.setId(resultados.getInt("id"));
        categoria.setNombre(resultados.getString("nombre"));
        categoria.setEmoji(resultados.getString("emoji"));
        categoria.setTipo(resultados.getString("tipo"));
        categoria.setColor(resultados.getString("color"));

        return categoria;
    }
}
