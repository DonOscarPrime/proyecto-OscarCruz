package com.finanzapp.util;

import java.sql.*;

/**
 Esta clase se encarga de la conexión con la base de datos MySQL
 */
public class DatabaseConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "finanzapp";
    private static final String USER     = "root";
    private static final String PASSWORD = "12345";

    private static final String URL_SIN_BD =
            "jdbc:mysql://" + HOST + ":" + PORT +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static final String URL_FOX_WALLET =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    private static Connection conexionActiva;
    private static boolean esquemaInicializado = false;


    public static Connection getConnection() throws SQLException {
        boolean conexionNula = (conexionActiva == null);
        boolean conexionCerrada = false;

        if (!conexionNula) {
            conexionCerrada = conexionActiva.isClosed();
        }

        if (conexionNula || conexionCerrada) {
            conexionActiva = conectarOCrearBaseDatos();

            if (!esquemaInicializado) {
                DatabaseInitializer.inicializarEsquemaFoxWallet(conexionActiva);
                esquemaInicializado = true;
            }
        }

        return conexionActiva;
    }

    private static Connection conectarOCrearBaseDatos() throws SQLException {
        try {
            Connection con = DriverManager.getConnection(URL_FOX_WALLET, USER, PASSWORD);
            System.out.println("[FoxWallet] Conectado a MySQL " + HOST + ":" + PORT + "/" + DATABASE);
            return con;

        } catch (SQLException errorConexion) {

            if (errorConexion.getErrorCode() == 1049) {
                System.out.println("[FoxWallet] Base de datos '" + DATABASE + "' no existe → creando...");

                try (Connection conexionBase = DriverManager.getConnection(URL_SIN_BD, USER, PASSWORD);
                     Statement stmtCrear = conexionBase.createStatement()) {

                    stmtCrear.execute(
                        "CREATE DATABASE " + DATABASE +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }

                System.out.println("[FoxWallet] Base de datos creada con UTF-8.");
                return DriverManager.getConnection(URL_FOX_WALLET, USER, PASSWORD);
            }

            if (errorConexion.getErrorCode() == 1045) {
                System.err.println("[FoxWallet] ERROR 1045: contraseña/usuario incorrectos.");
                System.err.println("           Edita PASSWORD en DatabaseConnection.java (actual: '" + PASSWORD + "').");
            } else {
                String mensajeError = errorConexion.getMessage();
                boolean mensajeNoNulo = mensajeError != null;
                boolean esErrorConexion = mensajeNoNulo && mensajeError.toLowerCase().contains("communications link failure");
                if (esErrorConexion) {
                    System.err.println("[FoxWallet] ERROR de conexión: ¿está arrancado el servicio MySQL en " + HOST + ":" + PORT + "?");
                }
            }

            throw errorConexion;
        }
    }

    public static void cerrarConexion() {
        try {
            boolean conexionExiste = (conexionActiva != null);

            if (conexionExiste) {
                boolean estaAbierta = !conexionActiva.isClosed();
                if (estaAbierta) {
                    conexionActiva.close();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
