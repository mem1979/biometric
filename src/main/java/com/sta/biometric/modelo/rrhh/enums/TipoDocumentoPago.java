package com.sta.biometric.modelo.rrhh.enums;

/**
 * Define el tipo de documento que se genera con la liquidación.
 */
public enum TipoDocumentoPago {

    /**
     * Recibo de Sueldo oficial (Ley 20.744).
     * - Firma por duplicado.
     * - Validez legal para aportes.
     */
    RECIBO_LEY,

    /**
     * Documento interno de pago ("Recibo X").
     * - Para pagos informales o adelantos en efectivo no registrados.
     */
    RECIBO_INTERNO,

    /**
     * Orden de Pago (OP).
     * - Para prestadores de servicios que facturan.
     * - Detalla honorarios y retenciones.
     */
    ORDEN_PAGO
}
