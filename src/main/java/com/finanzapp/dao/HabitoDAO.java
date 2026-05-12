package com.finanzapp.dao;

import com.finanzapp.model.Habito;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class HabitoDAO {


    public List<Habito> obtenerHabitos(int usuarioId) {
        List<Habito> lista = new ArrayList<>();
        String sqlHabitos = "SELECT * FROM habitos WHERE usuario_id=? ORDER BY id";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlHabitos)) {

            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Habito habito = mapearHabito(resultados);
                lista.add(habito);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return lista;
    }


    public boolean registrarHabito(Habito habito) {
        String insertarHabito =
                "INSERT INTO habitos " +
                "(usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertarHabito, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, habito.getUsuarioId());
            stmt.setString(2, habito.getEmoji());
            stmt.setString(3, habito.getNombre());
            stmt.setInt(4, habito.getFrecuenciaActual());
            stmt.setInt(5, habito.getFrecuenciaObj());
            stmt.setString(6, habito.getUnidad());
            stmt.setDouble(7, habito.getCoste());
            stmt.setString(8, habito.getDescripcion());

            int insertadas = stmt.executeUpdate();

            if (insertadas > 0) {
                ResultSet idGene = stmt.getGeneratedKeys();
                if (idGene.next()) {
                    habito.setId(idGene.getInt(1));
                }
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public boolean actualizarFrecuencia(Habito habito) {
        String sqlUpdate =
                "UPDATE habitos SET frecuencia_actual=?, frecuencia_obj=? WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlUpdate)) {

            stmt.setInt(1, habito.getFrecuenciaActual());
            stmt.setInt(2, habito.getFrecuenciaObj());
            stmt.setInt(3, habito.getId());

            int actualizadas = stmt.executeUpdate();
            boolean actualizacionExitosa = actualizadas > 0;
            return actualizacionExitosa;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public boolean eliminarHabito(int habitoId) {
        String eliminarHabito = "DELETE FROM habitos WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarHabito)) {

            stmt.setInt(1, habitoId);
            int borradas = stmt.executeUpdate();
            boolean eliminacionExitosa = borradas > 0;
            return eliminacionExitosa;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /** Construye un objeto Habito a partir de la fila actual del ResultSet. */
    private Habito mapearHabito(ResultSet resultados) throws SQLException {
        Habito habito = new Habito();

        habito.setId(resultados.getInt("id"));
        habito.setUsuarioId(resultados.getInt("usuario_id"));
        habito.setEmoji(resultados.getString("emoji"));
        habito.setNombre(resultados.getString("nombre"));
        habito.setFrecuenciaActual(resultados.getInt("frecuencia_actual"));
        habito.setFrecuenciaObj(resultados.getInt("frecuencia_obj"));
        habito.setUnidad(resultados.getString("unidad"));
        habito.setCoste(resultados.getDouble("coste"));
        habito.setDescripcion(resultados.getString("descripcion"));

        return habito;
    }
}
