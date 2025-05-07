package com.sta.biometric.servicios;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

/**
 * Servicio para consolidar registros diarios de asistencia de los empleados.
 * 
 * Refactorizado para utilizar EvaluacionJornada como único resultado final.
 */
public class AsistenciaDiariaService {

    /**
     * Consolida la asistencia de un empleado en una fecha específica
     * a partir de sus registros del día.
     * @param hora 
     */
    public static void consolidarDia(Personal empleado, LocalDate fecha, LocalTime hora, List<ColeccionRegistros> registrosDelDia) {
        EntityManager em = XPersistence.getManager();

        if (empleado == null || fecha == null) return;

        // Buscamos si ya existe AsistenciaDiaria para el empleado y fecha
        AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, fecha);
        if (asistencia == null) {
            asistencia = new AuditoriaRegistros();
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(fecha);
            em.persist(asistencia);
        }

        // Limpiamos registros anteriores
        asistencia.getRegistros().clear();

        // Si hay registros nuevos, los vinculamos
        if (registrosDelDia != null && !registrosDelDia.isEmpty()) {
            for (ColeccionRegistros registro : registrosDelDia) {
                registro.setAsistenciaDiaria(asistencia);
                asistencia.getRegistros().add(registro);
            }
        }

        // Consolidamos usando la propia lógica interna de AsistenciaDiaria
        asistencia.consolidarDesdeRegistros();

        em.merge(asistencia);
    }

    /**
     * Busca la asistencia diaria de un empleado para una fecha específica.
     * Si no existe, retorna null.
     */
    private static AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha) {
        try {
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = inicio.plusDays(1);

            return XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                             "WHERE a.empleado = :emp " +
                             "AND a.fechaHoraInicio >= :inicio " +
                             "AND a.fechaHoraInicio < :fin", AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
