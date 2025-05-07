package com.sta.biometric.qartzJobs;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
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
        EntityManager em = XPersistence.getManager();

        System.out.println("Iniciando cierre de jornada para " + hoy);

        try {
            // Obtenemos todas las asistencias correspondientes al día de hoy
            List<AuditoriaRegistros> asistencias = em.createQuery(
                "SELECT a FROM AuditoriaRegistros a WHERE a.fechaEsperada = :fecha", AuditoriaRegistros.class)
                .setParameter("fecha", hoy)
                .getResultList();

            for (AuditoriaRegistros asistencia : asistencias) {
                try {
                    // Consolidamos la jornada en base a los registros obtenidos
                    asistencia.consolidarDesdeRegistros();
                    em.merge(asistencia);
                } catch (Exception e) {
                    System.err.println("Error al consolidar asistencia para " + 
                        (asistencia.getEmpleado() != null ? asistencia.getEmpleado().getNombreCompleto() : "Empleado desconocido")
                        + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error general al cerrar jornada: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
