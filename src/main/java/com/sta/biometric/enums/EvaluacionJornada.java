package com.sta.biometric.enums;
/**
 * Resultado final de la evaluación de una jornada de asistencia.
 * Representa el estado global de toda la jornada diaria.
 */
public enum EvaluacionJornada {

    PENDIENTE,
    EN_CURSO,
    COMPLETA,
    INCOMPLETA,
    AUSENTE,
    LICENCIA,
    FERIADO,
    FERIADO_TRABAJADO,
    DIA_NO_LABORAL,
    SIN_TURNO_ASIGNADO,
    SIN_DATOS;

}
