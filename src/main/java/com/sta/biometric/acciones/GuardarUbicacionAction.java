package com.sta.biometric.acciones;

import java.lang.reflect.*;

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

        // Obtener la entidad padre desde la vista anterior
        Object entidadPadre = getPreviousView().getRoot().getEntity();

        if (entidadPadre == null) {
            addError("No se pudo acceder a la entidad padre.");
            closeDialog();
            return;
        }

        try {
            // Obtener la dirección de la entidad padre
            Method getDireccion = entidadPadre.getClass().getMethod("getDireccion");
            Object direccionObj = getDireccion.invoke(entidadPadre);

            if (direccionObj instanceof Direccion) {
                Direccion direccion = (Direccion) direccionObj;
                String ubicacionAnterior = direccion.getUbicacion();

                // Actualizar la ubicación
                direccion.setUbicacion(nuevaUbicacion.trim());

                // Persistir el cambio
                XPersistence.getManager().merge(entidadPadre);
                XPersistence.commit();

                // Actualizar la vista padre
                getPreviousView().setValueNotifying("direccion.ubicacion", nuevaUbicacion.trim());

                if (ubicacionAnterior == null || !ubicacionAnterior.equals(nuevaUbicacion)) {
                    addMessage("Ubicación actualizada: " + nuevaUbicacion);
                } else {
                    addMessage("Ubicación sin cambios.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Error al guardar la ubicación: " + e.getMessage());
        }

        // Cerrar el diálogo
        closeDialog();
    }
}
