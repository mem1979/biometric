package com.sta.biometric.acciones;

import org.openxava.actions.*;

import com.sta.biometric.enums.Signo;
import com.sta.biometric.modelo.*;

/**
 * Acción que abre el diálogo de ajuste manual de horas.
 * Inicializa los ajustes actuales del registro para edición.
 */
public class AjustarHorasAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        AuditoriaRegistros reg = (AuditoriaRegistros) getView().getEntity();

        if (reg == null) {
            addError("No se pudo obtener el registro.");
            return;
        }

        showDialog();
        getView().setTitle("⚙️ Ajustar Horas Manualmente");
        getView().setModelName("AjusteHorasManual");

        // Inicializar signos y valores según ajustes actuales
        setSignoYAjuste("Normales", reg.getAjusteMinutosNormales());
        setSignoYAjuste("Extras", reg.getAjusteMinutosExtras());
        setSignoYAjuste("Especiales", reg.getAjusteMinutosEspeciales());

        // Motivo vacío
        getView().setValue("motivo", "");

        setControllers("AjusteHoras");
    }

    /**
     * Establece el signo y el valor de ajuste para un tipo.
     * Por defecto siempre MAS (sumar).
     */
    private void setSignoYAjuste(String tipo, int minutos) {
        Signo signo = Signo.MAS;
        int absMinutos = Math.abs(minutos);

        if (minutos < 0) {
            signo = Signo.MENOS;
        }

        getView().setValue("signo" + tipo, signo);
        getView().setValue("ajuste" + tipo, formatearMinutos(absMinutos));
    }

    private String formatearMinutos(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%02d:%02d", h, m);
    }
}
