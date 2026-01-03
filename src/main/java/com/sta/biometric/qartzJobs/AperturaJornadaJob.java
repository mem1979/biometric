package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.quartz.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Tarea programada para generar la apertura de jornada diaria para todos los
 * empleados activos.
 * Ejecutada automáticamente a las 00:00 hs .
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

                    if (asistencia == null) {
                        asistencia = new AuditoriaRegistros();
                        asistencia.setEmpleado(empleado);
                        asistencia.setFecha(hoy);
                        asistencia.setLicencia(Licencia.tieneLicenciaEnFecha(empleado, hoy));
                        asistencia.setFeriado(feriado != null);
                        inicializarAsistencia(asistencia, empleado, hoy, feriado);
                        em.persist(asistencia);
                        System.out.println("  [+] Nueva asistencia creada para: " + empleado.getNombreCompleto());
                    } else {
                        asistencia.setLicencia(Licencia.tieneLicenciaEnFecha(empleado, hoy));
                        asistencia.setFeriado(feriado != null);
                        inicializarAsistencia(asistencia, empleado, hoy, feriado);
                        em.merge(asistencia);
                    }

                    contador++;

                } catch (Exception e) {
                    System.err
                            .println("[!] Error procesando a " + empleado.getNombreCompleto() + ": " + e.getMessage());
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
            return em
                    .createQuery("SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                            AuditoriaRegistros.class)
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

    private void inicializarAsistencia(AuditoriaRegistros asistencia, Personal empleado, LocalDate hoy,
            Feriados feriado) {

        // 1. Inicializar datos del turno (Horarios, Nombre, Tolerancia)
        asistencia.inicializarTurnoYCondiciones();

        // 2. Determinar Evaluación Inicial
        TurnosHorarios turno = empleado.getTurnoParaFecha(hoy);
        boolean esLaboral = turno != null && turno.esLaboral(hoy.getDayOfWeek());

        if (asistencia.isLicencia()) {
            asistencia.setEvaluacion(EvaluacionJornada.LICENCIA);
            asistencia.setJustificado(true);
        } else if (asistencia.isFeriado()) {
            asistencia.setEvaluacion(EvaluacionJornada.FERIADO);
            asistencia.setJustificado(true);
        } else if (!esLaboral) {
            asistencia.setEvaluacion(EvaluacionJornada.DIA_NO_LABORAL);
            asistencia.setJustificado(false);
        } else {
            asistencia.setEvaluacion(EvaluacionJornada.PENDIENTE);
            asistencia.setJustificado(false);
            if (asistencia.getNota() == null || asistencia.getNota().isBlank()) {
                asistencia.setNota("Pendiente de ingreso.");
            }
        }

        // 3. Generar nota con detalles (Licencia tipo, Feriado motivo, etc.)
        asistencia.actualizarNotaSegunEvaluacion();
    }
}
