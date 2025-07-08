package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;

public class VerMapaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {

        // Obtener entidad raíz (por ejemplo, Sucursales o Empleado)
        Object obj = getView().getRoot().getEntity();
        Map<?, ?> clave = getView().getKeyValues();

        // Obtener dirección embebida
        Object direccionObj = null;
        try {
            direccionObj = obj.getClass().getMethod("getDireccion").invoke(obj);
        } catch (Exception e) {
            addError("No se pudo acceder a la direcci�n embebida.");
            return;
        }

        if (!(direccionObj instanceof Direccion)) {
            addError("La direcci�n embebida no es v�lida.");
            return;
        }

        Direccion direccion = (Direccion) direccionObj;

        // Si no hay coordenadas, ejecutar acción que las obtenga
        if (direccion.getUbicacion() == null || direccion.getUbicacion().trim().isEmpty()) {
            // Ejecutar la acción ObtenerCoordenadasGenericaAction programáticamente
            executeAction("Coordenadas.ObtenerCoordenadas");
        }

        // Mostrar diálogo con la vista de dirección
        showDialog();
        getView().setModel(obj);
        getView().setViewName("VerMapa"); // Asegúrate que esta vista esté definida
        getView().setValues(clave);
        getView().findObject();
        getView().setKeyEditable(false);
        getView().setEditable(false);
    }
}
