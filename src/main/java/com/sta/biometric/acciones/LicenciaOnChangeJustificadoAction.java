package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class LicenciaOnChangeJustificadoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        // getNewValue() devuelve el valor recién elegido en UI
        Boolean nuevoValor = (Boolean) getNewValue();

        // Solo mostramos el mensaje cuando pasa a NO justificado
        if (Boolean.FALSE.equals(nuevoValor)) {
            // Usa i18n; si preferís solo informativo usa addMessage(...)
            addWarning("licencia_no_justificada");
        }
        // Si es true o null, no hacemos nada.
    }
}