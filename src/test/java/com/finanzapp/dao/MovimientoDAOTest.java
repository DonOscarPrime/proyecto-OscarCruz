package com.finanzapp.dao;

import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integración de {@link MovimientoDAO} de Fox Wallet.
 * <p>
 * Verifica contra la base de datos real que el DAO puede registrar un nuevo
 * movimiento (gasto), recuperarlo filtrando por mes y eliminarlo correctamente.
 * Requiere conexión activa a MySQL; si no está disponible se saltan con {@code assumeTrue}.
 */
@DisplayName("MovimientoDAO — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MovimientoDAOTest {

    private static final MovimientoDAO movDAO = new MovimientoDAO();
    private static final UsuarioDAO    uDao   = new UsuarioDAO();
    private static final String EMAIL = "junit_mov_" + System.currentTimeMillis() + "@test.com";
    private static boolean dbOk = false;
    private static int     uid  = -1;
    private static int     movId = -1;

    @BeforeAll
    static void setup() {
        try { DatabaseConnection.getConnection(); dbOk = true; }
        catch (Exception e) { System.err.println("MySQL no disponible: " + e.getMessage()); }
        if (dbOk) {
            Usuario u = new Usuario();
            u.setNombre("JUnit Mov"); u.setEmail(EMAIL); u.setComunidad("Madrid");
            if (uDao.registrarNuevoUsuario(u, "TestPass1234!")) uid = u.getId();
        }
    }

    @AfterAll
    static void cleanup() {
        if (!dbOk || uid <= 0) return;
        try (Connection c = DatabaseConnection.getConnection()) {
            c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
        } catch (Exception ignored) { }
    }

    @Test @Order(1)
    @DisplayName("DAO-M01: registrarMovimiento() con datos válidos devuelve true y asigna ID")
    void testInsertar() {
        assumeTrue(dbOk && uid > 0);
        Movimiento m = new Movimiento();
        m.setUsuarioId(uid); m.setTipo("gasto"); m.setNombre("Compra test");
        m.setCantidad(49.99); m.setCategoriaId(2); m.setFecha(LocalDate.now());
        assertTrue(movDAO.registrarMovimiento(m));
        assertTrue(m.getId() > 0);
        movId = m.getId();
    }

    @Test @Order(2)
    @DisplayName("DAO-M02: obtenerMovimientosPorMes() devuelve los movimientos del mes actual")
    void testListar() {
        assumeTrue(dbOk && uid > 0);
        LocalDate hoy = LocalDate.now();
        List<Movimiento> lista = movDAO.obtenerMovimientosPorMes(uid, hoy.getYear(), hoy.getMonthValue());
        assertFalse(lista.isEmpty());
        lista.forEach(m -> assertEquals(hoy.getMonthValue(), m.getFecha().getMonthValue()));
    }

    @Test @Order(3)
    @DisplayName("DAO-M03: eliminar() con ID válido devuelve true")
    void testEliminar() {
        assumeTrue(dbOk && movId > 0);
        assertTrue(movDAO.eliminarMovimiento(movId));
    }
}
