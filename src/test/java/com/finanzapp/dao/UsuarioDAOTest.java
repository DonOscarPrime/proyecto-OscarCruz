package com.finanzapp.dao;

import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integración de {@link UsuarioDAO} de Fox Wallet.
 * <p>
 * Verifica contra la base de datos real el ciclo de vida completo de la cuenta:
 * registro de nueva cuenta con hash BCrypt, autenticación correcta e incorrecta,
 * y rechazo de email duplicado con la excepción esperada.
 * Requiere conexión activa a MySQL; si no está disponible se saltan con {@code assumeTrue}.
 */
@DisplayName("UsuarioDAO — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioDAOTest {

    private static final UsuarioDAO dao = new UsuarioDAO();
    private static final String EMAIL   = "junit_" + System.currentTimeMillis() + "@test.com";
    private static boolean dbOk = false;
    private static int     uid  = -1;

    @BeforeAll
    static void setup() {
        try { DatabaseConnection.getConnection(); dbOk = true; }
        catch (Exception e) { System.err.println("MySQL no disponible: " + e.getMessage()); }
    }

    @AfterAll
    static void cleanup() {
        if (!dbOk || uid <= 0) return;
        try (Connection c = DatabaseConnection.getConnection()) {
            c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
        } catch (Exception ignored) { }
    }

    @Test @Order(1)
    @DisplayName("DAO-U01: registrarNuevoUsuario() con email nuevo devuelve true y asigna ID")
    void testRegistrar() {
        assumeTrue(dbOk, "MySQL no disponible");
        Usuario u = new Usuario();
        u.setNombre("JUnit User"); u.setEmail(EMAIL); u.setComunidad("Madrid");
        assertTrue(dao.registrarNuevoUsuario(u, "TestPass1234!"));
        assertTrue(u.getId() > 0);
        uid = u.getId();
    }

    @Test @Order(2)
    @DisplayName("DAO-U02: autenticarUsuario() correcto devuelve usuario; incorrecto devuelve null")
    void testLogin() {
        assumeTrue(dbOk && uid > 0);
        assertNotNull(dao.autenticarUsuario(EMAIL, "TestPass1234!"));
        assertNull(dao.autenticarUsuario(EMAIL, "mal"));
    }

    @Test @Order(3)
    @DisplayName("DAO-U03: registrarNuevoUsuario() con email duplicado lanza RuntimeException")
    void testEmailDuplicado() {
        assumeTrue(dbOk && uid > 0);
        Usuario dup = new Usuario();
        dup.setNombre("Dup"); dup.setEmail(EMAIL); dup.setComunidad("Madrid");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> dao.registrarNuevoUsuario(dup, "otraPass!"));
        assertEquals("EMAIL_DUPLICADO", ex.getMessage());
    }
}
