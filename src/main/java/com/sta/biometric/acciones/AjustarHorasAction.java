package com.sta.biometric.acciones;

import org.openxava.actions.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

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

        // Calcular bases (sin ajustes)
        int minutosNormalesBase = 0;
        if (reg.getMinutosTrabajados() > 0 && reg.getMinutosEsperados() > 0) {
            minutosNormalesBase = Math.min(reg.getMinutosTrabajados(), reg.getMinutosEsperados());
        }

        // Corrección: Si es jornada especial, las normales base son 0
        boolean esEspecial = isJornadaEspecial(reg);
        if (esEspecial) {
            minutosNormalesBase = 0;
        }

        int minutosExtrasBase = reg.getMinutosExtras();
        int minutosEspecialesBase = esEspecial ? reg.getMinutosTrabajados() : 0;

        // Setear valores base (ocultos, en minutos para cálculos)
        getView().setValue("minutosNormalesBase", minutosNormalesBase);
        getView().setValue("minutosExtrasBase", minutosExtrasBase);
        getView().setValue("minutosEspecialesBase", minutosEspecialesBase);

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

    /**
     * Verifica si la jornada es especial (solo feriados trabajados).
     */
    private boolean isJornadaEspecial(AuditoriaRegistros reg) {
        if (reg.getEvaluacion() == null)
            return false;
        return reg.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO;
    }
}
