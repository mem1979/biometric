package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.quartz.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Tarea programada para cerrar automaticamente la jornada diaria consolidando
 * los registros.
 * Se ejecuta todos los dias a las 23:59 hs mediante Quartz Scheduler.
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

            int cerrados = 0;
            int postponed = 0;

            for (AuditoriaRegistros asistencia : asistencias) {
                try {
                    // === SOPORTE JORNADAS NOCTURNAS ===
                    // Skip jornadas nocturnas en curso: serán cerradas mañana a las 08:00
                    // por CierreJornadaNocturnaJob
                    if (asistencia.isEsJornadaNocturna() &&
                            asistencia.getEvaluacion() == EvaluacionJornada.EN_CURSO) {
                        System.out.println("  [⏳] Postponiendo cierre nocturno: " +
                                (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                        : "Empleado desconocido"));
                        postponed++;
                        continue;
                    }
                    // === FIN SOPORTE NOCTURNAS ===

                    asistencia.consolidarDesdeRegistros();
                    em.merge(asistencia);
                    cerrados++;
                } catch (Exception e) {
                    System.err.println("[!] Error consolidando para " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido")
                            + ": " + e.getMessage());
                }
            }

            em.getTransaction().commit();
            System.out.println(
                    "[CierreJornadaJob] Cierre: " + cerrados + " cerrados, " + postponed + " nocturnas postponed.");

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