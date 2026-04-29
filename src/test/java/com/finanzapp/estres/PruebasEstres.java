package com.finanzapp.estres;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * PRUEBAS DE ESTRÉS — Rendimiento bajo carga.
 * Umbrales: 500 inserciones < 10 s · listado < 2 s
 */
@DisplayName("Pruebas de Estrés")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasEstres {

    private static MovimientoDAO movimientoDAO;
    private static boolean       dbOk = false;
    private static int           uid;
    private static final String  EMAIL = "est_" + System.currentTimeMillis() + "@test.com";

    @BeforeAll
    static void setup() {
        try (Connection c = DatabaseConnection.getConnection()) {
            dbOk = c != null && !c.isClosed();
        } catch (Exception ignored) { }
        if (dbOk) {
            movimientoDAO = new MovimientoDAO();
            UsuarioDAO uDao = new UsuarioDAO();
            Usuario u = new Usuario();
            u.setNombre("Estres"); u.setEmail(EMAIL); u.setComunidad("Madrid");
            uDao.registrar(u, "estresPass");
            uid = u.getId();
        }
    }

    @AfterAll
    static void teardown() {
        if (dbOk && uid > 0) {
            try (Connection c = DatabaseConnection.getConnection()) {
                c.createStatement().executeUpdate("DELETE FROM movimientos WHERE usuario_id=" + uid);
                c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
            } catch (Exception ignored) { }
        }
    }

    @Test @Order(1)
    @DisplayName("EST-01: 500 inserciones de movimientos en menos de 10 segundos")
    void testInsercionMasiva() {
        assumeTrue(dbOk && uid > 0);
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            Movimiento m = new Movimiento();
            m.setUsuarioId(uid); m.setTipo(i % 2 == 0 ? "ingreso" : "gasto");
            m.setNombre("Mov #" + i); m.setCantidad(10.0 + (i % 100));
            m.setCategoriaId((i % 10) + 1); m.setFecha(LocalDate.now().minusDays(i % 30));
            movimientoDAO.registrarMovimiento(m);
        }
        long ms = System.currentTimeMillis() - inicio;
        System.out.printf("  500 inserciones en %d ms%n", ms);
        assertTrue(ms < 10_000, "Tardó " + ms + " ms (límite: 10.000 ms)");
    }

    @Test @Order(2)
    @DisplayName("EST-02: Consulta del listado mensual en menos de 2 segundos")
    void testConsultaRapida() {
        assumeTrue(dbOk && uid > 0);
        LocalDate hoy = LocalDate.now();
        long inicio = System.currentTimeMillis();
        var lista = movimientoDAO.obtenerMovimientosPorMes(uid, hoy.getYear(), hoy.getMonthValue());
        long ms = System.currentTimeMillis() - inicio;
        System.out.printf("  %d registros en %d ms%n", lista.size(), ms);
        assertFalse(lista.isEmpty());
        assertTrue(ms < 2_000, "Consulta tardó " + ms + " ms (límite: 2.000 ms)");
    }
}
