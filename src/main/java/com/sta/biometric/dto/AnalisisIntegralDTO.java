package com.sta.biometric.dto;

import lombok.*;

/**
 * DTO que contiene las 4 secciones del análisis integral de desempeño
 * generado por IA (Gemini) o por el sistema experto local (fallback).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisIntegralDTO {

    /**
     * Resumen ejecutivo del desempeño anual.
     * Formato: 2 párrafos formales con tono corporativo.
     */
    private String resumenEjecutivo;

    /**
     * Lista de fortalezas identificadas.
     * Formato: HTML con viñetas (• punto1<br>
     * • punto2...)
     */
    private String fortalezas;

    /**
     * Lista de áreas de mejora/debilidades.
     * Formato: HTML con viñetas (• punto1<br>
     * • punto2...)
     */
    private String debilidades;

    /**
     * Recomendaciones y acciones concretas.
     * Formato: HTML con viñetas o lista numerada.
     */
    private String recomendaciones;

    /**
     * Indica si el análisis fue generado por IA (true) o fallback local (false).
     */
    private boolean generadoPorIA;

    /**
     * Mensaje de estado/error para debugging (opcional).
     */
    private String mensajeEstado;
}
