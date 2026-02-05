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
 * Tarea programada para generar la apertura de jornada diaria para todos los
 * empleados activos.
 * Ejecutada automáticamente a las 00:01 hs.
 * 
 * <p>
 * <strong>Nota JPA:</strong> Usa {@code XPersistence.createManager()} para
 * reutilizar el EntityManagerFactory singleton de OpenXava.
 * </p>
 */
@DisallowConcurrentExecution
public class AperturaJornadaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate hoy = LocalDate.now();
        System.out.println("[AperturaJornadaJob] ===== INICIO " + LocalDateTime.now() + " =====");
        System.out.println("[AperturaJornadaJob] Procesando fecha: " + hoy);

        // Usar XPersistence.createManager() para reutilizar el EMFactory singleton
        EntityManager em = XPersistence.createManager();

        try {
            em.getTransaction().begin();

            List<Personal> empleados = em.createQuery(
                    "SELECT e FROM Personal e WHERE e.activo = true AND e.eliminado = false", Personal.class)
                    .getResultList();

            System.out.println("[AperturaJornadaJob] Empleados activos encontrados: " + empleados.size());

            int contador = 0;
            int omitidos = 0;

            for (Personal empleado : empleados) {
                try {
                    // === VERIFICAR JORNADA NOCTURNA EN CURSO ===
                    LocalDate ayer = hoy.minusDays(1);
                    AuditoriaRegistros jornadaNocturnaAbierta = buscarJornadaNocturnaEnCurso(empleado, ayer, em);

                    if (jornadaNocturnaAbierta != null) {
                        System.out.println("  [⏳] Omitida: " + empleado.getNombreCompleto() +
                                " - jornada nocturna en curso desde ayer");
                        omitidos++;
                        continue;
                    }
                    // === FIN VERIFICACIÓN ===

                    // DELEGACIÓN AL SERVICIO - Pasando EntityManager
                    GestionJornadasService.getInstance().abrirOActualizarJornada(empleado, hoy, em);
                    contador++;

                } catch (Exception e) {
                    System.err.println("[!] Error procesando " + empleado.getNombreCompleto() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            em.getTransaction().commit();
            System.out.println("[AperturaJornadaJob] Resultado: " + contador + " abiertos, " + omitidos
                    + " omitidos por nocturna.");
            System.out.println("[AperturaJornadaJob] ===== FIN " + LocalDateTime.now() + " =====");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("[AperturaJornadaJob] ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            // NO cerrar XPersistence factory - es singleton compartido
        }
    }

    /**
     * Busca si el empleado tiene una jornada nocturna EN_CURSO para la fecha
     * indicada.
     */
    private AuditoriaRegistros buscarJornadaNocturnaEnCurso(Personal empleado, LocalDate fecha, EntityManager em) {
        try {
            return em.createQuery(
                    "SELECT a FROM AuditoriaRegistros a " +
                            "WHERE a.empleado = :emp " +
                            "AND a.fecha = :fecha " +
                            "AND a.esJornadaNocturna = true " +
                            "AND a.evaluacion = :estado",
                    AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("fecha", fecha)
                    .setParameter("estado", EvaluacionJornada.EN_CURSO)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
