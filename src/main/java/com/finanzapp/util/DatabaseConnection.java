package com.finanzapp.util;

import java.sql.*;

/**
 * Gestiona la conexión a MySQL.
 * Si la base de datos "finanzapp" no existe, la crea automáticamente
 * junto con todas las tablas y los datos demo.
 *
 * Solo tienes que cambiar PASSWORD si tu MySQL no usa "1234".
 */
public class DatabaseConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "finanzapp";
    private static final String USER     = "root";
    private static final String PASSWORD = "1234";   // ← cambia esto si tu MySQL usa otra contraseña

    private static final String URL_BASE =
            "jdbc:mysql://" + HOST + ":" + PORT +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static Connection connection;
    private static boolean    initialized = false;

    /** Devuelve una conexión singleton (la reutiliza si sigue abierta). */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = openOrCreate();
            if (!initialized) {
                DatabaseInitializer.initIfNeeded(connection);
                initialized = true;
            }
        }
        return connection;
    }

    /**
     * Intenta conectar a la BD. Si no existe (error 1049),
     * la crea y vuelve a conectar.
     */
    private static Connection openOrCreate() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1049) { // Unknown database
                // Crear la base de datos
                try (Connection base = DriverManager.getConnection(URL_BASE, USER, PASSWORD);
                     Statement  st   = base.createStatement()) {
                    st.execute("CREATE DATABASE " + DATABASE +
                               " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }
                return DriverManager.getConnection(URL, USER, PASSWORD);
            }
            throw e;
        }
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
