package com.finanzapp.seguridad;

import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import com.finanzapp.util.LoginAttemptService;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * PRUEBAS DE SEGURIDAD de Fox Wallet — Contraseñas, bloqueo de acceso e inyección SQL.
 * <p>
 * Verifica que las contraseñas se almacenan con BCrypt (nunca en texto plano),
 * que el mecanismo de bloqueo por intentos fallidos ({@link com.finanzapp.util.LoginAttemptService})
 * actúa correctamente tras 5 intentos incorrectos, y que el sistema es resistente
 * a ataques de inyección SQL en los campos de login.
 */
@DisplayName("Pruebas de Seguridad")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasSeguridad {

    private static UsuarioDAO usuarioDAO;
    private static boolean    dbOk = false;
    private static int        uid;
    private static final String EMAIL    = "seg_" + System.currentTimeMillis() + "@test.com";
    private static final String PASSWORD = "MiPassword123";

    @BeforeAll
    static void setup() {
        try (Connection c = DatabaseConnection.getConnection()) {
            dbOk = c != null && !c.isClosed();
        } catch (Exception ignored) { }
        if (dbOk) {
            usuarioDAO = new UsuarioDAO();
            Usuario u = new Usuario();
            u.setNombre("Usuario Seg"); u.setEmail(EMAIL); u.setComunidad("Madrid");
            usuarioDAO.registrarNuevoUsuario(u, PASSWORD);
            uid = u.getId();
        }
        LoginAttemptService.reset();
    }

    @AfterAll
    static void teardown() {
        LoginAttemptService.reset();
        if (dbOk && uid > 0) {
            try (Connection c = DatabaseConnection.getConnection()) {
                c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
            } catch (Exception ignored) { }
        }
    }

    @Test @Order(1)
    @DisplayName("SEG-01: La contraseña se guarda como hash BCrypt, nunca en texto plano")
    void testPasswordHasheada() {
        assumeTrue(dbOk && uid > 0);
        Usuario u = usuarioDAO.autenticarUsuario(EMAIL, PASSWORD);
        assertNotNull(u);
        assertTrue(u.getPasswordHash().startsWith("$2"));
        assertFalse(u.getPasswordHash().contains(PASSWORD));
    }

    @Test @Order(2)
    @DisplayName("SEG-02: La cuenta se bloquea tras demasiados intentos fallidos")
    void testBloqueoLogin() {
        String email = "victima@test.com";
        LoginAttemptService.reset();
        for (int i = 0; i < LoginAttemptService.MAX_INTENTOS; i++) {
            LoginAttemptService.registrarFallo(email);
        }
        assertTrue(LoginAttemptService.estaBloqueado(email));
        LoginAttemptService.registrarExito(email);
        assertFalse(LoginAttemptService.estaBloqueado(email));
    }

    @Test @Order(3)
    @DisplayName("SEG-03: Inyección SQL en el login no devuelve usuarios ni lanza excepción")
    void testInyeccionSQL() {
        assumeTrue(dbOk);
        assertDoesNotThrow(() -> {
            assertNull(usuarioDAO.autenticarUsuario("' OR '1'='1' --", "x"));
            assertNull(usuarioDAO.autenticarUsuario(EMAIL, "' OR '1'='1"));
        });
    }
}
