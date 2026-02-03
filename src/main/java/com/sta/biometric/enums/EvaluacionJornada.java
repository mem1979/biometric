package com.sta.biometric.enums;

/**
 * Resultado final de la evaluación de una jornada de asistencia.
 * Representa el estado global de toda la jornada diaria.
 */
public enum EvaluacionJornada {

    PENDIENTE("Pendiente de procesamiento"),
    EN_CURSO("Jornada en curso"),
    COMPLETA("Jornada completa"),
    INCOMPLETA("Jornada incompleta (faltan horas)"),
    AUSENTE("Ausente sin aviso"),
    SIN_ENTRADA("Falta registro de entrada"),
    SIN_SALIDA("Falta registro de salida"),
    LICENCIA("Licencia justificada"),
    LICENCIA_SIN_GOCE("Licencia sin goce de sueldo"),
    LICENCIA_NO_JUSTIFICADA("Licencia no justificada"),
    LICENCIA_PARCIAL("Licencia parcial con fichajes"),
    FERIADO("Día feriado"),
    FERIADO_TRABAJADO("Feriado trabajado"),
    DIA_NO_LABORAL("Día no laboral"),
    DIA_NO_LABORAL_TRABAJADO("Día no laboral trabajado"),
    SIN_TURNO_ASIGNADO("Sin turno asignado"),
    SIN_DATOS("Sin datos de asistencia");

    private final String descripcion;

    EvaluacionJornada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
