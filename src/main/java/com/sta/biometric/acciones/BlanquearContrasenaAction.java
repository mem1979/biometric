package com.sta.biometric.acciones;
import org.openxava.actions.*;

import com.sta.biometric.modelo.*;

public class BlanquearContrasenaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        Object entidad = getView().getEntity();

        if (entidad instanceof Personal) {
            Personal emp = (Personal) entidad;
            emp.setContrasena("1234"); // o "1234", si prefer�s un valor por defecto

            getView().setValueNotifying("contrasena","1234"); // Refresca la vista

            addMessage("La contrase�a ha sido blanqueada. Se asign� Valor por defecto '1234'");
            
        } else {
            addError("No se pudo acceder al empleado.");
        }
    }
}