package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.quartz.*;

import com.sta.biometric.modelo.*;

/**
 * Tarea programada para cerrar automáticamente la jornada diaria consolidando los registros.
 * Se ejecuta todos los días a las 23:59 hs mediante Quartz Scheduler.
 */
@DisallowConcurrentExecution
public class CierreJornadaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate hoy = LocalDate.now();
        System.out.println("[CierreJornadaJob] Iniciando cierre de jornada para: " + hoy);

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
        EntityManager em = factory.createEntityManager();

        try {
            em.getTransaction().begin();

            List<AuditoriaRegistros> asistencias = em.createQuery(
                "SELECT a FROM AuditoriaRegistros a WHERE a.fecha = :fecha", AuditoriaRegistros.class)
                .setParameter("fecha", hoy)
                .getResultList();

            for (AuditoriaRegistros asistencia : asistencias) {
                try {
                    asistencia.consolidarDesdeRegistros();
                    em.merge(asistencia);
                } catch (Exception e) {
                    System.err.println("[!] Error consolidando para " +
                        (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto() : "Empleado desconocido")
                        + ": " + e.getMessage());
                }
            }

            em.getTransaction().commit();
            System.out.println("[CierreJornadaJob] Cierre completado para " + asistencias.size() + " registros.");

        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("[!] Error general al cerrar jornada: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            factory.close();
        }
    }
}

