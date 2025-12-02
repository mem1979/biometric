package com.sta.biometric.modelo;

import org.junit.*;
import org.openxava.tests.*;

/**
 * Pruebas automáticas del módulo TurnosHorarios usando ModuleTestBase.
 * 
 * Verifica la creación, validación y cálculo de turnos.
 */
public class TurnosHorariosTest extends ModuleTestBase {

    public TurnosHorariosTest(String testName) {
        super(testName, "biometric", "TurnosHorarios");
    }

    /**
     * Test: Crear turno de mañana completo
     */
    public void testCrearTurnoMananaCompleto() throws Exception {
        login("admin", "admin");

        execute("CRUD.new");
        assertNoErrors();

        // Configurar turno de mañana
        setValue("turnoNombre", "MANANA");
        setValue("tolerancia", "5");

        // Configurar lunes a viernes
        setValue("lunes", "true");
        setValue("horaEntradaLunes", "08:00");
        setValue("horaSalidaLunes", "16:00");

        setValue("martes", "true");
        setValue("horaEntradaMartes", "08:00");
        setValue("horaSalidaMartes", "16:00");

        setValue("miercoles", "true");
        setValue("horaEntradaMiercoles", "08:00");
        setValue("horaSalidaMiercoles", "16:00");

        setValue("jueves", "true");
        setValue("horaEntradaJueves", "08:00");
        setValue("horaSalidaJueves", "16:00");

        setValue("viernes", "true");
        setValue("horaEntradaViernes", "08:00");
        setValue("horaSalidaViernes", "16:00");

        execute("CRUD.save");
        assertNoErrors();

        // Verificar que se generó el código
        String codigo = getValue("codigo");
        assertTrue(codigo.startsWith("TM.")); // TM = Turno Mañana

        // Verificar cálculo de horas
        String totalHoras = getValue("calculaTotalHoras");
        assertNotNull(totalHoras);
    }

    /**
     * Test: Validación - día activo sin horarios
     */
    public void testValidacionDiaSinHorarios() throws Exception {
        login("admin", "admin");

        execute("CRUD.new");
        setValue("turnoNombre", "TARDE");

        // Activar lunes pero no poner horarios
        setValue("lunes", "true");
        // No setear horaEntradaLunes ni horaSalidaLunes

        execute("CRUD.save");
        assertErrorsCount(1); // Error: debe definir horarios
    }

    /**
     * Test: Validación - debe seleccionar al menos un día
     */
    public void testValidacionAlMenosUnDia() throws Exception {
        login("admin", "admin");

        execute("CRUD.new");
        setValue("turnoNombre", "NOCHE");

        // No activar ningún día
        execute("CRUD.save");
        assertErrorsCount(1); // Error: debe seleccionar al menos un día
    }

    /**
     * Test: Crear turno nocturno (cruza medianoche)
     */
    public void testCrearTurnoNocturno() throws Exception {
        login("admin", "admin");

        execute("CRUD.new");
        setValue("turnoNombre", "NOCHE");

        // Turno que cruza medianoche: 22:00 a 06:00
        setValue("lunes", "true");
        setValue("horaEntradaLunes", "22:00");
        setValue("horaSalidaLunes", "06:00"); // Día siguiente

        execute("CRUD.save");
        assertNoErrors();

        // Verificar que calculó correctamente (8 horas)
        String horasLunes = getValue("horasLunes");
        assertTrue(horasLunes.contains("8"));
    }

    /**
     * Test: Generación secuencial de códigos
     */
    public void testGeneracionCodigosSecuencial() throws Exception {
        login("admin", "admin");

        // Crear primer turno mañana
        testCrearTurnoMananaCompleto();
        String primerCodigo = getValue("codigo");

        // Crear segundo turno mañana
        execute("CRUD.new");
        setValue("turnoNombre", "MANANA");
        setValue("lunes", "true");
        setValue("horaEntradaLunes", "07:00");
        setValue("horaSalidaLunes", "15:00");
        execute("CRUD.save");

        String segundoCodigo = getValue("codigo");

        // Deben ser secuenciales
        Assert.assertNotEquals(primerCodigo, segundoCodigo);
        assertTrue(segundoCodigo.compareTo(primerCodigo) > 0);
    }

    /**
     * Test: Detalle de jornada agrupa días con mismo horario
     */
    public void testDetalleJornadaAgrupaHorarios() throws Exception {
        login("admin", "admin");

        execute("CRUD.new");
        setValue("turnoNombre", "MANANA");

        // Todos los días laborales con mismo horario
        String[] diasLaborales = { "lunes", "martes", "miercoles", "jueves", "viernes" };
        for (String dia : diasLaborales) {
            setValue(dia, "true");
            setValue("horaEntrada" + capitalize(dia), "08:00");
            setValue("horaSalida" + capitalize(dia), "16:00");
        }

        execute("CRUD.save");
        assertNoErrors();

        String detalle = getValue("detalleJornadaHoras");
        // Debe mostrar "Lu.Ma.Mi.Ju.Vi. de 08:00 a 16:00 Hs"
        assertTrue(detalle.contains("Lu."));
        assertTrue(detalle.contains("08:00"));
        assertTrue(detalle.contains("16:00"));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
