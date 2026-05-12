package com.finanzapp.dao;

import com.finanzapp.model.Objetivo;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra los objetivos de ahorro del usuario.
 * Permite crear, consultar y actualizar metas
 */
public class ObjetivoDAO {


    public List<Objetivo> obtenerObjetivosUsuario(int usuarioId) {
        List<Objetivo> lista = new ArrayList<>();

        String sqlObjetivos =
                "SELECT * FROM objetivos WHERE usuario_id=? ORDER BY completado ASC, created_at DESC";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlObjetivos)) {

            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Objetivo obj = mapearObjetivo(resultados);
                lista.add(obj);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }

    public boolean crearObjetivo(Objetivo objetivo) {
        String sqlInsert =
                "INSERT INTO objetivos " +
                "(usuario_id, nombre, objetivo, actual, emoji, fecha_limite) " +
                "VALUES (?,?,?,?,?,?)";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, objetivo.getUsuarioId());
            stmt.setString(2, objetivo.getNombre());
            stmt.setDouble(3, objetivo.getObjetivo());
            stmt.setDouble(4, objetivo.getActual());

            String emoji = objetivo.getEmoji();
            if (emoji != null) {
                stmt.setString(5, emoji);
            } else {
                stmt.setString(5, "🎯");
            }

            stmt.setObject(6, objetivo.getFechaLimite());

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) {
                    objetivo.setId(idGenerado.getInt(1));
                }
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public boolean registrarAporte(int objetivoId, double importeActual) {
        String llamadaProcedimiento = "{CALL actualizarProgresoObjetivo(?, ?)}";

        try (Connection conexion = DatabaseConnection.getConnection();
             CallableStatement stmt = conexion.prepareCall(llamadaProcedimiento)) {

            stmt.setInt(1, objetivoId);
            stmt.setDouble(2, importeActual);
            stmt.execute();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean eliminarObjetivo(int objetivoId) {
        String eliminarObjetivo = "DELETE FROM objetivos WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarObjetivo)) {

            stmt.setInt(1, objetivoId);
            int borradas = stmt.executeUpdate();
            boolean eliminacionExitosa = borradas > 0;
            return eliminacionExitosa;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    private Objetivo mapearObjetivo(ResultSet resultados) throws SQLException {
        Objetivo objetivo = new Objetivo();

        objetivo.setId(resultados.getInt("id"));
        objetivo.setUsuarioId(resultados.getInt("usuario_id"));
        objetivo.setNombre(resultados.getString("nombre"));
        objetivo.setObjetivo(resultados.getDouble("objetivo"));
        objetivo.setActual(resultados.getDouble("actual"));
        objetivo.setEmoji(resultados.getString("emoji"));

        Date limite = resultados.getDate("fecha_limite");
        if (limite != null) {
            java.time.LocalDate fechaLimiteLocal = limite.toLocalDate();
            objetivo.setFechaLimite(fechaLimiteLocal);
        } else {
            objetivo.setFechaLimite(null);
        }

        objetivo.setCompletado(resultados.getBoolean("completado"));

        return objetivo;
    }
}
