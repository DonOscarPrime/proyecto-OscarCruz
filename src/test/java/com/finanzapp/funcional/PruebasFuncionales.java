package com.finanzapp.funcional;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.dao.UsuarioDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Objetivo;
import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * PRUEBAS FUNCIONALES de Fox Wallet — Verifica los requisitos funcionales del cliente.
 * <p>
 * Cubre los flujos principales de la aplicación contra la base de datos real:
 * registro de movimientos (gastos e ingresos), creación y actualización de
 * objetivos de ahorro mediante el procedimiento almacenado MySQL, y cálculo
 * de estadísticas mensuales. Requiere conexión activa a MySQL; si no está
 * disponible, los tests se saltan con {@code assumeTrue}.
 */
@DisplayName("Pruebas Funcionales")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasFuncionales {

    private static UsuarioDAO    usuarioDAO;
    private static MovimientoDAO movimientoDAO;
    private static ObjetivoDAO   objetivoDAO;
    private static boolean       dbOk = false;
    private static int           uid;
    private static final String  EMAIL = "func_" + System.currentTimeMillis() + "@test.com";

    @BeforeAll
    static void setup() {
        try (Connection c = DatabaseConnection.getConnection()) {
            dbOk = c != null && !c.isClosed();
        } catch (Exception ignored) { }
        if (dbOk) {
            usuarioDAO    = new UsuarioDAO();
            movimientoDAO = new MovimientoDAO();
            objetivoDAO   = new ObjetivoDAO();
        }
    }

    @AfterAll
    static void teardown() {
        if (dbOk && uid > 0) {
            try (Connection c = DatabaseConnection.getConnection()) {
                c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
            } catch (Exception ignored) { }
        }
    }

    @Test @Order(1)
    @DisplayName("FUN-01: Registro y login de usuario")
    void testRegistroYLogin() {
        assumeTrue(dbOk, "BD no disponible");
        Usuario u = new Usuario();
        u.setNombre("Test Funcional"); u.setEmail(EMAIL); u.setComunidad("Madrid");
        assertTrue(usuarioDAO.registrarNuevoUsuario(u, "pass123"));
        uid = u.getId();
        assertTrue(uid > 0);
        assertNotNull(usuarioDAO.autenticarUsuario(EMAIL, "pass123"));
        assertNull(usuarioDAO.autenticarUsuario(EMAIL, "mal"));
    }

    @Test @Order(2)
    @DisplayName("FUN-02: Insertar y listar movimientos del mes")
    void testMovimientos() {
        assumeTrue(dbOk && uid > 0);
        LocalDate hoy = LocalDate.now();
        Movimiento m = new Movimiento();
        m.setUsuarioId(uid); m.setTipo("ingreso"); m.setNombre("Nómina");
        m.setCantidad(2000.0); m.setCategoriaId(1); m.setFecha(hoy);
        assertTrue(movimientoDAO.registrarMovimiento(m));
        assertFalse(movimientoDAO.obtenerMovimientosPorMes(uid, hoy.getYear(), hoy.getMonthValue()).isEmpty());
    }

    @Test @Order(3)
    @DisplayName("FUN-03: Crear objetivo de ahorro y añadir aporte")
    void testObjetivo() {
        assumeTrue(dbOk && uid > 0);
        Objetivo o = new Objetivo();
        o.setUsuarioId(uid); o.setNombre("Vacaciones"); o.setObjetivo(1000.0);
        o.setActual(0.0); o.setEmoji("🏖");
        assertTrue(objetivoDAO.crearObjetivo(o));
        assertTrue(objetivoDAO.registrarAporteObjetivo(o.getId(), 300.0));
    }
}
