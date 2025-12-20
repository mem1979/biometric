package com.sta.biometric.acciones;

import org.openxava.actions.ViewBaseAction;

/**
 * Acción que permite cambiar el legajo (userId) de un empleado.
 * 
 * <p>
 * Abre un diálogo para ingresar el nuevo legajo, valida que no esté duplicado,
 * y si es válido lo asigna y guarda la entidad.
 * </p>
 */
public class CambiarLegajoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Verificar que la entidad exista (tiene id)
        if (getView().getKeyValues() == null || getView().getKeyValues().isEmpty()
                || getView().getKeyValues().get("id") == null) {
            addError("Primero debe guardar el empleado antes de cambiar el legajo.");
            return;
        }

        // Guardar datos en el contexto para la acción de confirmación
        getContext().put(getRequest(), "cambiarLegajo_personalId", getView().getKeyValues().get("id"));
        getContext().put(getRequest(), "cambiarLegajo_legajoActual", getView().getValue("userId"));

        // Mostrar diálogo
        showDialog();
        getView().setTitle("Cambiar Legajo");
        getView().setModelName("CambiarLegajoDialogo");

        setControllers("CambiarLegajoDialogo");
    }
}
