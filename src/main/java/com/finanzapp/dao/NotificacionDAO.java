package com.finanzapp.dao;

import com.finanzapp.model.Notificacion;
import com.finanzapp.util.DatabaseConnection;
import com.finanzapp.util.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Se encarga de las notificaciones generadas por la aplicación.
 * Permite consultar, guardar y actualizar avisos del usuario.
 */
public class NotificacionDAO {

    /**
     * Recupera las notificaciones más recientes del usuario.
     */
    public List<Notificacion> obtenerNotificaciones(int usuarioId) {
        List<Notificacion> lista = new ArrayList<>();

        String sqlNotifs =
                "SELECT * FROM notificaciones WHERE usuario_id=? ORDER BY created_at DESC LIMIT 30";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlNotifs)) {

            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();

            while (resultados.next()) {
                Notificacion notif = mapearNotificacion(resultados);
                lista.add(notif);
            }

        } catch (SQLException error) {
            error.printStackTrace();
        }

        return lista;
    }

    public boolean registrarNotificacion(Notificacion notif) {
        String sqlInsert =
                "INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo) VALUES (?,?,?,?)";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sqlInsert)) {

            stmt.setInt(1, notif.getUsuarioId());
            stmt.setString(2, notif.getTitulo());
            stmt.setString(3, notif.getMensaje());

            String tipo = notif.getTipo();
            if (tipo != null) {
                stmt.setString(4, tipo);
            } else {
                stmt.setString(4, "info");
            }

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException error) {
            error.printStackTrace();
        }

        return false;
    }

    public List<Notificacion> obtenerNotificacionesActivas() {
        int uid = Session.getInstance().getUsuarioActual().getId();
        return obtenerNotificaciones(uid);
    }

    /** Construye un objeto Notificacion a partir de la fila actual del ResultSet. */
    private Notificacion mapearNotificacion(ResultSet resultados) throws SQLException {
        Notificacion notificacion = new Notificacion();

        notificacion.setId(resultados.getInt("id"));
        notificacion.setUsuarioId(resultados.getInt("usuario_id"));
        notificacion.setTitulo(resultados.getString("titulo"));
        notificacion.setMensaje(resultados.getString("mensaje"));
        notificacion.setTipo(resultados.getString("tipo"));
        Timestamp stamp = resultados.getTimestamp("created_at");
        if (stamp != null) {
            java.time.LocalDateTime fechaHoraCreacion = stamp.toLocalDateTime();
            notificacion.setCreatedAt(fechaHoraCreacion);
        } else {
            notificacion.setCreatedAt(null);
        }

        return notificacion;
    }
}
