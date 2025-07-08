package com.sta.biometric.servicios;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.dashboard.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

public class ResumenAsistenciaHoyService {

    /**
     * Devuelve el resumen diario de asistencia para cada empleado activo.
     * Incluye evaluación inteligente según hora, turno, licencia y fichadas.
     *
     * @param empleados Lista de empleados a considerar
     * @param fechaEvaluacion Fecha del día a evaluar
     * @return Lista de resúmenes por empleado
     */
	
    public static List<ResumenEmpleadoHoy> calcularResumen(List<Personal> empleados, LocalDate fechaEvaluacion) {
        EntityManager em = XPersistence.getManager();
        DayOfWeek dia = fechaEvaluacion.getDayOfWeek();
        LocalTime ahora = LocalTime.now();

        List<ResumenEmpleadoHoy> resumenes = new ArrayList<>();

        for (Personal e : empleados) {
            if (!e.isActivo()) continue;

            TurnosHorarios turno = e.getTurnoParaFecha(fechaEvaluacion);
            boolean tieneTurnoAsignado = turno != null;
            boolean esLaboral = tieneTurnoAsignado && turno.esLaboral(dia);
            boolean esFeriado = Feriados.existeParaFecha(fechaEvaluacion);
            boolean conLicencia = Licencia.tieneLicenciaEnFecha(e, fechaEvaluacion);

            LocalTime entradaEsperada = (turno != null) ? turno.getEntradaParaDia(dia) : null;
            LocalTime salidaEsperada = (turno != null) ? turno.getSalidaParaDia(dia) : null;
            int tolerancia = (turno != null && turno.getTolerancia() != null) ? turno.getTolerancia() : 5;

            boolean ingresoRealizado = false;
            boolean llegadaTarde = false;
            boolean salidaAnticipada = false;

            // Obtener fichadas si corresponde
            if (esLaboral && !conLicencia) {
                List<LocalTime> entradas = em.createQuery(
                        "SELECT r.hora FROM ColeccionRegistros r " +
                        "WHERE r.asistenciaDiaria.empleado = :emp " +
                        "AND r.fecha = :fecha " +
                        "AND r.tipoMovimiento = :tipo " +
                        "ORDER BY r.hora ASC", LocalTime.class)
                        .setParameter("emp", e)
                        .setParameter("fecha", fechaEvaluacion)
                        .setParameter("tipo", TipoMovimiento.ENTRADA)
                        .getResultList();

                if (!entradas.isEmpty()) {
                    ingresoRealizado = true;
                    llegadaTarde = entradaEsperada != null && entradas.get(0).isAfter(entradaEsperada.plusMinutes(tolerancia));
                }

                List<LocalTime> salidas = em.createQuery(
                        "SELECT r.hora FROM ColeccionRegistros r " +
                        "WHERE r.asistenciaDiaria.empleado = :emp " +
                        "AND r.fecha = :fecha " +
                        "AND r.tipoMovimiento = :tipo " +
                        "ORDER BY r.hora DESC", LocalTime.class)
                        .setParameter("emp", e)
                        .setParameter("fecha", fechaEvaluacion)
                        .setParameter("tipo", TipoMovimiento.SALIDA)
                        .getResultList();

                if (!salidas.isEmpty()) {
                    salidaAnticipada = salidaEsperada != null && salidas.get(0).isBefore(salidaEsperada.minusMinutes(tolerancia));
                }
            }

            // ================================
            // Evaluación inteligente
            // ================================
            EvaluacionJornada evaluacion;

            if (conLicencia) {
                evaluacion = EvaluacionJornada.LICENCIA;

            } else if (!tieneTurnoAsignado) {
                evaluacion = EvaluacionJornada.SIN_TURNO_ASIGNADO;

            } else if (esFeriado && ingresoRealizado) {
                evaluacion = EvaluacionJornada.FERIADO_TRABAJADO;

            } else if (esFeriado) {
                evaluacion = EvaluacionJornada.FERIADO;

            } else if (!esLaboral) {
                evaluacion = EvaluacionJornada.DIA_NO_LABORAL;

            } else if (!ingresoRealizado) {
                if (entradaEsperada != null && ahora.isBefore(entradaEsperada.plusMinutes(tolerancia))) {
                    evaluacion = EvaluacionJornada.PENDIENTE;
                } else {
                    evaluacion = EvaluacionJornada.AUSENTE;
                }

            } else if (!llegadaTarde && !salidaAnticipada) {
                evaluacion = EvaluacionJornada.COMPLETA;

            } else {
                evaluacion = EvaluacionJornada.INCOMPLETA;
            }

            resumenes.add(new ResumenEmpleadoHoy(
                e, esLaboral, conLicencia, ingresoRealizado, llegadaTarde, salidaAnticipada, evaluacion
            ));
        }

        return resumenes;
    }
}

