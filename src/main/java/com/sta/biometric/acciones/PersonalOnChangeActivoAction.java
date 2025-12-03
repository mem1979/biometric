package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

public class PersonalOnChangeActivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean activo = (Boolean) getNewValue();
        if (activo == null)
            return;

        if (!activo) {
            getView().setLabelId("activo", "🔒 DESABILITADO");// Candado cerrado
            addWarning("El empleado fue marcado como INACTIVO.");
        } else {
            getView().setLabelId("activo", "🔓 HABILITADO");// Candado cerrado
            addInfo("El empleado fue marcado como ACTIVO.");
        }

        MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());
        addMessage("Datos guardados correctamente.");
    }
}
