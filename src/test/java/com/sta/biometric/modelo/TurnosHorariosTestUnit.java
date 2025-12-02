package com.sta.biometric.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.sta.biometric.auxiliares.TurnosHorarios;
import com.sta.biometric.enums.Turnos;

/**
 * Tests unitarios para TurnosHorarios (no requieren app corriendo)
 * 
 * IMPORTANTE: getHorasParaDia() solo funciona si el día está activo
 * (setLunes(true), etc.)
 */
class TurnosHorariosTestUnit {

    @Test
    void testCrearTurnoBasico() {
        TurnosHorarios turno = new TurnosHorarios();
        turno.setTurnoNombre(Turnos.MANANA);
        turno.setTolerancia(5);

        assertEquals(Turnos.MANANA, turno.getTurnoNombre());
        assertEquals(5, turno.getTolerancia());
    }

    @Test
    void testEsLaboralDiaActivo() {
        TurnosHorarios turno = new TurnosHorarios();
        turno.setLunes(true);
        turno.setMartes(false);

        assertTrue(turno.esLaboral(DayOfWeek.MONDAY));
        assertFalse(turno.esLaboral(DayOfWeek.TUESDAY));
    }

    @Test
    void testGetEntradaParaDia() {
        TurnosHorarios turno = new TurnosHorarios();
        LocalTime entrada = LocalTime.of(8, 30);

        turno.setHoraEntradaLunes(entrada);

        assertEquals(entrada, turno.getEntradaParaDia(DayOfWeek.MONDAY));
        assertNull(turno.getEntradaParaDia(DayOfWeek.TUESDAY)); // No configurado
    }

    @Test
    void testGetSalidaParaDia() {
        TurnosHorarios turno = new TurnosHorarios();
        LocalTime salida = LocalTime.of(17, 0);

        turno.setHoraSalidaLunes(salida);

        assertEquals(salida, turno.getSalidaParaDia(DayOfWeek.MONDAY));
        assertNull(turno.getSalidaParaDia(DayOfWeek.WEDNESDAY)); // No configurado
    }

    @Test
    void testCalculoHorasParaDia() {
        TurnosHorarios turno = new TurnosHorarios();

        // IMPORTANTE: Activar el día ANTES de configurar horarios
        turno.setLunes(true);
        turno.setHoraEntradaLunes(LocalTime.of(8, 0));
        turno.setHoraSalidaLunes(LocalTime.of(16, 0));

        int minutos = turno.getHorasParaDia(DayOfWeek.MONDAY);

        assertEquals(480, minutos); // 8 horas = 480 minutos
    }

    @Test
    void testCalculoHorasParaDiaConMinutos() {
        TurnosHorarios turno = new TurnosHorarios();

        // IMPORTANTE: Activar el día ANTES
        turno.setMartes(true);
        turno.setHoraEntradaMartes(LocalTime.of(8, 0));
        turno.setHoraSalidaMartes(LocalTime.of(16, 30));

        int minutos = turno.getHorasParaDia(DayOfWeek.TUESDAY);

        assertEquals(510, minutos); // 8.5 horas = 510 minutos
    }

    @Test
    void testTurnoNocturno_CruzaMedianoche() {
        TurnosHorarios turno = new TurnosHorarios();
        turno.setTurnoNombre(Turnos.NOCHE);

        // IMPORTANTE: Activar el día ANTES
        turno.setLunes(true);
        turno.setHoraEntradaLunes(LocalTime.of(22, 0));
        turno.setHoraSalidaLunes(LocalTime.of(6, 0));

        int minutos = turno.getHorasParaDia(DayOfWeek.MONDAY);

        // Debería calcular correctamente: 2h (22-24) + 6h (00-06) = 8 horas = 480 min
        assertEquals(480, minutos);
    }

    @Test
    void testDetalleJornadaHoras_TodosLosDiasIguales() {
        TurnosHorarios turno = new TurnosHorarios();
        turno.setTurnoNombre(Turnos.MANANA);

        // Configurar lunes a miércoles con mismo horario
        LocalTime entrada = LocalTime.of(8, 0);
        LocalTime salida = LocalTime.of(16, 0);

        turno.setLunes(true);
        turno.setHoraEntradaLunes(entrada);
        turno.setHoraSalidaLunes(salida);

        turno.setMartes(true);
        turno.setHoraEntradaMartes(entrada);
        turno.setHoraSalidaMartes(salida);

        turno.setMiercoles(true);
        turno.setHoraEntradaMiercoles(entrada);
        turno.setHoraSalidaMiercoles(salida);

        String detalle = turno.getDetalleJornadaHoras();

        // Debería agrupar: "Lu.Ma.Mi. de 08:00 a 16:00 Hs"
        assertNotNull(detalle);
        assertTrue(detalle.contains("Lu."));
        assertTrue(detalle.contains("Ma."));
        assertTrue(detalle.contains("Mi."));
        assertTrue(detalle.contains("08:00"));
        assertTrue(detalle.contains("16:00"));
    }

    @Test
    void testCalculaTotalHoras_TurnoCompleto() {
        TurnosHorarios turno = new TurnosHorarios();

        // Configurar lunes a viernes, 8 horas por día
        LocalTime entrada = LocalTime.of(8, 0);
        LocalTime salida = LocalTime.of(16, 0);

        turno.setLunes(true);
        turno.setHoraEntradaLunes(entrada);
        turno.setHoraSalidaLunes(salida);

        turno.setMartes(true);
        turno.setHoraEntradaMartes(entrada);
        turno.setHoraSalidaMartes(salida);

        turno.setMiercoles(true);
        turno.setHoraEntradaMiercoles(entrada);
        turno.setHoraSalidaMiercoles(salida);

        turno.setJueves(true);
        turno.setHoraEntradaJueves(entrada);
        turno.setHoraSalidaJueves(salida);

        turno.setViernes(true);
        turno.setHoraEntradaViernes(entrada);
        turno.setHoraSalidaViernes(salida);

        String totalHoras = turno.getCalculaTotalHoras();

        // 5 días x 8 horas = 40 horas
        assertNotNull(totalHoras);
        assertTrue(totalHoras.contains("40") || totalHoras.contains("40:00"));
    }

    @Test
    void testDiaSinHorarios_DevuelveCero() {
        TurnosHorarios turno = new TurnosHorarios();

        // Día no configurado ni activo
        int minutos = turno.getHorasParaDia(DayOfWeek.SATURDAY);

        assertEquals(0, minutos);
    }

    @Test
    void testMultiplesTurnos_DiferentesCodigos() {
        TurnosHorarios turno1 = new TurnosHorarios();
        turno1.setTurnoNombre(Turnos.MANANA);

        TurnosHorarios turno2 = new TurnosHorarios();
        turno2.setTurnoNombre(Turnos.TARDE);

        // Los turnos pueden tener nombres diferentes
        assertNotEquals(turno1.getTurnoNombre(), turno2.getTurnoNombre());
    }
}
