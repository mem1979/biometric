package com.sta.biometric.acciones;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.ViewBaseAction;
import org.openxava.model.MapFacade;

/**
 * Acción para mover un legajo a la papelera desde la vista de detalle.
 * 
 * <p>
 * Esta acción reemplaza la acción de delete estándar de OpenXava para
 * implementar eliminación lógica (soft-delete) en lugar de eliminación física.
 * Se ejecuta cuando el usuario presiona Control+D o el botón Eliminar en
 * la vista de detalle de un empleado.
 * </p>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @version 1.0
 * @see Personal
 */
public class EliminarPersonalDesdeDetalleAction extends ViewBaseAction {

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Obtener la clave del registro actual desde la vista
        Map<String, Object> clave = getView().getKeyValues();

        if (clave == null || clave.isEmpty() || clave.get("id") == null) {
            addError("no_delete_not_exists");
            return;
        }

        try {
            // Preparar los valores para marcar como eliminado e inactivo
            Map<String, Object> valores = new HashMap<>();
            valores.put("eliminado", true);
            valores.put("activo", false); // Desactivar el empleado
            valores.put("fechaEliminacion", LocalDateTime.now());

            // Actualizar el registro usando MapFacade
            MapFacade.setValues(getModelName(), clave, valores);

            // Reiniciar caches para combos
            resetDescriptionsCache();

            // Mostrar mensaje de éxito usando mensaje estándar de OX
            addMessage("object_deleted", getModelName());

            // Limpiar la vista y dejarla como nueva
            getView().clear();
            getView().setKeyEditable(true);
            getView().setEditable(false);

        } catch (javax.validation.ValidationException ve) {
            addError("no_delete_row", 0, clave);
            addError("remove_error", getModelName(), ve.getMessage());
        } catch (Exception e) {
            addError("error_procesar_registro", e.getMessage());
        }
    }

}
