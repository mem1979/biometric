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
        } else if (reg.getMinutosTrabajados() > 0) {
            // Si no hay esperados pero hay trabajados (ej: dia no laboral trabajado sin
            // turno)
            // Depende de la lógica, pero asumimos que si no es especial, son normales hasta
            // el tope (que es 0 si no hay esperado)
            // Replicando lógica de AuditoriaRegistros:
            minutosNormalesBase = Math.min(reg.getMinutosTrabajados(), reg.getMinutosEsperados());
        }

        // Corrección: Si es jornada especial, las normales base son 0 (todo va a
        // especiales)
        // Pero necesitamos acceder a esJornadaEspecial() que es privado.
        // Usamos la evaluación.
        boolean esEspecial = isJornadaEspecial(reg);
        if (esEspecial) {
            minutosNormalesBase = 0;
        }

        int minutosExtrasBase = reg.getMinutosExtras();
        int minutosEspecialesBase = esEspecial ? reg.getMinutosTrabajados() : 0;

        // Setear valores base (ocultos)
        getView().setValue("minutosNormalesBase", minutosNormalesBase);
        getView().setValue("minutosExtrasBase", minutosExtrasBase);
        getView().setValue("minutosEspecialesBase", minutosEspecialesBase);

        // Setear ajustes actuales
        getView().setValue("ajusteNormales", reg.getAjusteMinutosNormales());
        getView().setValue("ajusteExtras", reg.getAjusteMinutosExtras());
        getView().setValue("ajusteEspeciales", reg.getAjusteMinutosEspeciales());

        // Setear motivo vacío (o podríamos traer el último si quisiéramos, pero mejor
        // pedir uno nuevo)
        getView().setValue("motivo", "");

        setControllers("AjusteHoras");
    }

    /**
     * Verifica si la jornada es especial (solo feriados trabajados).
     * Según LCT Art. 201, solo feriados aplican como horas especiales (extras
     * 100%).
     */
    private boolean isJornadaEspecial(AuditoriaRegistros reg) {
        if (reg.getEvaluacion() == null)
            return false;
        return reg.getEvaluacion() == EvaluacionJornada.FERIADO_TRABAJADO;
    }
}
