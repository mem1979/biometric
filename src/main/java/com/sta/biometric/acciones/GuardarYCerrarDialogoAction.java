package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción que guarda la entidad actual y cierra el diálogo.
 * Extiende SaveAction para heredar toda la lógica de guardado.
 */
public class GuardarYCerrarDialogoAction extends SaveAction {

    @Override
    public void execute() throws Exception {
        // Primero guardamos usando la lógica heredada de SaveAction
        super.execute();

        // Si no hay errores, cerramos el diálogo
        if (!getErrors().contains()) {
            closeDialog();
        }
    }
}
