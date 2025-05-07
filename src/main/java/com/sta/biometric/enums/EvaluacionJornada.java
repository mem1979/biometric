package com.sta.biometric.enums;
/**
 * Resultado final de la evaluación de una jornada de asistencia.
 * Representa el estado global de toda la jornada diaria.
 */
public enum EvaluacionJornada {

    PENDIENTE("PENDIENTE DE INICIO"),
    EN_CURSO("TURNO EN CURSO"),
    COMPLETA("TURNO COMPLETO"),
    INCOMPLETA("TURNO INCOMPLETO"),
    AUSENTE("AUSENTE"),
    LICENCIA_JUSTIFICADA("LICENCIA"),
    FERIADO("FERIADO"),
    FERIADO_TRABAJADO("FERIADO TRABAJADO"),
    DIA_NO_LABORAL("Día No Laboral"),
    SIN_TURNO_ASIGNADO("Sin Turno Asignado"),
    SIN_DATOS("SIN DATOS");

    private final String descripcion;

    EvaluacionJornada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
