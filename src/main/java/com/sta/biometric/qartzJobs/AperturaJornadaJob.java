package com.sta.biometric.qartzJobs;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.hibernate.*;
import org.openxava.jpa.*;
import org.quartz.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;

/**
 * Tarea programada para generar la apertura de jornada diaria para todos los empleados activos.
 * Ejecutada autom√°ticamente a las 00:01 hs.
 */
@DisallowConcurrentExecution
public class AperturaJornadaJob implements Job {

    @SuppressWarnings("unused")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate hoy = LocalDate.now();
        EntityManager em = XPersistence.getManager();

        System.out.println("Iniciando apertura de jornada para: " + hoy);

        List<Personal> empleados = em.createQuery(
            "SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
            .getResultList();

        Feriados feriado = buscarFeriado(hoy, em);
        boolean esFeriado = (feriado != null);

        int contador = 0;

        for (Personal empleado : empleados) {
            try {
                AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, hoy);
                if (asistencia == null) {
                    asistencia = new AuditoriaRegistros();
                    asistencia.setEmpleado(empleado);
                    asistencia.setFecha(hoy);
                    em.persist(asistencia);
                }

                inicializarAsistencia(asistencia, empleado, hoy, feriado);
                em.merge(asistencia);

                // Flush y clear para liberar memoria cada 50 registros
                if (++contador % 50 == 0) {
                    em.flush();
                    em.clear();
                    System.out.println("Procesados " + contador + " empleados...");
                }

            } catch (LazyInitializationException lie) {
                System.err.println("LazyInitializationException en empleado ID: " + empleado.getId());
                lie.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error general en apertura para empleado ID: " + empleado.getId());
                e.printStackTrace();
            }
        }

        System.out.println("Apertura completada para " + empleados.size() + " empleados.");
    }


    /**
     * Inicializa los datos de la asistencia diaria en base a turno, feriado y licencias.
     */
    private void inicializarAsistencia(AuditoriaRegistros asistencia, Personal empleado, LocalDate hoy, Feriados feriado) {
    	
        DayOfWeek dia = hoy.getDayOfWeek();
        TurnosHorarios turno = empleado.getTurnoParaFecha(hoy);
        
        boolean esLaboral = turno != null && turno.esLaboral(dia);

        LocalTime entradaEsperada = (turno != null) ? turno.getEntradaParaDia(dia) : null;
        LocalTime salidaEsperada = (turno != null) ? turno.getSalidaParaDia(dia) : null;

        
        asistencia.setHoraEsperadaEntrada(entradaEsperada);
        asistencia.setHoraEsperadaSalida(salidaEsperada);
        asistencia.setMinutosEsperados((entradaEsperada != null && salidaEsperada != null)
            ? TiempoUtils.calcularMinutosLocalTime(entradaEsperada, salidaEsperada) : 0);

        asistencia.setJustificado(true);
        asistencia.setNota(null);
        asistencia.setFeriado(feriado != null);
    
        asistencia.setLicencia(false);
    

        // Evaluacion inicial
        if (Licencia.tieneLicenciaEnFecha(empleado, hoy)) {
            asistencia.setLicencia(true);
            asistencia.setEvaluacion(EvaluacionJornada.LICENCIA_JUSTIFICADA);
            asistencia.setNota("Licencia activa para hoy.");
           

        } else if (feriado != null) {
            asistencia.setEvaluacion(EvaluacionJornada.FERIADO);
            asistencia.setNota("Feriado: " + feriado.getMotivo());
           
        } else if (!esLaboral) {
            asistencia.setEvaluacion(EvaluacionJornada.DIA_NO_LABORAL);
            asistencia.setNota("DÌa sin turno asignado.");
            

        } else {
            asistencia.setEvaluacion(EvaluacionJornada.EN_CURSO);
            asistencia.setNota("Pendiente de ingreso.");
            asistencia.setJustificado(false);
        }
    }

    /**
     * Busca si existe un feriado para la fecha dada.
     */
    private Feriados buscarFeriado(LocalDate fecha, EntityManager em) {
        try {
            return em.createQuery("SELECT f FROM Feriados f WHERE f.fecha = :fecha", Feriados.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Busca si ya existe una AsistenciaDiaria para el empleado y fecha indicados.
     */
    private AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha) {
        try {
            LocalDateTime inicio = fecha.atStartOfDay();
            @SuppressWarnings("unused")
			LocalDateTime fin = inicio.plusDays(1);
            return XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                              "WHERE a.empleado = :emp " +
                              "AND a.fecha = :fecha", AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
