package com.sta.biometric.enums;

/**
 * Estado del período de liquidación de jornadas.
 * 
 * <p>
 * Controla el ciclo de vida de una liquidación:
 * </p>
 * <ul>
 * <li><b>ABIERTO:</b> Período en curso, puede recibir nuevos registros</li>
 * <li><b>CERRADO:</b> Período finalizado, valores definitivos</li>
 * <li><b>RECALCULADO:</b> Fue recalculado manualmente después de cerrado</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public enum EstadoLiquidacion {

    /** Período abierto, puede recibir nuevos registros de AuditoriaRegistros */
    ABIERTO,

    /** Período cerrado, valores definitivos para nómina */
    CERRADO,

    /** Fue recalculado manualmente después del cierre */
    RECALCULADO
}
