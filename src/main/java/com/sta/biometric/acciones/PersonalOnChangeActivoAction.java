package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

public class PersonalOnChangeActivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean activo = (Boolean) getNewValue();
        if (activo == null)
            return;

        // Actualizar etiqueta siempre
        if (!activo) {
            getView().setLabelId("activo", "🔒 DESABILITADO");
        } else {
            getView().setLabelId("activo", "🔓 HABILITADO");
        }

        // Solo mostrar mensajes y guardar si la entidad ya existe (tiene id)
        if (getView().getKeyValues() != null && !getView().getKeyValues().isEmpty()
                && getView().getKeyValues().get("id") != null) {
            if (!activo) {
                addWarning("El empleado fue marcado como INACTIVO.");
            } else {
                addInfo("El empleado fue marcado como ACTIVO.");
            }
            MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());
            addMessage("Datos guardados correctamente.");
        }
    }
}
