package com.sta.biometric.dto;

import java.util.*;

import lombok.*;

/**
 * DTO que encapsula todos los datos crudos necesarios para el análisis de
 * desempeño.
 * Se usa como input para AnalisisDesempenoService.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosDesempenoDTO {

    // =================== DATOS DEL EMPLEADO ===================
    private String nombreEmpleado;
    private String puesto;
    private String sucursal;
    private int anio;

    // =================== MÉTRICAS DE ASISTENCIA ===================
    private double porcentajePresentismo;
    private long diasPresentes;
    private long diasLaborables;

    // =================== TARDANZAS ===================
    private int totalTardanzas;
    private int minutosTotalesTardanza;
    private double promedioMinutosTardanza;

    // =================== AUSENCIAS ===================
    private long totalAusencias;
    private long ausenciasInjustificadas;

    // =================== HORAS TRABAJADAS ===================
    private int minutosNormales;
    private int minutosExtras;
    private int minutosEspeciales;

    // =================== LICENCIAS ===================
    private int totalLicencias;
    private int diasLicenciaMedica;
    private int diasVacaciones;
    private int totalDiasLicencia;

    // =================== AUDITORÍAS ===================
    private int totalCorrecciones;

    // =================== NOTAS DE DESEMPEÑO ===================
    private List<NotaResumenDTO> notasDesempeno;
    private double promedioCalificacionNotas;
    private String evaluacionNotas;

    /**
     * Subclase para resumir notas de desempeño.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotaResumenDTO {
        private String contenido;
        private String calificacion;
        private String autor;
        private String fecha;
    }

    /**
     * Construye un string formateado con las notas para incluir en el prompt.
     */
    public String getNotasFormateadas() {
        if (notasDesempeno == null || notasDesempeno.isEmpty()) {
            return "No hay notas de desempeño registradas para este período.";
        }

        StringBuilder sb = new StringBuilder();
        for (NotaResumenDTO nota : notasDesempeno) {
            sb.append("- [").append(nota.getCalificacion()).append("] ")
                    .append(nota.getContenido());
            if (nota.getAutor() != null && !nota.getAutor().isEmpty()) {
                sb.append(" (por ").append(nota.getAutor()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Formatea horas desde minutos.
     */
    public String getHorasNormalesFormateadas() {
        return formatearMinutos(minutosNormales);
    }

    public String getHorasExtrasFormateadas() {
        return formatearMinutos(minutosExtras);
    }

    public String getHorasEspecialesFormateadas() {
        return formatearMinutos(minutosEspeciales);
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }
}
