package com.sta.biometric.modelo;

import org.openxava.annotations.*;
import com.sta.biometric.formateadores.TiempoUtils;
import lombok.*;

/**
 * Modelo transitorio para el diálogo de ajuste manual de horas.
 * 
 * Solo permite ajustar el TIEMPO (+/- minutos) para cada tipo de hora.
 * Los valores monetarios son calculados automáticamente desde los snapshots
 * históricos.
 */
@Getter
@Setter
@View(members =
// Ajuste de tiempo en formato HH:MM (editable)
"ajusteNormales; ajusteExtras; ajusteEspeciales;" +
// Motivo obligatorio
        "motivo")
public class AjusteHorasManual {

    // ==================================================================================
    // AJUSTES DE TIEMPO EN FORMATO HH:MM (EDITABLES)
    // ==================================================================================

    @Stereotype("TIEMPO_AJUSTE")
    private String ajusteNormales = "00:00";

    @Stereotype("TIEMPO_AJUSTE")
    private String ajusteExtras = "00:00";

    @Stereotype("TIEMPO_AJUSTE")
    private String ajusteEspeciales = "00:00";

    // ==================================================================================
    // MOTIVO (OBLIGATORIO)
    // ==================================================================================

    @Stereotype("MEMO")
    @Required
    private String motivo;

    // ==================================================================================
    // MÉTODOS UTILITARIOS PARA CONVERSIÓN
    // ==================================================================================

    /**
     * Convierte el ajuste HH:MM a minutos (puede ser negativo).
     */
    public int getAjusteNormalesMinutos() {
        return TiempoUtils.parsearHHMMaMinutos(ajusteNormales);
    }

    public int getAjusteExtrasMinutos() {
        return TiempoUtils.parsearHHMMaMinutos(ajusteExtras);
    }

    public int getAjusteEspecialesMinutos() {
        return TiempoUtils.parsearHHMMaMinutos(ajusteEspeciales);
    }
}
