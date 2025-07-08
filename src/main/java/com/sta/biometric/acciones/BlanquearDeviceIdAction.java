package com.sta.biometric.acciones;

import org.openxava.actions.*;

import com.sta.biometric.modelo.*;

public class BlanquearDeviceIdAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        Object entidad = getView().getEntity();

        if (entidad instanceof Personal) {
            Personal emp = (Personal) entidad;
            emp.setDeviceId("");

            getView().setValueNotifying("deviceId",""); // Refresca la vista

            addMessage("Se a desvinculado el dispositivo del sistema");
            
        } else {
            addError("No se pudo acceder al empleado.");
        }
    }
}
