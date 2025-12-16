package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

public class PersonalOnChangePausaAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean aceptaPausa = (Boolean) getNewValue();
        if (aceptaPausa == null)
            return;

        // Actualizar etiqueta siempre
        if (!aceptaPausa) {
            getView().setLabelId("aceptaPausa", "▶️ SIN PAUSAS");
        } else {
            getView().setLabelId("aceptaPausa", "⏸️ CON PAUSAS");
        }

        // Solo mostrar mensajes y guardar si la entidad ya existe (tiene id)
        if (getView().getKeyValues() != null && !getView().getKeyValues().isEmpty()
                && getView().getKeyValues().get("id") != null) {
            if (!aceptaPausa) {
                addWarning("Los turnos NO permitiran registrar pausas.");
            } else {
                addInfo("Los turnos permitiran registrar pausas.");
            }
            MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());
            addMessage("Datos guardados correctamente.");
        }
    }
}
