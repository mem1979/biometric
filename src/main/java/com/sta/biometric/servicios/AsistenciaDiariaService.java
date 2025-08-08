package com.sta.biometric.servicios;

import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

/**
 * Servicio para consolidar registros diarios de asistencia de los empleados.
 */
public class AsistenciaDiariaService {

    /**
     * Consolida la asistencia de un empleado en una fecha específica a partir
     * de los registros del día. Devuelve la instancia gestionada de
     * {@link AuditoriaRegistros} y realiza un {@link EntityManager#flush()} al
     * finalizar.
     */
    public static AuditoriaRegistros consolidarDia(Personal empleado, LocalDate fecha,
            List<ColeccionRegistros> registrosDelDia) {
        EntityManager em = XPersistence.getManager();

        if (empleado == null || fecha == null) return null;

        AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, fecha);
        boolean esNueva = false;
        if (asistencia == null) {
            asistencia = new AuditoriaRegistros();
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(fecha);
            em.persist(asistencia);
            esNueva = true;
        } else {
            asistencia.getRegistros().clear();
        }

        if (registrosDelDia != null && !registrosDelDia.isEmpty()) {
            for (ColeccionRegistros registro : registrosDelDia) {
                registro.setAsistenciaDiaria(asistencia);
                asistencia.getRegistros().add(registro);
            }
        }

        asistencia.consolidarDesdeRegistros();

        if (!esNueva) {
            asistencia = em.merge(asistencia);
        }

        em.flush();
        return asistencia;
    }

    /**
     * Busca la asistencia diaria de un empleado para una fecha específica. Si no
     * existe, retorna {@code null}.
     */
    private static AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha) {
        try {
            return XPersistence.getManager()
                .createQuery(
                    "SELECT a FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                    AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

