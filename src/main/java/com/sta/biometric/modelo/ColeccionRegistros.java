package com.sta.biometric.modelo;

import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;

import lombok.*;

/**
 * Entidad que representa un registro individual de fichada (entrada, salida,
 * pausa).
 * 
 * <p>
 * Cada {@code ColeccionRegistros} pertenece a un {@link AuditoriaRegistros}
 * (asistencia diaria)
 * y se evalúa automáticamente comparando la hora de fichada contra el turno
 * asignado al empleado.
 * </p>
 * 
 * <p>
 * <b>Tipos de fichada soportados:</b>
 * </p>
 * <ul>
 * <li>{@link TipoMovimiento#ENTRADA} - Registro de entrada al trabajo</li>
 * <li>{@link TipoMovimiento#SALIDA} - Registro de salida del trabajo</li>
 * <li>{@link TipoMovimiento#PAUSA_INICIO} - Inicio de pausa/descanso</li>
 * <li>{@link TipoMovimiento#PAUSA_FIN} - Fin de pausa/descanso</li>
 * <li>{@link TipoMovimiento#UBICACION} - Registro de ubicación GPS</li>
 * <li>{@link TipoMovimiento#MANUAL} - Registro manual por administrador</li>
 * </ul>
 * 
 * <p>
 * <b>Evaluaciones posibles:</b>
 * </p>
 * <ul>
 * <li>"ENTRADA EN HORARIO" / "SALIDA EN HORARIO" - Dentro de tolerancia</li>
 * <li>"ENTRADA TARDE" / "SALIDA ANTICIPADA" - Fuera de tolerancia</li>
 * <li>"ENTRADA ANTICIPADA" / "SALIDA TARDIA" - Antes/después de lo
 * esperado</li>
 * <li>"SIN TURNO ASIGNADO" - Empleado sin turno para la fecha</li>
 * <li>"DIA NO LABORAL" - El turno no tiene ese día como laboral</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @version 1.0
 * @see AuditoriaRegistros
 * @see TipoMovimiento
 * @see Personal#getTurnoParaFecha(LocalDate)
 */

@View(members = "fecha, hora, tipoMovimiento;" +
        "evaluacion;" +
        "observacion;" +
        "coordenada")

@Tab(properties = "asistenciaDiaria.empleado.userId, asistenciaDiaria.empleado.nombreCompleto, diaSemana, fecha, hora, tipoMovimiento, evaluacion", defaultOrder = "${fecha} asc")

@Entity
@Getter
@Setter
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

    /**
     * Devuelve el nombre del día de la semana en español para la fecha del
     * registro.
     */
    @Transient
    @ReadOnly
    @Depends("fecha")
    public String getDiaSemana() {
        return TiempoUtils.obtenerNombreDia(fecha);
    }

    /**
     * Hora exacta en que se registró la fichada.
     * 
     * <p>
     * Formato: HH:MM:SS
     * </p>
     */
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

    /**
     * Resultado de la evaluación de la fichada.
     * 
     * <p>
     * Se calcula automáticamente en {@link #preGuardarActualizar()}
     * comparando la hora contra el turno esperado.
     * </p>
     * 
     * @see #calcularEvaluacion()
     */
    @ReadOnly
    private String evaluacion;

    /**
     * Calcula la evaluación de la fichada comparando contra el turno esperado.
     * 
     * <p>
     * Este método realiza las siguientes validaciones:
     * </p>
     * <ol>
     * <li>Verifica que exista asistencia diaria asociada</li>
     * <li>Verifica que haya fecha y tipo de movimiento</li>
     * <li>Verifica que el empleado tenga turno asignado</li>
     * <li>Verifica que el día sea laboral según el turno</li>
     * <li>Compara la hora de fichada contra el horario esperado (± tolerancia)</li>
     * </ol>
     * 
     * <p>
     * <b>Evaluaciones de ENTRADA:</b>
     * </p>
     * <ul>
     * <li>"ENTRADA ANTICIPADA" - Antes de (entrada - tolerancia)</li>
     * <li>"ENTRADA EN HORARIO" - Dentro del rango de tolerancia</li>
     * <li>"ENTRADA TARDE" - Después de (entrada + tolerancia)</li>
     * </ul>
     * 
     * <p>
     * <b>Evaluaciones de SALIDA:</b>
     * </p>
     * <ul>
     * <li>"SALIDA ANTICIPADA" - Antes de (salida - tolerancia)</li>
     * <li>"SALIDA EN HORARIO" - Dentro del rango de tolerancia</li>
     * <li>"SALIDA TARDIA" - Después de (salida + tolerancia)</li>
     * </ul>
     * 
     * @return String con la evaluación del registro
     * @see TurnosHorarios#getEntradaParaDia(DayOfWeek)
     * @see TurnosHorarios#getSalidaParaDia(DayOfWeek)
     */
    @Transient
    public String calcularEvaluacion() {

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

        // Usamos la tolerancia persistida en el registro diario (snapshot)
        // para mantener consistencia histórica si el turno cambia después.
        int tolerancia = asistenciaDiaria.getToleranciaMinutos();

        switch (tipoMovimiento) {
            case ENTRADA:
                if (entradaEsperada == null)
                    return "SIN HORARIO DE ENTRADA";
                if (hora.isBefore(entradaEsperada.minusMinutes(tolerancia)))
                    return "ENTRADA ANTICIPADA";
                if (hora.isAfter(entradaEsperada.plusMinutes(tolerancia)))
                    return "ENTRADA TARDE";
                return "ENTRADA EN HORARIO";

            case SALIDA:
                if (salidaEsperada == null)
                    return "SIN HORARIO DE SALIDA";
                if (hora.isBefore(salidaEsperada.minusMinutes(tolerancia)))
                    return "SALIDA ANTICIPADA";
                if (hora.isAfter(salidaEsperada.plusMinutes(tolerancia)))
                    return "SALIDA TARDIA";
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

    /**
     * Retorna la evaluación calculada de la fichada.
     * 
     * @return Texto de evaluación (ej: "ENTRADA EN HORARIO")
     */
    public String getEvaluacion() {
        return evaluacion;
    }

    /**
     * Callback JPA que actualiza la evaluación antes de persistir o actualizar.
     * 
     * <p>
     * Garantiza que la evaluación siempre esté sincronizada con los datos actuales.
     * </p>
     */
    @PrePersist
    @PreUpdate
    private void preGuardarActualizar() {
        setEvaluacion(calcularEvaluacion());
    }
}
