package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;

public class VerMapaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {

        // Obtener entidad raiz (por ejemplo, Sucursales o Empleado)
        Object obj = getView().getRoot().getEntity();
        Map<?, ?> clave = getView().getKeyValues();

        // Obtener direccion embebida
        Object direccionObj = null;
        try {
            direccionObj = obj.getClass().getMethod("getDireccion").invoke(obj);
        } catch (Exception e) {
            addError("No se pudo acceder a la dirección embebida.");
            return;
        }

        if (!(direccionObj instanceof Direccion)) {
            addError("La dirección embebida no es válida.");
            return;
        }

        Direccion direccion = (Direccion) direccionObj;

        // Si no hay coordenadas, ejecutar accion que las obtenga
        if (direccion.getUbicacion() == null || direccion.getUbicacion().trim().isEmpty()) {
            // Ejecutar la accion ObtenerCoordenadasGenericaAction programaticamente
            executeAction("Coordenadas.ObtenerCoordenadas");
        }

        // Mostrar dialogo con la vista de direccion
        showDialog();
        getView().setModel(obj);
        getView().setViewName("VerMapa"); // AsegÃºrate que esta vista estÃ© definida
        getView().setValues(clave);
        getView().findObject();
        getView().setKeyEditable(false);
        getView().setEditable(false);
    }
}
