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
 * Job para cerrar jornadas nocturnas del día anterior.
 * 
 * Se ejecuta a las 08:00 AM (después de que terminan los turnos nocturnos
 * típicos).
 * Busca jornadas del día anterior que tengan:
 * - esJornadaNocturna = true
 * - evaluacion = EN_CURSO o PENDIENTE
 * 
 * Y las consolida para calcular las horas trabajadas correctamente.
 * 
 * <p>
 * <strong>Nota JPA:</strong> Usa {@code XPersistence.createManager()} para
 * reutilizar el EntityManagerFactory singleton de OpenXava.
 * </p>
 */
@DisallowConcurrentExecution
public class CierreJornadaNocturnaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate ayer = LocalDate.now().minusDays(1);
        System.out.println("[CierreJornadaNocturnaJob] ===== INICIO " + LocalDateTime.now() + " =====");
        System.out.println("[CierreJornadaNocturnaJob] Buscando jornadas nocturnas de: " + ayer);

        // Usar XPersistence.createManager() para reutilizar el EMFactory singleton
        EntityManager em = XPersistence.createManager();

        try {
            em.getTransaction().begin();

            // Buscar jornadas nocturnas de ayer que estén EN_CURSO o PENDIENTE
            List<AuditoriaRegistros> nocturnas = em.createQuery(
                    "SELECT a FROM AuditoriaRegistros a " +
                            "WHERE a.fecha = :fecha " +
                            "AND a.esJornadaNocturna = true " +
                            "AND a.evaluacion IN :estados",
                    AuditoriaRegistros.class)
                    .setParameter("fecha", ayer)
                    .setParameter("estados", java.util.Arrays.asList(
                            EvaluacionJornada.EN_CURSO,
                            EvaluacionJornada.PENDIENTE))
                    .getResultList();

            System.out.println("[CierreJornadaNocturnaJob] Jornadas nocturnas pendientes: " + nocturnas.size());

            if (nocturnas.isEmpty()) {
                em.getTransaction().commit();
                System.out.println("[CierreJornadaNocturnaJob] ===== FIN (nada que procesar) =====");
                return;
            }

            int cerradas = 0;
            int errores = 0;
            int pospuestas = 0;
            LocalTime ahora = LocalTime.now();

            for (AuditoriaRegistros asistencia : nocturnas) {
                try {
                    // === VERIFICAR HORA DE SALIDA ESPERADA ===
                    LocalTime horaSalidaEsperada = asistencia.getHoraEsperadaSalida();
                    if (horaSalidaEsperada != null && ahora.isBefore(horaSalidaEsperada)) {
                        System.out.println("  [⏳] Pospuesta (termina " + horaSalidaEsperada + "): " +
                                (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                        : "Empleado desconocido"));
                        pospuestas++;
                        continue;
                    }
                    // === FIN VERIFICACIÓN ===

                    // DELEGACIÓN AL SERVICIO - Pasando EntityManager
                    GestionJornadasService.getInstance().cerrarJornada(asistencia, em);
                    cerradas++;
                    System.out.println("  [✓] Cerrada: " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido"));

                } catch (Exception e) {
                    errores++;
                    System.err.println("  [!] Error cerrando " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido")
                            + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            em.getTransaction().commit();
            System.out.println("[CierreJornadaNocturnaJob] Resultado: " + cerradas + " cerradas, " +
                    pospuestas + " pospuestas, " + errores + " errores.");
            System.out.println("[CierreJornadaNocturnaJob] ===== FIN " + LocalDateTime.now() + " =====");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("[CierreJornadaNocturnaJob] ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            // NO cerrar XPersistence factory - es singleton compartido
        }
    }
}
