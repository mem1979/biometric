package com.sta.biometric.enums;
/**
 * Tipos de licencia laboral válidos según normativa nacional e interna.
 * Se almacena el nombre técnico en la base de datos (Ej: VACACIONES),
 * pero se presenta una descripción legible al usuario (Ej: "Vacaciones Anuales").
 */
public enum TipoLicenciaAR {

    VACACIONES("Vacaciones Anuales"),
    ENFERMEDAD("Enfermedad"),
    ACCIDENTE_TRABAJO("Accidente de Trabajo (ART)"),
    MATERNIDAD("Maternidad"),
    PATERNIDAD("Paternidad"),
    EXAMENES("Exámenes Académicos"),
    ESTUDIO("Licencia por Estudio"),
    MATRIMONIO("Matrimonio"),
    FALLECIMIENTO_FAMILIAR("Fallecimiento de Familiar"),
    MUDANZA("Mudanza"),
    DONACION_SANGRE("Donación de Sangre"),
    CITACION_JUDICIAL("Citaciones Judiciales"),
    DEBER_CIVICO("Deber Cívico (Elecciones)"),
    ASUNTOS_PERSONALES("Asuntos Personales"),
    LICENCIA_SINDICAL("Licencia Sindical"),
    LICENCIA_SIN_GOCE("Sin Goce de Sueldo"),
    LICENCIA_EXTRAORDINARIA("Licencia Extraordinaria"),
    LICENCIA_ADM_CON_GOCE("Administrativa con Goce"),
    LICENCIA_ADM_SIN_GOCE("Administrativa sin Goce"),
    EMBARAZO_RIESGO("Embarazo de Riesgo"),
    NACIMIENTO_HIJO_ADOPCION("Nacimiento o Adopción de Hijo"),
    VIOLENCIA_GENERO("Violencia de Género"),
    CUIDADO_FAMILIAR("Cuidado de Familiar"),
    TELETRABAJO_ESPECIAL("Teletrabajo Especial"),
    LICENCIA_ESPECIAL_PROFESIONAL("Licencia Profesional (Docentes, Médicos)"),
    OTRA("Otra Justificación");

    private final String descripcion;

    TipoLicenciaAR(String descripcion) {
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