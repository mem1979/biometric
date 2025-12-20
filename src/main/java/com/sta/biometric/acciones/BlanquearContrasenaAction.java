package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.MapFacade;

public class BlanquearContrasenaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Verificar que la entidad exista
        if (getView().getKeyValues() == null || getView().getKeyValues().isEmpty()
                || getView().getKeyValues().get("id") == null) {
            addError("Primero debe guardar el empleado.");
            return;
        }

        // Actualizar la vista
        getView().setValue("contrasena", "1234");

        // Guardar la entidad
        MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());

        addMessage("La contraseña ha sido blanqueada a '1234' y guardada.");
    }
}