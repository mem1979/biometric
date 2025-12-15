package com.sta.biometric.modelo;

import javax.persistence.Transient;
import org.openxava.annotations.*;
import com.sta.biometric.formateadores.TiempoUtils;
import lombok.*;

/**
 * Modelo transitorio para el diálogo de ajuste manual de horas.
 * Usa formato HH:MM para entrada de usuario.
 */
@Getter
@Setter
@View(members = "Horas_Base { horasNormalesBase; horasExtrasBase; horasEspecialesBase };" +
        "Ajustes_a_Aplicar {" +
        "  ajusteNormales; " +
        "  ajusteExtras; " +
        "  ajusteEspeciales" +
        "};" +
        "Vista_Previa { resultadoNormales; resultadoExtras; resultadoEspeciales };" +
        "Motivo_Del_Ajuste { motivo }")
public class AjusteHorasManual {

    // ==================================================================================
    // VALORES BASE (SOLO LECTURA - MOSTRADOS AL USUARIO)
    // ==================================================================================

    @Hidden
    private int minutosNormalesBase;

    @Hidden
    private int minutosExtrasBase;

    @Hidden
    private int minutosEspecialesBase;

    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    public String getHorasNormalesBase() {
        return TiempoUtils.formatearMinutosComoHHMM(minutosNormalesBase);
    }

    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    public String getHorasExtrasBase() {
        return TiempoUtils.formatearMinutosComoHHMM(minutosExtrasBase);
    }

    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    public String getHorasEspecialesBase() {
        return TiempoUtils.formatearMinutosComoHHMM(minutosEspecialesBase);
    }

    // ==================================================================================
    // AJUSTES EN FORMATO HH:MM (PUEDEN SER NEGATIVOS: -01:30)
    // ==================================================================================

    @Stereotype("TIEMPO_AJUSTE")
    @OnChange(RecalcularPreviewAction.class)
    private String ajusteNormales = "00:00";

    @Stereotype("TIEMPO_AJUSTE")
    @OnChange(RecalcularPreviewAction.class)
    private String ajusteExtras = "00:00";

    @Stereotype("TIEMPO_AJUSTE")
    @OnChange(RecalcularPreviewAction.class)
    private String ajusteEspeciales = "00:00";

    @Stereotype("MEMO")
    @Required
    private String motivo;

    // ==================================================================================
    // PREVIEWS CALCULADOS
    // ==================================================================================

    @Transient
    @ReadOnly
    @Depends("minutosNormalesBase, ajusteNormales")
    public String getResultadoNormales() {
        int ajusteMin = TiempoUtils.parsearHHMMaMinutos(ajusteNormales);
        int total = Math.max(0, minutosNormalesBase + ajusteMin);
        String signo = ajusteMin >= 0 ? "+" : "";
        return TiempoUtils.formatearMinutosComoHHMM(minutosNormalesBase) +
                " (" + signo + ajusteNormales + ") = " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

    @Transient
    @ReadOnly
    @Depends("minutosExtrasBase, ajusteExtras")
    public String getResultadoExtras() {
        int ajusteMin = TiempoUtils.parsearHHMMaMinutos(ajusteExtras);
        int total = Math.max(0, minutosExtrasBase + ajusteMin);
        String signo = ajusteMin >= 0 ? "+" : "";
        return TiempoUtils.formatearMinutosComoHHMM(minutosExtrasBase) +
                " (" + signo + ajusteExtras + ") = " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

    @Transient
    @ReadOnly
    @Depends("minutosEspecialesBase, ajusteEspeciales")
    public String getResultadoEspeciales() {
        int ajusteMin = TiempoUtils.parsearHHMMaMinutos(ajusteEspeciales);
        int total = Math.max(0, minutosEspecialesBase + ajusteMin);
        String signo = ajusteMin >= 0 ? "+" : "";
        return TiempoUtils.formatearMinutosComoHHMM(minutosEspecialesBase) +
                " (" + signo + ajusteEspeciales + ") = " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

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

    // Clase de acción para disparar el refresco de la vista
    public static class RecalcularPreviewAction extends org.openxava.actions.OnChangePropertyBaseAction {
        public void execute() throws Exception {
            // Solo refresca la vista al ejecutarse
        }
    }
}
