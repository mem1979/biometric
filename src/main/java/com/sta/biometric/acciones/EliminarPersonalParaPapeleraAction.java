package com.sta.biometric.acciones;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.model.MapFacade;

import lombok.Getter;
import lombok.Setter;

/**
 * Acción para mover legajos del personal a la papelera (eliminación lógica).
 * 
 * <p>
 * Esta acción reemplaza las acciones deleteSelected y deleteRow de OpenXava
 * para implementar soft-delete en lugar de eliminación física.
 * </p>
 * 
 * <p>
 * La propiedad {@code restaurar} permite reutilizar esta acción tanto para
 * eliminar como para restaurar registros:
 * </p>
 * <ul>
 * <li>{@code restaurar = false}: Mueve a papelera (eliminación lógica)</li>
 * <li>{@code restaurar = true}: Restaura desde papelera</li>
 * </ul>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @version 1.0
 * @see Personal
 */
public class EliminarPersonalParaPapeleraAction extends TabBaseAction {

    /**
     * Si es true, restaura los registros (eliminado = false).
     * Si es false, mueve a papelera (eliminado = true).
     */
    @Getter
    @Setter
    private boolean restaurar = false;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        // Obtener las claves seleccionadas
        Map<String, Object>[] clavesSeleccionadas = getSelectedKeys();

        if (clavesSeleccionadas == null || clavesSeleccionadas.length == 0) {
            addWarning("no_rows_selected");
            return;
        }

        int procesados = 0;

        for (int i = 0; i < clavesSeleccionadas.length; i++) {
            Map<String, Object> clave = clavesSeleccionadas[i];
            try {
                // Preparar los valores a actualizar
                Map<String, Object> valores = new HashMap<>();
                valores.put("eliminado", !isRestaurar());

                // Al eliminar: desactivar el empleado
                // Al restaurar: NO reactivar automáticamente (debe hacerse manualmente)
                if (!isRestaurar()) {
                    valores.put("activo", false);
                    valores.put("fechaEliminacion", LocalDateTime.now());
                } else {
                    // Solo limpiar la fecha de eliminación, activo permanece en false
                    valores.put("fechaEliminacion", null);
                }

                // Actualizar el registro usando MapFacade
                MapFacade.setValues(getModelName(), clave, valores);
                procesados++;

            } catch (javax.validation.ValidationException ve) {
                addError("no_delete_row", i, clave);
                addError("remove_error", getModelName(), ve.getMessage());
            } catch (Exception e) {
                addError("no_delete_row", i, clave);
            }
        }

        // Mostrar mensajes según el resultado
        if (procesados > 0) {
            if (isRestaurar()) {
                addMessage("registros_restaurados", procesados);
            } else {
                // Usar cantidad eliminada en mensaje
                addMessage("objects_deleted", procesados);
            }
        }

        // Refrescar la lista
        getTab().deselectAll();
        resetDescriptionsCache();
    }

}
