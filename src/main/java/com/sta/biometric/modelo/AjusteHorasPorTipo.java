package com.sta.biometric.modelo;

import java.math.BigDecimal;

import org.openxava.annotations.*;

import com.sta.biometric.enums.Signo;

import lombok.*;

/**
 * Modelo transitorio para el diálogo de ajuste de un tipo específico de hora.
 * 
 * Muestra campos readonly para visualización y campos editables para el ajuste.
 */
@Getter
@Setter
@View(members = "tipoHora; valorHora; horasRegistradas; Ajuste [signo, ajuste]; motivo")
public class AjusteHorasPorTipo {

    /**
     * Tipo de hora: "⏰ Normales", "⏰+ Extras", "⭐ Especiales"
     */
    @ReadOnly
    private String tipoHora;

    /**
     * Valor hora (snapshot histórico)
     */
    @ReadOnly
    @Money
    private BigDecimal valorHora;

    /**
     * Horas registradas antes del ajuste (formato HH:MM)
     */
    @ReadOnly
    private String horasRegistradas;

    /**
     * Signo del ajuste (sumar/restar)
     */
    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(3)
    private Signo signo = Signo.MAS;

    /**
     * Valor del ajuste en formato HH:MM
     */
    @Mask("00:00")
    @LabelFormat(LabelFormatType.NO_LABEL)
    @DisplaySize(6)
    private String ajuste = "00:00";

    /**
     * Motivo del ajuste (obligatorio)
     */
    @Stereotype("MEMO")
    @Required
    private String motivo;

    // ==================================================================================
    // MÉTODOS UTILITARIOS
    // ==================================================================================

    /**
     * Convierte ajuste a minutos (con signo).
     */
    public int getAjusteMinutos() {
        int minutos = parsearHHMM(ajuste);
        return signo.getMultiplicador() * minutos;
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
     * Establece el ajuste desde minutos (con signo).
     */
    public void setAjusteDesdeMinutos(int minutosConSigno) {
        if (minutosConSigno < 0) {
            signo = Signo.MENOS;
            minutosConSigno = -minutosConSigno;
        } else {
            signo = Signo.MAS;
        }
        ajuste = formatearMinutos(minutosConSigno);
    }

    private String formatearMinutos(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%02d:%02d", h, m);
    }
}
