package com.finanzapp.sistema;

import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PRUEBAS DE SISTEMA de Fox Wallet — Verifica que el entorno de ejecución
 * cumple los requisitos mínimos para desplegar la aplicación.
 * <p>
 * Comprueba la versión de Java (≥ 17), la disponibilidad de la conexión MySQL,
 * la existencia de las tablas principales del esquema y la presencia del
 * driver JDBC en el classpath.
 */
@DisplayName("Pruebas de Sistema")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasSistema {

    @Test @Order(1)
    @DisplayName("SIS-01: Java 17 o superior instalado")
    void testVersionJava() {
        assertTrue(Runtime.version().feature() >= 17,
            "Se requiere Java 17+. Detectado: " + Runtime.version());
    }

    @Test @Order(2)
    @DisplayName("SIS-02: Conexión a MySQL disponible")
    void testConexionMySQL() {
        try (Connection c = DatabaseConnection.getConnection()) {
            assertNotNull(c);
            assertFalse(c.isClosed());
        } catch (Exception e) {
            fail("No se pudo conectar a MySQL: " + e.getMessage());
        }
    }

    @Test @Order(3)
    @DisplayName("SIS-03: Las tablas principales existen en la BD")
    void testTablasExisten() {
        String[] tablas = {"usuarios", "movimientos", "objetivos", "categorias", "habitos", "notificaciones"};
        try (Connection c = DatabaseConnection.getConnection()) {
            for (String tabla : tablas) {
                assertTrue(c.getMetaData().getTables(null, null, tabla, null).next(),
                    "Tabla '" + tabla + "' no encontrada");
            }
        } catch (Exception e) {
            fail("Error verificando tablas: " + e.getMessage());
        }
    }
}
