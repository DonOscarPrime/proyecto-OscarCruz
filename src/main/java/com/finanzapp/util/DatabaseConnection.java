package com.finanzapp.util;

import java.sql.*;

/**
 * Gestiona la conexión singleton a la base de datos MySQL de Fox Wallet.
 * <p>
 * Si la base de datos {@code foxwallet} no existe al arrancar la aplicación,
 * la crea automáticamente con charset UTF-8 y delega en {@link DatabaseInitializer}
 * la creación de todas las tablas y los datos de demostración.
 * <p>
 * <b>Configuración:</b> cambia {@code PASSWORD} si tu MySQL no usa {@code "1234"}.
 */
public class DatabaseConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "finanzapp";
    private static final String USER     = "root";
    private static final String PASSWORD = "1234";   // ← cambia esto si tu MySQL usa otra contraseña

    /** URL de conexión sin base de datos (usada solo para crearla si no existe). */
    private static final String URL_SIN_BD =
            "jdbc:mysql://" + HOST + ":" + PORT +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    /** URL de conexión completa a la base de datos de Fox Wallet. */
    private static final String URL_FOX_WALLET =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static Connection conexionActiva;
    private static boolean    esquemaInicializado = false;

    /**
     * Devuelve la conexión singleton a MySQL.
     * Si está cerrada o es nula, abre una nueva (creando la BD si es necesario)
     * e inicializa el esquema de Fox Wallet la primera vez.
     */
    public static Connection getConnection() throws SQLException {
        if (conexionActiva == null || conexionActiva.isClosed()) {
            conexionActiva = conectarOCrearBaseDatos();
            if (!esquemaInicializado) {
                DatabaseInitializer.inicializarEsquemaFoxWallet(conexionActiva);
                esquemaInicializado = true;
            }
        }
        return conexionActiva;
    }

    /**
     * Intenta abrir conexión a la base de datos de Fox Wallet.
     * Si MySQL devuelve el error 1049 (base de datos inexistente),
     * la crea con UTF-8 y reintenta la conexión.
     */
    private static Connection conectarOCrearBaseDatos() throws SQLException {
        try {
            return DriverManager.getConnection(URL_FOX_WALLET, USER, PASSWORD);
        } catch (SQLException errorConexion) {
            if (errorConexion.getErrorCode() == 1049) { // Unknown database
                try (Connection conexionBase = DriverManager.getConnection(URL_SIN_BD, USER, PASSWORD);
                     Statement stmtCrear     = conexionBase.createStatement()) {
                    stmtCrear.execute(
                        "CREATE DATABASE " + DATABASE +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }
                return DriverManager.getConnection(URL_FOX_WALLET, USER, PASSWORD);
            }
            throw errorConexion;
        }
    }

    /**
     * Cierra la conexión activa a MySQL.
     * Se llama desde {@link com.finanzapp.MainApp#stop()} al cerrar Fox Wallet.
     */
    public static void cerrarConexion() {
        try {
            if (conexionActiva != null && !conexionActiva.isClosed()) {
                conexionActiva.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated Usar {@link #cerrarConexion()} para mayor claridad.
     */
    @Deprecated
    public static void closeConnection() { cerrarConexion(); }
}
