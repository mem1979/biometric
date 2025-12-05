package com.sta.biometric.modelo.rrhh.enums;

/**
 * Define la frecuencia y unidad de medida para la liquidación.
 */
public enum ModalidadLiquidacion {

    /**
     * Empleados mensualizados (Comercio, UTEDYC, etc.).
     * - Cobran por mes calendario (30 días).
     * - Fichan horas pero su básico es fijo (salvo horas extras).
     */
    MENSUAL,

    /**
     * Empleados jornalizados (UOCRA, UOM, Maestranza).
     * - Se liquida típicamente por Quincena (1ra y 2da).
     * - Cobran estrictamente por horas/días trabajados.
     */
    QUINCENAL,

    /**
     * Pago semanal (común en ciertos sectores informales o construcción puntual).
     */
    SEMANAL,

    /**
     * Pago por día trabajado (Jornal puro).
     */
    DIARIO
}
