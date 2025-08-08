package com.sta.biometric.modelo;
import java.time.*;
import java.time.format.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;

import lombok.*;

/**
 * 
 * 
 * Entidad que representa un registro individual (antes era embebido).
@EntityListeners(ColeccionRegistrosListener.class)
 * Ahora cada ColeccionRegistros sabe cÃ³mo evaluarse a sÃ­ mismo
 * a partir del turno asignado al empleado y la hora de fichada.
 * 
 * 
 */

@View(members= 
				"fecha, hora, tipoMovimiento;"+
				"evaluacion;" +
				"observacion;" +
				"coordenada" )

@Tab( properties = "diaSemana, fecha, hora, tipoMovimiento, evaluacion",
	   defaultOrder = "${fecha} asc"
	)

@Entity
@Getter @Setter
public class ColeccionRegistros extends Identifiable {

    /**
     * Relacion muchos-a-uno con la tabla de asistencia diaria.
     * El mappedBy en AsistenciaDiaria es "registros".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistencia_diaria_id")
    private AuditoriaRegistros asistenciaDiaria;

    /**
     * Fecha y hora exacta en que se registrÃ³ la fichada.
     */
    @ReadOnly
    private LocalDate fecha;
    
 
    /**
    * metodo adicional para mostrar el dia de la semana en español.
     */
    @Transient
    @ReadOnly
    @Depends("fecha")
    public String getDiaSemana() {
        if (fecha == null) return "";
        return fecha.getDayOfWeek().getDisplayName(
            TextStyle.FULL, new Locale("es", "ES")
        ).toUpperCase();
    }
    
    @ReadOnly
    private LocalTime hora;

    /**
     * Coordenada geografica (lat, lon), u otro identificador de ubicacion.
     */
    @ReadOnly
    @Coordinates 
    @Column(length = 50)
    private String coordenada;

    /**
     * Tipo de movimiento al que corresponde (Entrada, Salida, etc.).
     */
    @ReadOnly
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;

    /**
     * Observacion o comentario adicional del registro.
     */
    @TextArea
    private String observacion;

    @ReadOnly
    private String evaluacion;
    
    @Transient
    public String getEvaluacion() {
    	
        if (asistenciaDiaria == null) {
            return "ERROR DE REGISTRO - SIN ASISTENCIA DIARIA";
        }
        
        if (fecha == null || tipoMovimiento == null) {
            return "ERROR DE REGISTRO - SIN DATOS";
        }
        Personal empleado = asistenciaDiaria.getEmpleado();
        if (empleado == null) {
            return "ERROR DE REGISTRO - SIN EMPLEADO";
        }

        // Obtenemos la fecha y el dia de la semana
        
          DayOfWeek dia = fecha.getDayOfWeek();

        // Buscamos el turno asignado al empleado para esa fecha
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        if (turno == null) {
            return "SIN TURNO ASIGNADO";
        }
        if (!turno.esLaboral(dia)) {
            return "DIA NO LABORAL";
        }

   

        // Determinamos horas esperadas y tolerancia
        LocalTime entradaEsperada = turno.getEntradaParaDia(dia);
        LocalTime salidaEsperada = turno.getSalidaParaDia(dia);
        int tolerancia = (turno.getTolerancia() != null) ? turno.getTolerancia() : 5;

        switch (tipoMovimiento) {
            case ENTRADA:
                if (entradaEsperada == null) return "SIN HORARIO DE ENTRADA";
                if (hora.isBefore(entradaEsperada.minusMinutes(tolerancia))) return "ENTRADA ANTICIPADA";
                if (hora.isAfter(entradaEsperada.plusMinutes(tolerancia))) return "ENTRADA TARDE";
                return "ENTRADA EN HORARIO";

            case SALIDA:
                if (salidaEsperada == null) return "SIN HORARIO DE SALIDA";
                if (hora.isBefore(salidaEsperada.minusMinutes(tolerancia))) return "SALIDA ANTICIPADA";
                if (hora.isAfter(salidaEsperada.plusMinutes(tolerancia))) return "SALIDA TARDIA";
                return "SALIDA EN HORARIO";

            // Otros tipos de movimiento (Pausa, Ubicacion, etc.)
            case PAUSA_INICIO:
                return "INICIO PAUSA";
            case PAUSA_FIN:
                return "FIN PAUSA";
            case UBICACION:
                return "UBICACION";
            case MANUAL:
                return "REGISTRO MANUAL";

            default:
                return "REGISTRO NO VALIDADO - TIPO DE MOVIMIENTO INCORRECTO";
        }
    }

    
    @PrePersist @PreUpdate
    private void preGuardarActualizar() {
        setEvaluacion(getEvaluacion());
     }
}
