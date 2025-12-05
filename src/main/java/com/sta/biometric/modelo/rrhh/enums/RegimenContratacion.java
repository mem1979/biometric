package com.sta.biometric.modelo.rrhh.enums;

/**
 * Define el régimen legal bajo el cual se contrata al personal.
 * Fundamental para determinar qué impuestos, retenciones y descuentos aplican.
 */
public enum RegimenContratacion {

    /**
     * Ley de Contrato de Trabajo (20.744).
     * - Aplica recibo de sueldo oficial.
     * - Aplica retenciones de seguridad social (Jubilación, PAMI, Obra Social).
     * - Aplica ART y Seguro de Vida obligatorios.
     */
    RELACION_DEPENDENCIA,

    /**
     * Trabajo no registrado o "Informal".
     * - Gestión interna de pagos.
     * - No genera obligaciones fiscales automáticas en el sistema (salvo config
     * interna).
     * - Genera documento tipo "Recibo X".
     */
    INFORMAL,

    /**
     * Locación de Servicios (Facturación).
     * - El prestador emite factura (Monotributo/RI).
     * - Aplican Retenciones de Ganancias / IIBB según corresponda.
     * - Genera "Orden de Pago".
     */
    LOCACION_SERVICIOS,

    /**
     * Pasantías Educativas (Ley 26.427).
     * - Asignación estímulo (no remunerativa).
     * - Cargas sociales reducidas (solo Obra Social y ART).
     */
    PASANTIA,

    /**
     * Contrato eventual o temporario.
     * - Variantes de LCT con plazos definidos.
     */
    EVENTUAL
}
