package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;

public class LicenciasGenerateReportAction extends GenerateReportAction {

    @Override
    public void execute() throws Exception {

        // 1. Buscar el empleado a partir de la clave que hay en la vista
        String id = getView().getRoot().getValueString("id");   // o el tipo que uses
        Personal emp = XPersistence.getManager()
                          .find(Personal.class, id);

        // 2. Cambiar el t�tulo del Tab antes de exportar
        if (emp != null) {
            getTab().setTitle("Licencias de " + emp.getNombreCompleto());
            // setTitle() sustituye autom�ticamente el que usar�a el PDF :contentReference[oaicite:3]{index=3}
        }

        // 3. Ejecutar la l�gica est�ndar (genera PDF/XLS/CSV)
        super.execute();
    }
}