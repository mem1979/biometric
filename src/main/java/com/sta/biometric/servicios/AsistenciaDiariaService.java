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
     * de los registros del día. Solo agrega registros que no existan previamente
     * (evita duplicados comparando fecha, hora y tipo de movimiento).
     * Devuelve la instancia gestionada de {@link AuditoriaRegistros}.
     */
    public static AuditoriaRegistros consolidarDia(Personal empleado, LocalDate fecha,
            List<ColeccionRegistros> registrosDelDia) {
        EntityManager em = XPersistence.getManager();

        if (empleado == null || fecha == null)
            return null;

        AuditoriaRegistros asistencia = buscarAsistenciaDiaria(empleado, fecha);
        boolean esNueva = false;
        if (asistencia == null) {
            asistencia = new AuditoriaRegistros();
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(fecha);
            em.persist(asistencia);
            esNueva = true;
        }

        // Agregar solo registros que no existan (evitar duplicados)
        if (registrosDelDia != null && !registrosDelDia.isEmpty()) {
            // Normalizar secuencia de fichadas (corrige ENTRADA/SALIDA genéricas)
            registrosDelDia = InterpreteFichadasService.normalizarSecuencia(registrosDelDia);

            int agregados = 0;
            for (ColeccionRegistros nuevoRegistro : registrosDelDia) {
                if (!existeRegistroSimilar(asistencia, nuevoRegistro)) {
                    nuevoRegistro.setAsistenciaDiaria(asistencia);
                    asistencia.getRegistros().add(nuevoRegistro);
                    agregados++;
                }
            }
            // Solo reconsolidar si se agregaron nuevos registros
            if (agregados > 0) {
                asistencia.consolidarDesdeRegistros();
            }
        }

        if (!esNueva) {
            asistencia = em.merge(asistencia);
        }

        em.flush();
        return asistencia;
    }

    /**
     * Verifica si ya existe un registro similar en la asistencia.
     * Se considera duplicado si tiene la misma hora y tipo de movimiento.
     */
    private static boolean existeRegistroSimilar(AuditoriaRegistros asistencia, ColeccionRegistros nuevoRegistro) {
        if (asistencia.getRegistros() == null || asistencia.getRegistros().isEmpty()) {
            return false;
        }

        for (ColeccionRegistros existente : asistencia.getRegistros()) {
            // Comparar hora (con tolerancia de 1 minuto) y tipo de movimiento
            if (existente.getTipoMovimiento() == nuevoRegistro.getTipoMovimiento()
                    && horasSimilares(existente.getHora(), nuevoRegistro.getHora())) {
                return true; // Ya existe un registro similar
            }
        }
        return false;
    }

    /**
     * Compara dos horas con tolerancia de 5 minutos.
     */
    private static boolean horasSimilares(LocalTime hora1, LocalTime hora2) {
        if (hora1 == null || hora2 == null)
            return false;
        long diffMinutos = Math.abs(hora1.toSecondOfDay() - hora2.toSecondOfDay()) / 60;
        return diffMinutos <= 5; // Tolerancia de 5 minutos
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
