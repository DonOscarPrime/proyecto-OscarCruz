package com.finanzapp.regresion;

import com.finanzapp.dao.MovimientoDAOTest;
import com.finanzapp.dao.UsuarioDAOTest;
import com.finanzapp.unitarias.PruebasUnitarias;
import com.finanzapp.funcional.PruebasFuncionales;
import com.finanzapp.sistema.PruebasSistema;
import com.finanzapp.aceptacion.PruebasAceptacion;
import com.finanzapp.seguridad.PruebasSeguridad;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 *  SUITE DE REGRESIÓN
 *  Ejecuta TODOS los tests del proyecto en un único pase.
 *  Se lanza antes de cada entrega o despliegue para garantizar
 *  que ningún cambio ha roto funcionalidad existente.
 */
@Suite
@SuiteDisplayName("Suite de Regresión — Fox Wallet completo")
@SelectClasses({
    PruebasUnitarias.class,
    PruebasSistema.class,
    PruebasSeguridad.class,
    UsuarioDAOTest.class,
    MovimientoDAOTest.class,
    PruebasFuncionales.class,
    PruebasAceptacion.class
})
public class SuiteRegresion {
    /*
     * Esta clase no contiene tests propios.
     * Su única función es agrupar y ordenar las suites existentes.
     * JUnit Platform la detecta automáticamente gracias a @Suite.
     */
}
