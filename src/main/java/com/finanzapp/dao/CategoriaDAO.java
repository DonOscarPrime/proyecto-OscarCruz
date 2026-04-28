package com.finanzapp.dao;

import com.finanzapp.model.Categoria;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code categorias} de Fox Wallet.
 * <p>
 * Las categorías clasifican los movimientos del usuario (gastos e ingresos)
 * y se filtran por tipo para mostrar solo las opciones relevantes en cada
 * formulario del panel de movimientos.
 */
public class CategoriaDAO {

    /**
     * Devuelve todas las categorías del catálogo de Fox Wallet,
     * tanto las de gasto como las de ingreso, ordenadas por id.
     */
    public List<Categoria> obtenerTodasLasCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        String consultaCategorias = "SELECT * FROM categorias ORDER BY id";
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement stmt = conexion.createStatement();
             ResultSet resultados = stmt.executeQuery(consultaCategorias)) {
            while (resultados.next()) categorias.add(mapearCategoria(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return categorias;
    }

    /**
     * Devuelve las categorías aplicables a un tipo de movimiento concreto.
     * Incluye también las marcadas como {@code ambos} (p. ej. "Otros").
     *
     * @param tipoMovimiento {@code "gasto"} o {@code "ingreso"}
     */
    public List<Categoria> obtenerCategoriasPorTipo(String tipoMovimiento) {
        List<Categoria> categorias = new ArrayList<>();
        String consultaCategoriasPorTipo =
                "SELECT * FROM categorias WHERE tipo=? OR tipo='ambos' ORDER BY id";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaCategoriasPorTipo)) {
            stmt.setString(1, tipoMovimiento);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) categorias.add(mapearCategoria(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return categorias;
    }

    /** Construye un objeto {@link Categoria} a partir de la fila actual del {@link ResultSet}. */
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
