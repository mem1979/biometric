package com.sta.biometric.acciones;

import java.time.LocalDateTime;

import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.model.MapFacade;
import com.sta.biometric.modelo.*; // Importar modelos

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
                // Recuperar la entidad JPA
                Personal personal = (Personal) MapFacade.findEntity(getModelName(), clave);

                if (personal != null) {
                    if (!isRestaurar()) {
                        // Acciones al ELIMINAR

                        // 1. Obtener contrato vigente ANTES de cambiar estado
                        ContratoLaboral contrato = personal.getContratoVigente();

                        // 2. Cambiar estados
                        personal.setEliminado(true);
                        personal.setActivo(false);
                        personal.setFechaEliminacion(LocalDateTime.now());

                        // 3. Cerrar contrato
                        if (contrato != null) {
                            if (contrato.getFechaVigenciaHasta() == null ||
                                    contrato.getFechaVigenciaHasta().isAfter(java.time.LocalDate.now())) {
                                contrato.setFechaVigenciaHasta(java.time.LocalDate.now());
                                contrato.setMotivoFinalizacion("Baja automática por eliminación de empleado");
                            }
                        }
                    } else {
                        // Acciones al RESTAURAR
                        personal.setEliminado(false);
                        personal.setFechaEliminacion(null);
                        // NOTA: Activo permanece en false por seguridad
                    }

                    // Persistir cambios
                    org.openxava.jpa.XPersistence.getManager().merge(personal);
                    procesados++;
                }

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
