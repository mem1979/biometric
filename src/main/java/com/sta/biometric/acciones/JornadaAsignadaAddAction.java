package com.sta.biometric.acciones;

import org.openxava.actions.*;

import org.openxava.view.*;
import com.sta.biometric.embebidas.*;
import com.sta.biometric.modelo.*;

/**
 * Acción que se ejecuta al agregar una nueva JornadaAsignada a la colección.
 * 
 * <p>
 * Establece automáticamente la referencia al empleado padre (personal)
 * antes de guardar, asegurando la integridad de la relación bidireccional.
 * </p>
 * 
 * @see JornadaAsignada
 * @see Personal
 */
public class JornadaAsignadaAddAction extends AddElementsToCollectionAction {

    @Override
    public void execute() throws Exception {
        // Obtener la vista principal (Personal)
        View parentView = getView().getRoot();

        // Obtener el ID del Personal padre
        String personalId = (String) parentView.getValue("id");

        if (personalId != null) {
            // Establecer la referencia en la vista actual de JornadaAsignada
            getView().setValue("personal.id", personalId);
        }

        // Continuar con la acción normal de agregar elemento
        super.execute();
    }
}
