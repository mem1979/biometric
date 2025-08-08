package com.sta.biometric.servicios;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

/**
 * Servicio para consolidar registros diarios de asistencia de los empleados.
 * 
public class AsistenciaDiariaService {
     * Consolida la asistencia de un empleado en una fecha especifica a partir de sus registros del dia.
     * Los registros duplicados (misma fecha y hora) son reemplazados.
     *
     * @param empleado Empleado a procesar
     * @param fecha Fecha de la asistencia
     * @param hora Hora de consolidacion (opcional)
     * @param registrosDelDia Registros capturados en el dia
     */
 // Si hay registros nuevos, se agregan deduplicando por fecha/hora
                asistencia.agregarRegistro(registro);
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
  //      asistencia.getRegistros().clear();

 // Si hay registros nuevos, los vinculamos
        if (registrosDelDia != null && !registrosDelDia.isEmpty()) {
            for (ColeccionRegistros registro : registrosDelDia) {
                registro.setAsistenciaDiaria(asistencia);
                asistencia.getRegistros().add(registro);
            }
        }

 // Consolidamos usando la propia logica interna de AsistenciaDiaria
        asistencia.consolidarDesdeRegistros();

        em.merge(asistencia);
    }  


    /**
     * Busca la asistencia diaria de un empleado para una fecha específica.
     * Si no existe, retorna null.
     */
    private static AuditoriaRegistros buscarAsistenciaDiaria(Personal empleado, LocalDate fecha) {
        try {
            return XPersistence.getManager()
                .createQuery("SELECT a FROM AuditoriaRegistros a " +
                             "WHERE a.empleado = :emp " +
                             "AND a.fecha = :fecha", AuditoriaRegistros.class)
                .setParameter("emp", empleado)
                .setParameter("fecha", fecha)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

