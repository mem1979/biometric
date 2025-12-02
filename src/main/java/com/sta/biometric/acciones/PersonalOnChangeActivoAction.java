package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class PersonalOnChangeActivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        Boolean activo = (Boolean) getNewValue();
        if (activo == null) return;

        if (!activo) {
            addWarning("El empleado fue marcado como INACTIVO. Recuerde guardar para aplicar cambios.");
            
        }
        else {
            addInfo("El empleado fue marcado como ACTIVO. Recuerde guardar para aplicar cambios.");
            
        }
    }
}
