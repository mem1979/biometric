package com.sta.biometric.embebidas;

import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.modelo.*;

import lombok.*;

/**
 * Entidad que representa una asignación de turno a un empleado.
 * 
 * <p>
 * Cada JornadaAsignada vincula un {@link TurnosHorarios} con un empleado
 * para un rango de fechas específico.
 * </p>
 * 
 * <p>
 * <b>Tipos de asignación:</b>
 * </p>
 * <ul>
 * <li><b>Turno fijo:</b> fechaInicio y fechaFin definidos</li>
 * <li><b>Turno rotativo:</b> fechaFin = null (indefinido)</li>
 * </ul>
 * 
 * @see Personal
 * @see TurnosHorarios
 */
@Entity
@Table(name = "jornada_asignada")
@Getter
@Setter
public class JornadaAsignada extends Identifiable {

    /**
     * Empleado al que pertenece esta asignación de jornada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    /**
     * Turno asignado al empleado para este período.
     */
    @Required
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "turno_id")
    @DescriptionsList(descriptionProperties = "codigo, detalleJornadaHoras", order = "CASE " +
            "WHEN ${codigo} LIKE 'TM%' THEN 1 " +
            "WHEN ${codigo} LIKE 'TT%' THEN 2 " +
            "WHEN ${codigo} LIKE 'TN%' THEN 3 " +
            "WHEN ${codigo} LIKE 'TE%' THEN 4 " +
            "ELSE 5 END, ${codigo}")
    private TurnosHorarios turno;

    /**
     * Fecha de inicio de vigencia del turno.
     */
    @Required
    @Stereotype("FECHA")
    private LocalDate fechaInicio;

    /**
     * Fecha de fin de vigencia del turno.
     * Si es null, el turno es indefinido (rotativo).
     */
    @Stereotype("FECHA")
    private LocalDate fechaFin;

    /**
     * Verifica si esta jornada está vigente para una fecha específica.
     * 
     * @param fecha Fecha a verificar
     * @return true si la jornada aplica para la fecha
     */
    @Transient
    public boolean isVigenteParaFecha(LocalDate fecha) {
        if (fechaInicio == null || fecha == null)
            return false;
        if (fecha.isBefore(fechaInicio))
            return false;
        if (fechaFin != null && fecha.isAfter(fechaFin))
            return false;
        return true;
    }

    /**
     * Indica si es una jornada rotativa (sin fecha fin).
     */
    @Transient
    public boolean isRotativa() {
        return fechaFin == null;
    }
}
