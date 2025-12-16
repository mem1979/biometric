package com.sta.biometric.acciones;

import java.util.Map;

import org.openxava.actions.SearchByViewKeyAction;

/**
 * Acción unificada que se ejecuta al buscar/editar un registro existente en
 * Personal.
 * 
 * <p>
 * Esta acción:
 * <ul>
 * <li>Cambia a la vista principal (completa) con todas las secciones</li>
 * <li>Actualiza los labels dinámicos (activo, aceptaPausa)</li>
 * </ul>
 * </p>
 * 
 * @author Sistema STARH
 * @see PersonalNuevoAction
 */
public class PersonalBuscarVistaAction extends SearchByViewKeyAction {

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Guardar la clave actual antes de cambiar la vista
        Map<String, Object> clave = getView().getKeyValuesWithValue();

        // Cambiar a la vista principal (cadena vacía = vista por defecto)
        getView().setViewName("");

        // Restaurar la clave en la nueva vista
        getView().setValues(clave);

        // Ejecutar la búsqueda normal
        super.execute();

        // Después de buscar, actualizar los labels según el estado
        actualizarLabels();
    }

    /**
     * Actualiza los labels dinámicos de los campos booleanos
     * según su valor actual.
     */
    private void actualizarLabels() {
        // Evaluar y asignar label para el campo 'activo'
        Boolean activo = (Boolean) getView().getValue("activo");
        if (activo != null) {
            if (activo) {
                getView().setLabelId("activo", "🔓 HABILITADO");
            } else {
                getView().setLabelId("activo", "🔒 DESABILITADO");
            }
        }

        // Evaluar y asignar label para el campo 'aceptaPausa'
        Boolean aceptaPausa = (Boolean) getView().getValue("aceptaPausa");
        if (aceptaPausa != null) {
            if (aceptaPausa) {
                getView().setLabelId("aceptaPausa", "⏸️ CON PAUSAS");
            } else {
                getView().setLabelId("aceptaPausa", "▶️ SIN PAUSAS");
            }
        }
    }
}
