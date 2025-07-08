package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.quartz.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;

/**
 * Tarea programada para generar la apertura de jornada diaria para todos los empleados activos.
 * Ejecutada automáticamente a las 00:01 hs o manualmente desde una acción.
 */
@DisallowConcurrentExecution
public class AperturaJornadaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate hoy = LocalDate.now();
        System.out.println("[AperturaJornadaJob] Iniciando apertura de jornada para: " + hoy);

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
        EntityManager em = factory.createEntityManager();

        try {
            em.getTransaction().begin();

            List<Personal> empleados = em.createQuery(
                "SELECT e FROM Personal e WHERE e.activo = true", Personal.class)
                .getResultList();

            Feriados feriado = buscarFeriado(hoy, em);
            int contador = 0;

            for (Personal empleado : empleados) {
                try {
                    AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, hoy, em);

                    boolean esNueva = (asistencia == null);
                    if (esNueva) {
                        asistencia = new AuditoriaRegistros();
                        asistencia.setEmpleado(empleado);
                        asistencia.setFecha(hoy);
                        em.persist(asistencia);
                        System.out.println("  [+] Nueva asistencia creada para: " + empleado.getNombreCompleto());
                    }

                    inicializarAsistencia(asistencia, empleado, hoy, feriado);
                    asistencia.setLicencia(asistencia.getConLicencia());
                    asistencia.setFeriado(asistencia.getEsFeriado());
                    em.merge(asistencia);

                    contador++;

                } catch (Exception e) {
                    System.err.println("[!] Error procesando a " + empleado.getNombreCompleto() + ": " + e.getMessage());
                }
            }

            em.getTransaction().commit();
            System.out.println("[AperturaJornadaJob] Apertura completada para " + contador + " empleados activos.");

        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("[!] Error general en apertura de jornada: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            factory.close();
        }
    }

    private AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha, EntityManager em) {
        try {
            return em.createQuery("SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha", AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Feriados buscarFeriado(LocalDate fecha, EntityManager em) {
        try {
            return em.createQuery("SELECT f FROM Feriados f WHERE f.fecha = :fecha", Feriados.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void inicializarAsistencia(AuditoriaRegistros asistencia, Personal empleado, LocalDate hoy, Feriados feriado) {
        DayOfWeek dia = hoy.getDayOfWeek();
        TurnosHorarios turno = empleado.getTurnoParaFecha(hoy);
        boolean esLaboral = turno != null && turno.esLaboral(dia);

        LocalTime entrada = (turno != null) ? turno.getEntradaParaDia(dia) : null;
        LocalTime salida = (turno != null) ? turno.getSalidaParaDia(dia) : null;

        asistencia.setHoraEsperadaEntrada(entrada);
        asistencia.setHoraEsperadaSalida(salida);
        asistencia.setMinutosEsperados((entrada != null && salida != null)
            ? TiempoUtils.calcularMinutosLocalTime(entrada, salida)
            : 0);
        asistencia.setNota(null);

        if (asistencia.getConLicencia()) {
            asistencia.setEvaluacion(EvaluacionJornada.LICENCIA);
            asistencia.setNota("Licencia activa para hoy.");
            asistencia.setJustificado(true);
        } else if (asistencia.getEsFeriado()) {
            asistencia.setEvaluacion(EvaluacionJornada.FERIADO);
            asistencia.setNota("Feriado: " + (feriado != null ? feriado.getMotivo() : "Sin motivo especificado"));
            asistencia.setJustificado(true);
        } else if (!esLaboral) {
            asistencia.setEvaluacion(EvaluacionJornada.DIA_NO_LABORAL);
            asistencia.setNota("Día sin turno asignado.");
            asistencia.setJustificado(false);
        } else {
            asistencia.setEvaluacion(EvaluacionJornada.EN_CURSO);
            asistencia.setNota("Pendiente de ingreso.");
            asistencia.setJustificado(false);
        }
    }
}
