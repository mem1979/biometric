package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.quartz.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.GestionJornadasService;

/**
 * Tarea programada para cerrar automáticamente la jornada diaria consolidando
 * los registros.
 * Se ejecuta todos los días a las 23:55 hs mediante Quartz Scheduler.
 * 
 * <p>
 * <strong>Nota JPA:</strong> Usa {@code XPersistence.createManager()} para
 * reutilizar el EntityManagerFactory singleton de OpenXava.
 * </p>
 */
@DisallowConcurrentExecution
public class CierreJornadaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate hoy = LocalDate.now();
        System.out.println("[CierreJornadaJob] ===== INICIO " + LocalDateTime.now() + " =====");
        System.out.println("[CierreJornadaJob] Procesando fecha: " + hoy);

        // Usar XPersistence.createManager() para reutilizar el EMFactory singleton
        EntityManager em = XPersistence.createManager();

        try {
            em.getTransaction().begin();

            List<AuditoriaRegistros> asistencias = em.createQuery(
                    "SELECT a FROM AuditoriaRegistros a WHERE a.fecha = :fecha", AuditoriaRegistros.class)
                    .setParameter("fecha", hoy)
                    .getResultList();

            System.out.println("[CierreJornadaJob] Jornadas encontradas: " + asistencias.size());

            int cerrados = 0;
            int postponed = 0;
            int errores = 0;

            for (AuditoriaRegistros asistencia : asistencias) {
                try {
                    // === SOPORTE JORNADAS NOCTURNAS ===
                    // Skip jornadas nocturnas en curso: serán cerradas por CierreJornadaNocturnaJob
                    if (asistencia.isEsJornadaNocturna() &&
                            asistencia.getEvaluacion() == EvaluacionJornada.EN_CURSO) {
                        System.out.println("  [⏳] Postponed (nocturna): " +
                                (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                        : "Empleado desconocido"));
                        postponed++;
                        continue;
                    }
                    // === FIN SOPORTE NOCTURNAS ===

                    // DELEGACIÓN AL SERVICIO - Pasando EntityManager
                    GestionJornadasService.getInstance().cerrarJornada(asistencia, em);
                    cerrados++;

                } catch (Exception e) {
                    errores++;
                    System.err.println("[!] Error consolidando " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido")
                            + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            em.getTransaction().commit();
            System.out.println("[CierreJornadaJob] Resultado: " + cerrados + " cerrados, " + postponed + " postponed, "
                    + errores + " errores.");
            System.out.println("[CierreJornadaJob] ===== FIN " + LocalDateTime.now() + " =====");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("[CierreJornadaJob] ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            // NO cerrar XPersistence factory - es singleton compartido
        }
    }
}