package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Niveles jerárquicos universales para la estructura organizacional.
 * 
 * <p>
 * Diseñado para ser compatible con la mayoría de empresas y rubros,
 * desde pequeñas empresas hasta corporaciones multinacionales.
 * </p>
 * 
 * <p>
 * El campo {@code orden} permite ordenar de mayor a menor jerarquía,
 * y {@code path} facilita queries jerárquicas para futuros reportes.
 * </p>
 * 
 * @author Sistema STARH
 * @version 1.0
 * @see com.sta.biometric.modelo.ContratoLaboral
 */
@Getter
@RequiredArgsConstructor
public enum NivelJerarquico {

    // =========================================================================
    // NIVEL EJECUTIVO (Alta Dirección)
    // =========================================================================

    PRESIDENTE("Presidente / CEO", 1, "/PRES",
            "Máxima autoridad ejecutiva de la organización"),

    VICEPRESIDENTE("Vicepresidente", 2, "/PRES/VP",
            "Segunda autoridad ejecutiva, reporta al Presidente"),

    DIRECTOR_GENERAL("Director General", 3, "/PRES/DG",
            "Director máximo de operaciones o división principal"),

    // =========================================================================
    // NIVEL DIRECTIVO (Direcciones de área)
    // =========================================================================

    DIRECTOR("Director", 4, "/PRES/DG/DIR",
            "Director de departamento o área funcional (Finanzas, RRHH, Comercial, etc.)"),

    SUBDIRECTOR("Subdirector", 5, "/PRES/DG/DIR/SDIR",
            "Asiste al Director y lo reemplaza en su ausencia"),

    // =========================================================================
    // NIVEL GERENCIAL (Gestión de áreas)
    // =========================================================================

    GERENTE("Gerente", 6, "/PRES/DG/DIR/GER",
            "Gestiona un área específica con autonomía operativa"),

    SUBGERENTE("Subgerente", 7, "/PRES/DG/DIR/GER/SGER",
            "Asiste al Gerente y coordina equipos"),

    // =========================================================================
    // NIVEL DE JEFATURA (Supervisión de equipos)
    // =========================================================================

    JEFE("Jefe", 8, "/PRES/DG/DIR/GER/JEF",
            "Jefe de sector o unidad con personal a cargo"),

    SUBJEFE("Subjefe", 9, "/PRES/DG/DIR/GER/JEF/SJEF",
            "Asiste al Jefe en la coordinación del equipo"),

    // =========================================================================
    // NIVEL DE COORDINACIÓN (Liderazgo de equipos pequeños)
    // =========================================================================

    COORDINADOR("Coordinador", 10, "/PRES/DG/DIR/GER/JEF/COORD",
            "Coordina actividades y pequeños equipos de trabajo"),

    SUPERVISOR("Supervisor", 11, "/PRES/DG/DIR/GER/JEF/COORD/SUP",
            "Supervisa tareas operativas y control de calidad"),

    LIDER("Líder de Equipo", 12, "/PRES/DG/DIR/GER/JEF/COORD/LID",
            "Lidera un equipo específico sin jerarquía formal"),

    // =========================================================================
    // NIVEL PROFESIONAL (Especialistas y expertos)
    // =========================================================================

    ESPECIALISTA("Especialista", 13, "/PRES/DG/DIR/GER/ESP",
            "Experto en un área específica, sin personal a cargo"),

    PROFESIONAL_SENIOR("Profesional Senior", 14, "/PRES/DG/DIR/GER/ESP/PSEN",
            "Profesional con amplia experiencia (5+ años)"),

    PROFESIONAL_SEMI_SENIOR("Profesional Semi-Senior", 15, "/PRES/DG/DIR/GER/ESP/PSSEN",
            "Profesional con experiencia intermedia (2-5 años)"),

    PROFESIONAL_JUNIOR("Profesional Junior", 16, "/PRES/DG/DIR/GER/ESP/PJUN",
            "Profesional en desarrollo inicial (0-2 años)"),

    // =========================================================================
    // NIVEL TÉCNICO (Conocimientos técnicos específicos)
    // =========================================================================

    TECNICO_SENIOR("Técnico Senior", 17, "/PRES/DG/DIR/GER/TEC/TSEN",
            "Técnico con experiencia avanzada"),

    TECNICO("Técnico", 18, "/PRES/DG/DIR/GER/TEC",
            "Ejecuta tareas técnicas especializadas"),

    TECNICO_JUNIOR("Técnico Junior", 19, "/PRES/DG/DIR/GER/TEC/TJUN",
            "Técnico en formación o con poca experiencia"),

    // =========================================================================
    // NIVEL ADMINISTRATIVO (Apoyo administrativo)
    // =========================================================================

    ADMINISTRATIVO_SENIOR("Administrativo Senior", 20, "/PRES/DG/DIR/GER/ADM/ASEN",
            "Personal administrativo con experiencia"),

    ADMINISTRATIVO("Administrativo", 21, "/PRES/DG/DIR/GER/ADM",
            "Personal de oficina y gestión documental"),

    AUXILIAR_ADMINISTRATIVO("Auxiliar Administrativo", 22, "/PRES/DG/DIR/GER/ADM/AUX",
            "Apoyo en tareas administrativas básicas"),

    // =========================================================================
    // NIVEL OPERATIVO (Ejecución de tareas)
    // =========================================================================

    OPERARIO_SENIOR("Operario Senior", 23, "/PRES/DG/DIR/GER/OPE/OSEN",
            "Operario con experiencia y habilidades avanzadas"),

    OPERARIO("Operario", 24, "/PRES/DG/DIR/GER/OPE",
            "Ejecuta tareas operativas o de producción"),

    AYUDANTE("Ayudante", 25, "/PRES/DG/DIR/GER/OPE/AYU",
            "Asiste en tareas operativas"),

    // =========================================================================
    // NIVEL DE INGRESO (Formación inicial)
    // =========================================================================

    APRENDIZ("Aprendiz", 26, "/PRES/DG/DIR/GER/APR",
            "Personal en formación o capacitación"),

    PASANTE("Pasante", 27, "/PRES/DG/DIR/GER/PAS",
            "Estudiante realizando pasantía o práctica profesional"),

    BECARIO("Becario", 28, "/PRES/DG/DIR/GER/BEC",
            "Persona con beca de formación laboral");

    // =========================================================================
    // CAMPOS
    // =========================================================================

    private final String nombre;
    private final int orden;
    private final String path;
    private final String descripcion;

    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Retorna el nombre formateado con el nivel (para UI).
     * Ejemplo: "6. Gerente"
     */
    public String getNombreConOrden() {
        return orden + ". " + nombre;
    }

    /**
     * Retorna la profundidad jerárquica basada en el path.
     * Más barras = mayor profundidad = menor jerarquía.
     */
    public int getProfundidad() {
        return (int) path.chars().filter(c -> c == '/').count();
    }

    /**
     * Verifica si este nivel es superior a otro.
     */
    public boolean esSuperiorA(NivelJerarquico otro) {
        return this.orden < otro.orden;
    }

    /**
     * Verifica si este nivel es de categoría ejecutiva.
     */
    public boolean esEjecutivo() {
        return orden <= 3;
    }

    /**
     * Verifica si este nivel es de dirección o gerencia.
     */
    public boolean esDireccionOGerencia() {
        return orden >= 4 && orden <= 7;
    }

    /**
     * Verifica si tiene personal a cargo típicamente.
     */
    public boolean tienePersonalACargo() {
        return orden <= 12;
    }
}
