package com.finanzapp.dao;

import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas unitarias de integración para {@link UsuarioDAO}.
 *
 * <p>Estas pruebas requieren que MySQL esté arrancado y accesible con las
 * credenciales configuradas en {@code DatabaseConnection}. Si la base de
 * datos no está disponible, todas las pruebas se omiten automáticamente
 * sin marcarlas como fallidas.</p>
 *
 * <p>Se ejecutan en orden fijo para garantizar que el registro del usuario
 * de prueba precede a la comprobación de duplicado. El correo de prueba
 * incluye un timestamp para no interferir con datos existentes.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UsuarioDAO — pruebas unitarias de integración")
class UsuarioDAOTest {

    private static final UsuarioDAO dao = new UsuarioDAO();

    /** Correo único por ejecución para evitar colisiones con datos existentes. */
    private static final String TEST_EMAIL =
            "junit_test_" + System.currentTimeMillis() + "@finanzapp.test";

    private static boolean dbDisponible = false;
    private static int testUserId = -1;

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeAll
    static void verificarConexion() {
        try {
            DatabaseConnection.getConnection();
            dbDisponible = true;
        } catch (Exception e) {
            System.err.println("[UsuarioDAOTest] MySQL no disponible — se omiten las pruebas: " + e.getMessage());
        }
    }

    @AfterAll
    static void limpiarDatosDePrueba() {
        if (!dbDisponible || testUserId <= 0) return;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM usuarios WHERE email = ?")) {
            ps.setString(1, TEST_EMAIL);
            int borrados = ps.executeUpdate();
            System.out.println("[UsuarioDAOTest] Limpieza: " + borrados + " usuario(s) de prueba eliminado(s).");
        } catch (Exception e) {
            System.err.println("[UsuarioDAOTest] Error en limpieza: " + e.getMessage());
        }
    }

    // ── Casos de prueba ───────────────────────────────────────────────────────

    /**
     * TC-01: Registrar un usuario con un correo nuevo debe devolver {@code true}
     * y asignar un ID generado mayor que 0.
     */
    @Test
    @Order(1)
    @DisplayName("TC-01 registrar() con email nuevo → retorna true y asigna ID")
    void testRegistrarUsuario_emailNuevo_retornaTrue() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");

        Usuario u = new Usuario();
        u.setNombre("JUnit Test User");
        u.setEmail(TEST_EMAIL);
        u.setComunidad("Madrid");

        boolean resultado = dao.registrar(u, "TestPass1234!");

        assertTrue(resultado,
                "registrar() debe devolver true para un email que no existe en la BD");
        assertTrue(u.getId() > 0,
                "El ID generado por MySQL debe ser mayor que 0");

        testUserId = u.getId();
        System.out.println("[TC-01] Usuario de prueba creado con ID: " + testUserId);
    }

    /**
     * TC-02: Intentar registrar un segundo usuario con el mismo correo debe
     * lanzar una {@link RuntimeException} cuyo mensaje sea {@code "EMAIL_DUPLICADO"}.
     *
     * <p>La restricción UNIQUE del campo {@code email} en la tabla {@code usuarios}
     * hace que MySQL lance una {@code SQLIntegrityConstraintViolationException},
     * que {@code UsuarioDAO} convierte en esta RuntimeException.</p>
     */
    @Test
    @Order(2)
    @DisplayName("TC-02 registrar() con email duplicado → lanza RuntimeException(EMAIL_DUPLICADO)")
    void testRegistrarUsuario_emailDuplicado_lanzaExcepcion() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(testUserId > 0,
                "TC-01 no completado — se omite TC-02 por dependencia");

        Usuario duplicado = new Usuario();
        duplicado.setNombre("Usuario Duplicado");
        duplicado.setEmail(TEST_EMAIL);   // mismo correo que en TC-01
        duplicado.setComunidad("Madrid");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> dao.registrar(duplicado, "OtraPass5678!"),
                "Registrar con email duplicado debe lanzar RuntimeException"
        );

        assertEquals("EMAIL_DUPLICADO", ex.getMessage(),
                "El mensaje de la excepción debe ser exactamente 'EMAIL_DUPLICADO'");
        System.out.println("[TC-02] Excepción correcta recibida: " + ex.getMessage());
    }

    /**
     * TC-03: El login con las credenciales correctas debe devolver el objeto
     * {@link Usuario} con el ID y el nombre correctos.
     */
    @Test
    @Order(3)
    @DisplayName("TC-03 login() con credenciales correctas → devuelve Usuario")
    void testLogin_credencialesCorrectas_devuelveUsuario() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(testUserId > 0,
                "TC-01 no completado — se omite TC-03 por dependencia");

        Usuario resultado = dao.login(TEST_EMAIL, "TestPass1234!");

        assertNotNull(resultado,
                "login() debe devolver un objeto Usuario no nulo para credenciales válidas");
        assertEquals(TEST_EMAIL, resultado.getEmail(),
                "El email del usuario devuelto debe coincidir");
        assertEquals("JUnit Test User", resultado.getNombre(),
                "El nombre del usuario devuelto debe coincidir");
        System.out.println("[TC-03] Login correcto para usuario ID: " + resultado.getId());
    }

    /**
     * TC-04: El login con contraseña incorrecta debe devolver {@code null}.
     */
    @Test
    @Order(4)
    @DisplayName("TC-04 login() con contraseña incorrecta → devuelve null")
    void testLogin_passwordIncorrecta_devuelveNull() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(testUserId > 0,
                "TC-01 no completado — se omite TC-04 por dependencia");

        Usuario resultado = dao.login(TEST_EMAIL, "ContraseñaEquivocada!");

        assertNull(resultado,
                "login() debe devolver null cuando la contraseña no coincide con el hash BCrypt");
        System.out.println("[TC-04] login() con password incorrecta devolvió null correctamente.");
    }
}
