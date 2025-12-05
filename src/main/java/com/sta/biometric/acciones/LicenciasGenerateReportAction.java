package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

public class LicenciasGenerateReportAction extends GenerateReportAction {

    @Override
    public void execute() throws Exception {

        // 1. Buscar el empleado a partir de la clave que hay en la vista
        String id = getView().getRoot().getValueString("id");
        Personal emp = XPersistence.getManager()
                          .find(Personal.class, id);

        // 2. Cambiar el titulo del Tab antes de exportar
        if (emp != null) {
            getTab().setTitle("Licencias de " + emp.getNombreCompleto());
        }

        // 3. Ejecutar la logica estandar (genera PDF/XLS/CSV)
        super.execute();
    }
}