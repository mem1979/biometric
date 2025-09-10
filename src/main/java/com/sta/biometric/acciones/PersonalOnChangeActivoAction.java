package com.sta.biometric.acciones;
import org.openxava.actions.*;

/**
 * Acción OnChange que informa al usuario sobre el cambio de estado activo/inactivo,
 * solicitando que guarde manualmente para aplicar los cambios.
 */
public class PersonalOnChangeActivoAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {

        Boolean activo = (Boolean) getNewValue();
        if (activo == null) return;

        if (!activo) {
        	// Prefijar "x-" al userId original
            String userId = (String) getView().getValue("userId");
            if (userId != null && !userId.startsWith("x-")) {
                getView().setValue("userId", "x-" + userId);
            }
            getView().setEditable(false);
            addWarning("El empleado ha sido marcado como inactivo. Debe guardar los cambios para que el estado se aplique en el sistema.");
        } else {
            addInfo("El empleado activo. Debe guardar los cambios para que el estado se aplique en el sistema.");
            getView().setEditable(true);
        }
    }
}
