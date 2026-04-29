package com.finanzapp.dao;

import com.finanzapp.model.Objetivo;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// CallableStatement está incluido en java.sql.*

/**
 * Acceso a datos de la tabla {@code objetivos} de Fox Wallet.
 * <p>
 * Un objetivo de ahorro representa una meta financiera del usuario
 * (p. ej. "Fondo de emergencia", "Viaje a Japón", "Entrada del coche").
 * El progreso se actualiza a través del procedimiento almacenado
 * {@code actualizarProgresoObjetivo}, que marca el objetivo como
 * completado de forma atómica cuando el importe alcanza la meta.
 */
public class ObjetivoDAO {

    /**
     * Devuelve todos los objetivos de ahorro del usuario, mostrando primero
     * los pendientes y luego los ya completados.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public List<Objetivo> obtenerObjetivosDeUsuario(int usuarioId) {
        List<Objetivo> objetivos = new ArrayList<>();
        String consultaObjetivos =
                "SELECT * FROM objetivos WHERE usuario_id=? ORDER BY completado ASC, created_at DESC";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaObjetivos)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) objetivos.add(mapearObjetivo(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return objetivos;
    }

    /**
     * Persiste un nuevo objetivo de ahorro en la base de datos.
     * Si el emoji es {@code null}, se asigna 🎯 por defecto.
     *
     * @param objetivo objetivo con nombre, importe meta, aportación inicial y fecha límite
     * @return {@code true} si la inserción fue exitosa
     */
    public boolean crearObjetivo(Objetivo objetivo) {
        String insertarObjetivo =
                "INSERT INTO objetivos " +
                "(usuario_id, nombre, objetivo, actual, emoji, fecha_limite) " +
                "VALUES (?,?,?,?,?,?)";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertarObjetivo, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, objetivo.getUsuarioId());
            stmt.setString(2, objetivo.getNombre());
            stmt.setDouble(3, objetivo.getObjetivo());
            stmt.setDouble(4, objetivo.getActual());
            stmt.setString(5, objetivo.getEmoji() != null ? objetivo.getEmoji() : "🎯");
            stmt.setObject(6, objetivo.getFechaLimite());
            int filasInsertadas = stmt.executeUpdate();
            if (filasInsertadas > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) objetivo.setId(idGenerado.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Actualiza el importe ahorrado de un objetivo delegando la lógica en el
     * procedimiento almacenado {@code actualizarProgresoObjetivo}.
     * <p>
     * El servidor MySQL evalúa en el mismo UPDATE si el nuevo valor alcanza el
     * objetivo y actualiza {@code completado} de forma atómica, evitando la
     * condición de carrera que produciría realizar la comprobación en el cliente
     * en dos operaciones separadas.
     *
     * @param objetivoId   id del objetivo cuyo progreso se actualiza
     * @param importeActual nuevo importe acumulado por el usuario
     */
    public boolean registrarAporteObjetivo(int objetivoId, double importeActual) {
        String llamadaProcedimiento = "{CALL actualizarProgresoObjetivo(?, ?)}";
        try (Connection conexion = DatabaseConnection.getConnection();
             CallableStatement stmt = conexion.prepareCall(llamadaProcedimiento)) {
            stmt.setInt(1, objetivoId);
            stmt.setDouble(2, importeActual);
            stmt.execute();
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Elimina un objetivo de ahorro de Fox Wallet por su id.
     *
     * @param objetivoId id del objetivo a borrar
     * @return {@code true} si se eliminó correctamente
     */
    public boolean eliminarObjetivo(int objetivoId) {
        String eliminarObjetivo = "DELETE FROM objetivos WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(eliminarObjetivo)) {
            stmt.setInt(1, objetivoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Construye un objeto {@link Objetivo} a partir de la fila actual del {@link ResultSet}. */
    private Objetivo mapearObjetivo(ResultSet resultados) throws SQLException {
        Objetivo objetivo = new Objetivo();
        objetivo.setId(resultados.getInt("id"));
        objetivo.setUsuarioId(resultados.getInt("usuario_id"));
        objetivo.setNombre(resultados.getString("nombre"));
        objetivo.setObjetivo(resultados.getDouble("objetivo"));
        objetivo.setActual(resultados.getDouble("actual"));
        objetivo.setEmoji(resultados.getString("emoji"));
        Date fechaLimiteSQL = resultados.getDate("fecha_limite");
        objetivo.setFechaLimite(fechaLimiteSQL != null ? fechaLimiteSQL.toLocalDate() : null);
        objetivo.setCompletado(resultados.getBoolean("completado"));
        return objetivo;
    }
}
