package com.sta.biometric.modelo.rrhh.enums;

/**
 * Clasificación fiscal y lógica de los conceptos del recibo.
 */
public enum TipoConcepto {

    /**
     * Sumas sujetas a aportes y contribuciones (Sueldo Básico, Horas Extras,
     * Adicionales).
     * - Forman parte del "Bruto" para cálculo de aguinaldo (SAC) y vacaciones.
     * - Base imponible para Jubilación, Obra Social, etc.
     */
    REMUNERATIVO,

    /**
     * Sumas NO sujetas a descuentos de seguridad social (Viáticos, Asignaciones no
     * rem, etc.).
     * - Se pagan al empleado "de bolsillo" directo (salvo excepciones normativas).
     */
    NO_REMUNERATIVO,

    /**
     * Descuentos al empleado (Aportes Seg. Social, Sindicato, Seguro de Vida).
     * - Restan del total a cobrar.
     */
    DEDUCCION,

    /**
     * Retenciones Impositivas (Impuesto a las Ganancias Cuarta Categoría, IIBB).
     * - Específico para Locación de Servicios o Altos Ingresos LCT.
     */
    RETENCION_IMPOSITIVA,

    /**
     * Pagos en especie o beneficios que no mueven dinero pero figuran en recibo.
     * - Ej: Comedor en planta, Celular corporativo (según ley).
     */
    PAGO_EN_ESPECIE,

    /**
     * Conceptos auxiliares de cálculo intermedio.
     * - No se imprimen en el recibo.
     * - Ej: "Base Cálculo Antigüedad".
     */
    AUXILIAR_CALCULO
}
