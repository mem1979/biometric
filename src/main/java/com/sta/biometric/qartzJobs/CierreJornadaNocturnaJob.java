package com.sta.biometric.qartzJobs;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.quartz.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Job para cerrar jornadas nocturnas del día anterior.
 * 
 * Se ejecuta a las 08:00 AM (después de que terminan los turnos nocturnos
 * típicos).
 * Busca jornadas del día anterior que tengan:
 * - esJornadaNocturna = true
 * - evaluacion = EN_CURSO
 * 
 * Y las consolida para calcular las horas trabajadas correctamente.
 */
@DisallowConcurrentExecution
public class CierreJornadaNocturnaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate ayer = LocalDate.now().minusDays(1);
        System.out.println("[CierreJornadaNocturnaJob] Cerrando jornadas nocturnas de: " + ayer);

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
        EntityManager em = factory.createEntityManager();

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

            if (nocturnas.isEmpty()) {
                System.out.println("[CierreJornadaNocturnaJob] No hay jornadas nocturnas pendientes.");
                em.getTransaction().commit();
                return;
            }

            int cerradas = 0;
            int errores = 0;
            int pospuestas = 0;
            LocalTime ahora = LocalTime.now();

            for (AuditoriaRegistros asistencia : nocturnas) {
                try {
                    // === VERIFICAR HORA DE SALIDA ESPERADA ===
                    // No cerrar si el turno aún no debería haber terminado
                    LocalTime horaSalidaEsperada = asistencia.getHoraEsperadaSalida();
                    if (horaSalidaEsperada != null && ahora.isBefore(horaSalidaEsperada)) {
                        System.out.println("  [⏳] Pospuesta (termina " + horaSalidaEsperada + "): " +
                                (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                        : "Empleado desconocido"));
                        pospuestas++;
                        continue;
                    }
                    // === FIN VERIFICACIÓN ===

                    asistencia.consolidarDesdeRegistros();
                    em.merge(asistencia);
                    cerradas++;
                    System.out.println("  [✓] Cerrada: " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido"));
                } catch (Exception e) {
                    errores++;
                    System.err.println("  [!] Error cerrando jornada nocturna para " +
                            (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto()
                                    : "Empleado desconocido")
                            + ": " + e.getMessage());
                }
            }

            em.getTransaction().commit();
            System.out.println(
                    "[CierreJornadaNocturnaJob] Resultado: " + cerradas + " cerradas, " +
                            pospuestas + " pospuestas, " + errores + " errores.");

        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("[!] Error general en cierre nocturno: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            factory.close();
        }
    }
}
