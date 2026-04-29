package com.finanzapp.dao;

import com.finanzapp.model.Habito;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code habitos} de Fox Wallet.
 * <p>
 * Los hábitos representan gastos recurrentes del usuario (café, gimnasio,
 * suscripciones…) que el simulador de ahorro usa para calcular cuánto
 * podría ahorrar reduciendo su frecuencia de consumo.
 */
public class HabitoDAO {

    /**
     * Devuelve todos los hábitos registrados por un usuario en Fox Wallet,
     * ordenados por id de creación.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public List<Habito> obtenerHabitosDeUsuario(int usuarioId) {
        List<Habito> habitos = new ArrayList<>();
        String consultaHabitos = "SELECT * FROM habitos WHERE usuario_id=? ORDER BY id";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaHabitos)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) habitos.add(mapearHabito(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return habitos;
    }

    /**
     * Persiste un nuevo hábito de gasto en la base de datos y asigna
     * el id generado al objeto recibido.
     *
     * @param habito hábito a guardar con todos sus campos rellenos
     * @return {@code true} si la inserción fue exitosa
     */
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
            int filasInsertadas = stmt.executeUpdate();
            if (filasInsertadas > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) habito.setId(idGenerado.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Actualiza las frecuencias actual y objetivo de un hábito existente.
     * Se llama desde el simulador cuando el usuario ajusta los deslizadores.
     *
     * @param habito hábito con las nuevas frecuencias ya asignadas
     * @return {@code true} si se actualizó al menos una fila
     */
    public boolean actualizarFrecuenciasHabito(Habito habito) {
        String actualizarFrecuencias =
                "UPDATE habitos SET frecuencia_actual=?, frecuencia_obj=? WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(actualizarFrecuencias)) {
            stmt.setInt(1, habito.getFrecuenciaActual());
            stmt.setInt(2, habito.getFrecuenciaObj());
            stmt.setInt(3, habito.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Elimina un hábito del simulador de ahorro por su id.
     *
     * @param habitoId id del hábito a borrar
     * @return {@code true} si se eliminó correctamente
     */
    public boolean eliminarHabito(int habitoId) {
        String eliminarHabito = "DELETE FROM habitos WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarHabito)) {
            stmt.setInt(1, habitoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Construye un objeto {@link Habito} a partir de la fila actual del {@link ResultSet}. */
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
