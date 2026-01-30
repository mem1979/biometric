package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

public class PersonalOnChangeActivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean activo = (Boolean) getNewValue();
        if (activo == null)
            return;

        // === VERIFICAR CONTRATO VIGENTE ANTES DE ACTIVAR ===
        if (activo && getView().getKeyValues() != null && getView().getKeyValues().get("id") != null) {
            // Verificar si tiene contrato vigente
            Object entity = MapFacade.findEntity(getModelName(), getView().getKeyValues());
            if (entity instanceof com.sta.biometric.modelo.Personal) {
                com.sta.biometric.modelo.Personal personal = (com.sta.biometric.modelo.Personal) entity;
                if (personal.getContratoVigente() == null) {
                    // No permitir activar sin contrato
                    getView().setValue("activo", false);
                    addWarning("empleado_desactivado_sin_contrato");
                    getView().setLabelId("activo", "🔒 DESABILITADO");
                    return;
                }
            }
        }

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
