package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.embebidas.*;

/**
 * Acción que guarda las coordenadas editadas en el mapa
 * y cierra el diálogo.
 */
public class GuardarUbicacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener las coordenadas del diálogo
        String nuevaUbicacion = (String) getView().getValue("ubicacion");

        if (nuevaUbicacion == null || nuevaUbicacion.trim().isEmpty()) {
            addWarning("No hay coordenadas para guardar.");
            closeDialog();
            return;
        }

        System.out.println("[GuardarUbicacion] Nueva ubicación: " + nuevaUbicacion);

        try {
            // Obtener el modelo y las claves desde la vista padre
            String modelName = getPreviousView().getRoot().getModelName();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> keyValues = getPreviousView().getRoot().getKeyValues();

            System.out.println("[GuardarUbicacion] Modelo: " + modelName + ", Keys: " + keyValues);

            if (keyValues == null || keyValues.isEmpty()) {
                addError("No se encontraron claves de la entidad padre.");
                closeDialog();
                return;
            }

            // Buscar la entidad en la base de datos
            Object entidad = org.openxava.model.MapFacade.findEntity(modelName, keyValues);

            if (entidad == null) {
                addError("No se encontró la entidad en la base de datos.");
                closeDialog();
                return;
            }

            // Obtener la dirección y actualizarla
            java.lang.reflect.Method getDireccion = entidad.getClass().getMethod("getDireccion");
            Object direccionObj = getDireccion.invoke(entidad);

            if (direccionObj instanceof Direccion) {
                Direccion direccion = (Direccion) direccionObj;
                String ubicacionAnterior = direccion.getUbicacion();

                System.out.println("[GuardarUbicacion] Ubicación anterior: " + ubicacionAnterior);

                // Actualizar la ubicación
                direccion.setUbicacion(nuevaUbicacion.trim());

                // Persistir el cambio usando merge
                XPersistence.getManager().merge(entidad);
                XPersistence.getManager().flush();

                System.out.println("[GuardarUbicacion] Entidad persistida con merge+flush");

                // Actualizar la vista padre
                getPreviousView().setValueNotifying("direccion.ubicacion", nuevaUbicacion.trim());

                if (ubicacionAnterior == null || !ubicacionAnterior.equals(nuevaUbicacion)) {
                    addMessage("Ubicación actualizada: " + nuevaUbicacion);
                } else {
                    addMessage("Ubicación sin cambios.");
                }
            } else {
                addError("La entidad no tiene una dirección válida.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Error al guardar la ubicación: " + e.getMessage());
        }

        // Cerrar el diálogo
        closeDialog();
    }
}
