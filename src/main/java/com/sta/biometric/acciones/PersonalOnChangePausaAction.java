package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

public class PersonalOnChangePausaAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean aceptaPausa = (Boolean) getNewValue();
        if (aceptaPausa == null)
            return;

        if (!aceptaPausa) {
            getView().setLabelId("aceptaPausa", "▶️ SIN PAUSAS");// PLAY
            addWarning("Los turnos NO permitiran registrar pausas.");
        } else {
            getView().setLabelId("aceptaPausa", "⏸️ CON PAUSAS");// PAUSE
            addInfo("Los turnos permitiran registrar pausas.");
        }

        MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());
        addMessage("Datos guardados correctamente.");
    }
}
