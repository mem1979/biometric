package com.sta.biometric.modelo;

import org.openxava.annotations.*;
import com.sta.biometric.enums.Signo;
import lombok.*;

/**
 * Modelo transitorio para el diálogo de ajuste manual de horas.
 * 
 * Usa @Mask para formato HH:MM con selectores de signo (+/-) para cada tipo.
 */
@Getter
@Setter
@View(members = "Normales [ signoNormales, ajusteNormales ];" +
        "Extras [ signoExtras, ajusteExtras ];" +
        "Especiales [ signoEspeciales, ajusteEspeciales ];" +
        "motivo")
public class AjusteHorasManual {

    // ==================================================================================
    // AJUSTE NORMALES
    // ==================================================================================

    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(3)
    private Signo signoNormales = Signo.MAS;

    @Mask("00:00")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(6)
    private String ajusteNormales = "00:00";

    // ==================================================================================
    // AJUSTE EXTRAS
    // ==================================================================================

    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(3)
    private Signo signoExtras = Signo.MAS;

    @Mask("00:00")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(6)
    private String ajusteExtras = "00:00";

    // ==================================================================================
    // AJUSTE ESPECIALES
    // ==================================================================================

    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(3)
    private Signo signoEspeciales = Signo.MAS;

    @Mask("00:00")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(6)
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
     * Convierte ajuste normales a minutos (con signo).
     */
    public int getAjusteNormalesMinutos() {
        int minutos = parsearHHMM(ajusteNormales);
        return signoNormales.getMultiplicador() * minutos;
    }

    public int getAjusteExtrasMinutos() {
        int minutos = parsearHHMM(ajusteExtras);
        return signoExtras.getMultiplicador() * minutos;
    }

    public int getAjusteEspecialesMinutos() {
        int minutos = parsearHHMM(ajusteEspeciales);
        return signoEspeciales.getMultiplicador() * minutos;
    }

    /**
     * Parsea HH:MM a minutos totales (siempre positivo).
     */
    private int parsearHHMM(String valor) {
        if (valor == null || valor.isBlank())
            return 0;
        String limpio = valor.replace("_", "0").trim();
        String[] partes = limpio.split(":");
        try {
            int horas = partes.length >= 1 ? Integer.parseInt(partes[0].trim()) : 0;
            int minutos = partes.length >= 2 ? Integer.parseInt(partes[1].trim()) : 0;
            return horas * 60 + minutos;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Establece el ajuste de normales desde minutos (con signo).
     */
    public void setAjusteNormalesDesdeMinutos(int minutosConSigno) {
        if (minutosConSigno < 0) {
            signoNormales = Signo.MENOS;
            minutosConSigno = -minutosConSigno;
        } else {
            signoNormales = Signo.MAS;
        }
        ajusteNormales = formatearMinutos(minutosConSigno);
    }

    public void setAjusteExtrasDesdeMinutos(int minutosConSigno) {
        if (minutosConSigno < 0) {
            signoExtras = Signo.MENOS;
            minutosConSigno = -minutosConSigno;
        } else {
            signoExtras = Signo.MAS;
        }
        ajusteExtras = formatearMinutos(minutosConSigno);
    }

    public void setAjusteEspecialesDesdeMinutos(int minutosConSigno) {
        if (minutosConSigno < 0) {
            signoEspeciales = Signo.MENOS;
            minutosConSigno = -minutosConSigno;
        } else {
            signoEspeciales = Signo.MAS;
        }
        ajusteEspeciales = formatearMinutos(minutosConSigno);
    }

    private String formatearMinutos(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%02d:%02d", h, m);
    }
}
