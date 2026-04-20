package com.finanzapp.dao;

import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas unitarias de integración para {@link MovimientoDAO}.
 *
 * <p>Se crea un usuario de prueba exclusivo al inicio del ciclo de vida,
 * se insertan movimientos bajo ese usuario y se eliminan todos al final
 * mediante CASCADE DELETE (basta con borrar el usuario).</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MovimientoDAO — pruebas unitarias de integración")
class MovimientoDAOTest {

    private static final MovimientoDAO movDAO    = new MovimientoDAO();
    private static final UsuarioDAO    usuDAO    = new UsuarioDAO();

    private static final String TEST_EMAIL =
            "junit_mov_" + System.currentTimeMillis() + "@finanzapp.test";

    private static boolean dbDisponible = false;
    private static int     testUserId   = -1;
    private static int     movimientoId = -1;  // ID del movimiento insertado en TC-01

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeAll
    static void setup() {
        try {
            DatabaseConnection.getConnection();
            dbDisponible = true;

            // Crear usuario de prueba exclusivo para estos tests
            Usuario u = new Usuario();
            u.setNombre("JUnit Mov Test");
            u.setEmail(TEST_EMAIL);
            u.setComunidad("Madrid");
            boolean creado = usuDAO.registrar(u, "TestPass1234!");
            if (creado) {
                testUserId = u.getId();
                System.out.println("[MovimientoDAOTest] Usuario de prueba creado con ID: " + testUserId);
            }
        } catch (Exception e) {
            System.err.println("[MovimientoDAOTest] Setup fallido: " + e.getMessage());
        }
    }

    @AfterAll
    static void cleanup() {
        if (!dbDisponible || testUserId <= 0) return;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, testUserId);
            // ON DELETE CASCADE elimina también los movimientos de este usuario
            int borrados = ps.executeUpdate();
            System.out.println("[MovimientoDAOTest] Limpieza: " + borrados
                    + " usuario(s) eliminado(s) (movimientos borrados por CASCADE).");
        } catch (Exception e) {
            System.err.println("[MovimientoDAOTest] Error en limpieza: " + e.getMessage());
        }
    }

    // ── Casos de prueba ───────────────────────────────────────────────────────

    /**
     * TC-01: Insertar un movimiento con datos válidos debe devolver {@code true}
     * y asignar al objeto un ID mayor que 0.
     */
    @Test
    @Order(1)
    @DisplayName("TC-01 insertar() con datos válidos → retorna true y asigna ID")
    void testInsertarMovimiento_datosValidos_retornaTrue() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(testUserId > 0, "Usuario de prueba no creado — prueba omitida");

        Movimiento m = new Movimiento();
        m.setUsuarioId(testUserId);
        m.setTipo("gasto");
        m.setNombre("Compra de prueba JUnit");
        m.setCantidad(49.99);
        m.setCategoriaId(2);   // Alimentación
        m.setNotas("Movimiento generado por prueba unitaria");
        m.setFecha(LocalDate.now());

        boolean resultado = movDAO.insertar(m);

        assertTrue(resultado,
                "insertar() debe devolver true cuando los datos son válidos");
        assertTrue(m.getId() > 0,
                "El ID generado por MySQL debe ser mayor que 0 tras el INSERT");

        movimientoId = m.getId();
        System.out.println("[TC-01] Movimiento de prueba creado con ID: " + movimientoId);
    }

    /**
     * TC-02: Listar movimientos de un mes concreto debe devolver únicamente
     * los movimientos de ese mes, ordenados por fecha descendente.
     *
     * <p>Se insertan tres movimientos: dos en el mes actual y uno en el mes
     * anterior. La consulta debe devolver exactamente dos registros para el
     * mes actual.</p>
     */
    @Test
    @Order(2)
    @DisplayName("TC-02 listarPorMes() → devuelve solo movimientos del mes, ordenados DESC")
    void testListarMovimientosPorMes_retornaListaOrdenada() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(testUserId > 0, "Usuario de prueba no creado — prueba omitida");

        LocalDate hoy       = LocalDate.now();
        LocalDate mesAnterior = hoy.minusMonths(1).withDayOfMonth(15);

        // Movimiento 1 del mes actual (día 1 del mes)
        Movimiento m1 = movimiento(testUserId, "ingreso", "Nómina test",   1000.0, hoy.withDayOfMonth(1));
        // Movimiento 2 del mes actual (día 10)
        Movimiento m2 = movimiento(testUserId, "gasto",   "Alquiler test",  450.0,
                hoy.withDayOfMonth(Math.min(10, hoy.lengthOfMonth())));
        // Movimiento del mes anterior — NO debe aparecer en la consulta del mes actual
        Movimiento m3 = movimiento(testUserId, "gasto",   "Gasto mes ant",   50.0, mesAnterior);

        assertTrue(movDAO.insertar(m1), "Insertar m1 debe devolver true");
        assertTrue(movDAO.insertar(m2), "Insertar m2 debe devolver true");
        assertTrue(movDAO.insertar(m3), "Insertar m3 (mes anterior) debe devolver true");

        // Consultar solo el mes actual
        List<Movimiento> lista = movDAO.listarPorMes(testUserId, hoy.getYear(), hoy.getMonthValue());

        // Debe haber al menos 2 movimientos (los del mes actual + el de TC-01)
        assertTrue(lista.size() >= 2,
                "La lista del mes actual debe contener al menos los 2 movimientos insertados");

        // Verificar orden descendente por fecha (el primero debe ser >= el último)
        if (lista.size() > 1) {
            LocalDate primerFecha  = lista.get(0).getFecha();
            LocalDate ultimaFecha  = lista.get(lista.size() - 1).getFecha();
            assertFalse(primerFecha.isBefore(ultimaFecha),
                    "Los movimientos deben estar ordenados por fecha descendente (más reciente primero)");
        }

        // Ningún movimiento del mes anterior debe aparecer en los resultados
        boolean hayMesAnterior = lista.stream()
                .anyMatch(m -> m.getFecha() != null
                        && m.getFecha().getMonthValue() != hoy.getMonthValue());
        assertFalse(hayMesAnterior,
                "La lista no debe contener movimientos de un mes diferente al consultado");

        System.out.println("[TC-02] listarPorMes devolvió " + lista.size()
                + " movimiento(s) para " + hoy.getMonth() + "/" + hoy.getYear());
    }

    /**
     * TC-03: Eliminar un movimiento existente por ID debe devolver {@code true}.
     */
    @Test
    @Order(3)
    @DisplayName("TC-03 eliminar() con ID válido → retorna true")
    void testEliminarMovimiento_idValido_retornaTrue() {
        assumeTrue(dbDisponible, "MySQL no disponible — prueba omitida");
        assumeTrue(movimientoId > 0,
                "TC-01 no completó el INSERT — se omite TC-03 por dependencia");

        boolean resultado = movDAO.eliminar(movimientoId);

        assertTrue(resultado,
                "eliminar() debe devolver true cuando el ID existe en la BD");
        System.out.println("[TC-03] Movimiento ID " + movimientoId + " eliminado correctamente.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static Movimiento movimiento(int uid, String tipo, String nombre,
                                         double cantidad, LocalDate fecha) {
        Movimiento m = new Movimiento();
        m.setUsuarioId(uid);
        m.setTipo(tipo);
        m.setNombre(nombre);
        m.setCantidad(cantidad);
        m.setFecha(fecha);
        return m;
    }
}
