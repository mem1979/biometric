package com.sta.biometric.servicios;

import java.time.LocalDate;
import java.util.List;

import org.openxava.jpa.XPersistence;

import com.sta.biometric.auxiliares.Licencia;
import com.sta.biometric.modelo.AuditoriaRegistros;
import com.sta.biometric.modelo.Personal;

import javax.persistence.EntityManager;

/**
 * Servicio para recalcular registros de asistencia al modificar licencias.
 * 
 * <p>
 * Cuando se crea o modifica una licencia, este servicio busca todos los
 * registros de asistencia afectados (en el rango de fechas de la licencia)
 * y los recalcula para reflejar el nuevo estado.
 * </p>
 */
public class LicenciaRecalculacionService {

    /**
     * Recalcula los registros de asistencia para el rango de fechas de una
     * licencia.
     * 
     * <p>
     * Este método se invoca automáticamente desde los callbacks @PostPersist
     * y @PostUpdate
     * de la entidad Licencia.
     * </p>
     * 
     * @param licencia La licencia creada o modificada
     */
    public static void recalcularPorLicencia(Licencia licencia) {
        if (licencia == null || licencia.getEmpleado() == null) {
            return;
        }

        Personal empleado = licencia.getEmpleado();
        LocalDate desde = licencia.getFechaInicio();
        LocalDate hasta = licencia.getFechaFin();

        if (desde == null || hasta == null) {
            return;
        }

        EntityManager em = XPersistence.getManager();

        // Buscar todos los registros de asistencia en el rango de la licencia
        List<AuditoriaRegistros> registros = em
                .createQuery(
                        "SELECT a FROM AuditoriaRegistros a " +
                                "WHERE a.empleado = :emp " +
                                "AND a.fecha BETWEEN :desde AND :hasta",
                        AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        // Recalcular cada registro afectado
        for (AuditoriaRegistros registro : registros) {
            registro.consolidarDesdeRegistros();
        }
    }
}
