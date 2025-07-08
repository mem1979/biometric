package com.sta.biometric.servicios;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

/**
 * Servicio para consolidar registros diarios de asistencia de los empleados.
 * 
 * Refactorizado para utilizar EvaluacionJornada como Ãºnico resultado final.
 */
	public class AsistenciaDiariaService {

    /**
     * Consolida la asistencia de un empleado en una fecha especafica
     * a partir de sus registros del dia.
     * @param hora 
   /*
    * 
    * @param empleado
    * @param fecha
    * @param hora
    * @param registrosDelDia
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

