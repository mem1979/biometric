package com.sta.biometric.modelo;

import org.openxava.annotations.*;
import com.sta.biometric.formateadores.TiempoUtils;
import lombok.*;

/**
 * Modelo transitorio para el diálogo de ajuste manual de horas.
 */
@Getter
@Setter
@View(members = "minutosNormalesBase, minutosExtrasBase, minutosEspecialesBase;" +
        "Resumen_Actual {" +
        "  resultadoNormales; resultadoExtras; resultadoEspeciales" +
        "}" +
        "Ajustes_a_Aplicar {" +
        "  ajusteNormales; " +
        "  ajusteExtras; " +
        "  ajusteEspeciales" +
        "}" +
        "Motivo_Del_Ajuste {" +
        "  motivo" +
        "}")
public class AjusteHorasManual {

    @Hidden
    private int minutosNormalesBase;

    @Hidden
    private int minutosExtrasBase;

    @Hidden
    private int minutosEspecialesBase;

    @Label
    @OnChange(RecalcularPreviewAction.class)
    private int ajusteNormales;

    @Label
    @OnChange(RecalcularPreviewAction.class)
    private int ajusteExtras;

    @Label
    @OnChange(RecalcularPreviewAction.class)
    private int ajusteEspeciales;

    @Stereotype("MEMO")
    @Required
    private String motivo;

    // ==================================================================================
    // PREVIEWS CALCULADOS
    // ==================================================================================

    @Label
    @Depends("minutosNormalesBase, ajusteNormales")
    public String getResultadoNormales() {
        int total = Math.max(0, minutosNormalesBase + ajusteNormales);
        return TiempoUtils.formatearMinutosComoHHMM(minutosNormalesBase) + "  ➜  " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

    @Label
    @Depends("minutosExtrasBase, ajusteExtras")
    public String getResultadoExtras() {
        int total = Math.max(0, minutosExtrasBase + ajusteExtras);
        return TiempoUtils.formatearMinutosComoHHMM(minutosExtrasBase) + "  ➜  " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

    @Label
    @Depends("minutosEspecialesBase, ajusteEspeciales")
    public String getResultadoEspeciales() {
        int total = Math.max(0, minutosEspecialesBase + ajusteEspeciales);
        return TiempoUtils.formatearMinutosComoHHMM(minutosEspecialesBase) + "  ➜  " +
                TiempoUtils.formatearMinutosComoHHMM(total);
    }

    // Clase de acción vacía solo para disparar el refresco de la vista
    public static class RecalcularPreviewAction extends org.openxava.actions.OnChangePropertyBaseAction {
        public void execute() throws Exception {
            // No hace nada, solo refresca la vista al ejecutarse
        }
    }
}
