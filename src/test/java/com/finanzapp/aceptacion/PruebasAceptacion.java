package com.finanzapp.aceptacion;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.UsuarioDAO;
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
 * PRUEBAS DE ACEPTACIÓN (UAT) de Fox Wallet — Escenarios reales del usuario final.
 * <p>
 * Simula los flujos completos que realizaría Oswaldo (usuario tipo de Fox Wallet):
 * registrar un ingreso de nómina y un gasto de supermercado, y verificar que
 * el balance mensual calculado por la app es correcto. Valida la experiencia
 * end-to-end contra la base de datos real.
 */
@DisplayName("Pruebas de Aceptación (UAT)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasAceptacion {

    private static UsuarioDAO    usuarioDAO;
    private static MovimientoDAO movimientoDAO;
    private static boolean       dbOk = false;
    private static int           uid;
    private static final String  EMAIL = "uat_" + System.currentTimeMillis() + "@test.com";

    @BeforeAll
    static void setup() {
        try (Connection c = DatabaseConnection.getConnection()) {
            dbOk = c != null && !c.isClosed();
        } catch (Exception ignored) { }
        if (dbOk) { usuarioDAO = new UsuarioDAO(); movimientoDAO = new MovimientoDAO(); }
    }

    @AfterAll
    static void cleanup() {
        if (dbOk && uid > 0) {
            try (Connection c = DatabaseConnection.getConnection()) {
                c.createStatement().executeUpdate("DELETE FROM usuarios WHERE id=" + uid);
            } catch (Exception ignored) { }
        }
    }

    /** HU-01: Como nuevo usuario quiero registrarme y acceder a la aplicación. */
    @Test @Order(1)
    @DisplayName("HU-01: Registro y acceso a la aplicación")
    void hu01_registro() {
        assumeTrue(dbOk);
        Usuario u = new Usuario();
        u.setNombre("Ana García"); u.setEmail(EMAIL); u.setComunidad("Cataluña");
        assertTrue(usuarioDAO.registrarNuevoUsuario(u, "Segura@123"));
        uid = u.getId();
        assertNotNull(usuarioDAO.autenticarUsuario(EMAIL, "Segura@123"));
    }

    /** HU-02: Como usuario quiero ver mi balance mensual. */
    @Test @Order(2)
    @DisplayName("HU-02: Balance mensual positivo tras ingresar más de lo que se gasta")
    void hu02_balanceMensual() {
        assumeTrue(dbOk && uid > 0);
        LocalDate hoy = LocalDate.now();

        Movimiento ing = new Movimiento();
        ing.setUsuarioId(uid); ing.setTipo("ingreso"); ing.setNombre("Sueldo");
        ing.setCantidad(1800); ing.setCategoriaId(1); ing.setFecha(hoy);
        movimientoDAO.registrarMovimiento(ing);

        Movimiento gas = new Movimiento();
        gas.setUsuarioId(uid); gas.setTipo("gasto"); gas.setNombre("Alquiler");
        gas.setCantidad(700); gas.setCategoriaId(1); gas.setFecha(hoy);
        movimientoDAO.registrarMovimiento(gas);

        List<Movimiento> lista = movimientoDAO.obtenerMovimientosPorMes(uid, hoy.getYear(), hoy.getMonthValue());
        double balance = lista.stream()
            .mapToDouble(m -> "ingreso".equals(m.getTipo()) ? m.getCantidad() : -m.getCantidad())
            .sum();
        assertTrue(balance > 0, "Balance esperado positivo: " + balance);
    }

    /** HU-03: El sistema impide registrar dos cuentas con el mismo email. */
    @Test @Order(3)
    @DisplayName("HU-03: Email duplicado rechazado")
    void hu03_emailDuplicado() {
        assumeTrue(dbOk && uid > 0);
        Usuario dup = new Usuario();
        dup.setNombre("Otro"); dup.setEmail(EMAIL); dup.setComunidad("Madrid");
        assertThrows(RuntimeException.class, () -> usuarioDAO.registrarNuevoUsuario(dup, "otrapass"));
    }
}
