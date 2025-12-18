package com.sta.biometric.acciones;

import org.openxava.actions.*;

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

        // Convertir ajustes actuales de minutos a formato HH:MM
        getView().setValue("ajusteNormales", formatearAjuste(reg.getAjusteMinutosNormales()));
        getView().setValue("ajusteExtras", formatearAjuste(reg.getAjusteMinutosExtras()));
        getView().setValue("ajusteEspeciales", formatearAjuste(reg.getAjusteMinutosEspeciales()));

        // Motivo vacío
        getView().setValue("motivo", "");

        setControllers("AjusteHoras");
    }

    /**
     * Formatea minutos como HH:MM (soporta negativos como -01:30)
     */
    private String formatearAjuste(int minutos) {
        if (minutos == 0)
            return "00:00";

        boolean negativo = minutos < 0;
        int absMinutos = Math.abs(minutos);
        int horas = absMinutos / 60;
        int mins = absMinutos % 60;

        String formato = String.format("%02d:%02d", horas, mins);
        return negativo ? "-" + formato : formato;
    }
}
