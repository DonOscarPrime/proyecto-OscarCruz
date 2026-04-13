package com.finanzapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión a la base de datos MySQL.
 * Configura las credenciales en las constantes de esta clase.
 */
public class DatabaseConnection {

    // ── Configura estos valores según tu entorno ──────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "finanzapp";
    private static final String USER     = "root";
    private static final String PASSWORD = "1234";          // ← tu contraseña
    // ──────────────────────────────────────────────────────────

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static Connection connection;

    /** Devuelve una conexión singleton (la reutiliza si sigue abierta). */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /** Cierra la conexión al salir de la aplicación. */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
