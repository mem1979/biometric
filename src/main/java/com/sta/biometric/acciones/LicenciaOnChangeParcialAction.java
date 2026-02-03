package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción que se ejecuta cuando cambia el valor de esParcial.
 * Habilita o deshabilita la edición de horaInicio y horaFin.
 */
public class LicenciaOnChangeParcialAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean esParcial = (Boolean) getNewValue();

        if (esParcial != null && esParcial) {
            // Habilitar edición de horas
            getView().setEditable("horaInicio", true);
            getView().setEditable("horaFin", true);
        } else {
            // Deshabilitar edición de horas y limpiar valores
            getView().setEditable("horaInicio", false);
            getView().setEditable("horaFin", false);
            getView().setValue("horaInicio", null);
            getView().setValue("horaFin", null);
        }
    }
}
