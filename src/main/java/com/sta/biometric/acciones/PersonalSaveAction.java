package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.*;

/**
 * Acción de guardado personalizada para Personal.
 * 
 * <p>
 * Verifica si el empleado debe ser desactivado por falta de contrato vigente
 * y muestra un mensaje informativo al usuario.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class PersonalSaveAction extends SaveAction {

    @Override
    public void execute() throws Exception {
        // Verificar contrato ANTES de guardar
        boolean fueDesactivado = verificarYDesactivarSinContrato();

        // Ejecutar guardado normal
        super.execute();

        // Mostrar mensaje si fue desactivado
        if (fueDesactivado) {
            addWarning("empleado_desactivado_sin_contrato");
        }
    }

    /**
     * Verifica si el empleado está activo sin contrato vigente y lo desactiva.
     * 
     * @return true si el empleado fue desactivado automáticamente
     */
    private boolean verificarYDesactivarSinContrato() {
        try {
            Boolean activo = (Boolean) getView().getValue("activo");

            // Si ya está inactivo, no hay nada que hacer
            if (activo == null || !activo) {
                return false;
            }

            // Obtener la entidad para verificar contrato
            MapFacade.setValues(getModelName(), getView().getKeyValues(), getView().getValues());
            Object entity = MapFacade.findEntity(getModelName(), getView().getKeyValues());

            if (entity instanceof Personal) {
                Personal personal = (Personal) entity;
                if (personal.getContratoVigente() == null) {
                    // Desactivar en la vista
                    getView().setValue("activo", false);
                    return true;
                }
            }
        } catch (Exception e) {
            // Si hay error, continuar sin modificar
            e.printStackTrace();
        }
        return false;
    }
}
