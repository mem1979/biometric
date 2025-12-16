package com.sta.biometric.acciones;

import org.openxava.actions.NewAction;

/**
 * Acción que se ejecuta al pulsar "Nuevo" en el módulo Personal.
 * 
 * <p>
 * Cambia a la vista simplificada "Crear" que solo muestra las secciones
 * de Datos Personales y Datos Laborales básicos.
 * </p>
 * 
 * @author Sistema STARH
 * @see PersonalBuscarVistaAction
 */
public class PersonalNuevoAction extends NewAction {

    @Override
    public void execute() throws Exception {
        getView().setViewName("Crear");
        super.execute();
    }
}
