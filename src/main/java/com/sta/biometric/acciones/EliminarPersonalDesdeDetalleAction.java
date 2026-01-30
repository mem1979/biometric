package com.sta.biometric.acciones;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.ViewBaseAction;
import org.openxava.model.MapFacade;
import com.sta.biometric.modelo.*; // Importar modelos

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
            // Usar JPA directamente para asegurar integridad y cierre de contrato
            Personal personal = (Personal) MapFacade.findEntity(getModelName(), clave);

            if (personal != null) {
                // 1. Obtener contrato vigente ANTES de marcar eliminado
                // (Porque isVigente() devuelve false si el empleado ya está eliminado)
                ContratoLaboral contrato = personal.getContratoVigente();

                // 2. Marcar eliminado e inactivo
                personal.setEliminado(true);
                personal.setActivo(false);
                personal.setFechaEliminacion(LocalDateTime.now());

                // 3. Cerrar contrato si existe
                if (contrato != null) {
                    if (contrato.getFechaVigenciaHasta() == null ||
                            contrato.getFechaVigenciaHasta().isAfter(java.time.LocalDate.now())) {

                        contrato.setFechaVigenciaHasta(java.time.LocalDate.now());
                        contrato.setMotivoFinalizacion("Baja automática por eliminación de empleado");
                    }
                }

                // 4. Persist cambios
                org.openxava.jpa.XPersistence.getManager().merge(personal);
            }

            // Reiniciar caches para combos
            resetDescriptionsCache();

            // Mostrar mensaje de éxito usando mensaje estándar de OX
            addMessage("object_deleted", getModelName());

            // Limpiar la vista y dejarla como nueva
            getView().clear();
            getView().setKeyEditable(true);
            getView().setEditable(false);

        } catch (javax.validation.ValidationException ve) {
            // ... existing catch ...
            addError("no_delete_row", 0, clave);
            addError("remove_error", getModelName(), ve.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

}
