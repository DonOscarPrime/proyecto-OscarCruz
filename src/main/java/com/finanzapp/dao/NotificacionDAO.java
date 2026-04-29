package com.finanzapp.dao;

import com.finanzapp.model.Notificacion;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla {@code notificaciones} de Fox Wallet.
 * <p>
 * Fox Wallet genera notificaciones automáticas cuando el usuario supera
 * su presupuesto mensual, alcanza un objetivo de ahorro o recibe un
 * aviso de vencimiento de préstamo. Este DAO gestiona su lectura y estado.
 */
public class NotificacionDAO {

    /**
     * Devuelve las 30 notificaciones más recientes del usuario,
     * ordenadas de la más nueva a la más antigua.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public List<Notificacion> obtenerNotificacionesDeUsuario(int usuarioId) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String consultaNotificaciones =
                "SELECT * FROM notificaciones WHERE usuario_id=? ORDER BY created_at DESC LIMIT 30";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaNotificaciones)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            while (resultados.next()) notificaciones.add(mapearNotificacion(resultados));
        } catch (SQLException e) { e.printStackTrace(); }
        return notificaciones;
    }

    /**
     * Cuenta cuántas notificaciones no ha leído todavía el usuario.
     * El resultado se muestra en el badge rojo del icono de la campana.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public long contarNotificacionesNoLeidas(int usuarioId) {
        String consultaNoLeidas =
                "SELECT COUNT(*) FROM notificaciones WHERE usuario_id=? AND leida=0";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaNoLeidas)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            if (resultados.next()) return resultados.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * Marca todas las notificaciones del usuario como leídas.
     * Se llama automáticamente cuando el usuario abre el panel de notificaciones.
     *
     * @param usuarioId identificador del usuario en sesión
     */
    public void marcarTodasLasNotificacionesComoLeidas(int usuarioId) {
        String marcarLeidas = "UPDATE notificaciones SET leida=1 WHERE usuario_id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(marcarLeidas)) {
            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Persiste una nueva notificación del sistema en la base de datos.
     * Si el tipo es {@code null}, se asigna {@code "info"} por defecto.
     *
     * @param notificacion notificación a guardar con título, mensaje y tipo
     * @return {@code true} si la inserción fue exitosa
     */
    public boolean registrarNotificacion(Notificacion notificacion) {
        String insertarNotificacion =
                "INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo) VALUES (?,?,?,?)";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertarNotificacion)) {
            stmt.setInt(1, notificacion.getUsuarioId());
            stmt.setString(2, notificacion.getTitulo());
            stmt.setString(3, notificacion.getMensaje());
            stmt.setString(4, notificacion.getTipo() != null ? notificacion.getTipo() : "info");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Construye un objeto {@link Notificacion} a partir de la fila actual del {@link ResultSet}. */
    private Notificacion mapearNotificacion(ResultSet resultados) throws SQLException {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(resultados.getInt("id"));
        notificacion.setUsuarioId(resultados.getInt("usuario_id"));
        notificacion.setTitulo(resultados.getString("titulo"));
        notificacion.setMensaje(resultados.getString("mensaje"));
        notificacion.setTipo(resultados.getString("tipo"));
        notificacion.setLeida(resultados.getBoolean("leida"));
        Timestamp marcaTemporal = resultados.getTimestamp("created_at");
        notificacion.setCreatedAt(marcaTemporal != null ? marcaTemporal.toLocalDateTime() : null);
        return notificacion;
    }
}
