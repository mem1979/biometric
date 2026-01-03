package com.sta.biometric.acciones;

import org.hibernate.*;
import org.openxava.actions.*;

import com.sta.biometric.embebidas.*;

/**
 * Acción que abre un diálogo para ver y editar la ubicación en el mapa.
 * Permite al usuario mover el pin y guardar las nuevas coordenadas.
 */
public class VerMapaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {

        // Obtener entidad raiz (por ejemplo, Sucursales o Empleado)
        Object obj = getView().getRoot().getEntity();

        // Obtener direccion embebida
        Direccion direccion = null;
        try {
            Object direccionObj = obj.getClass().getMethod("getDireccion").invoke(obj);
            if (direccionObj instanceof Direccion) {
                direccion = (Direccion) direccionObj;
            }
        } catch (Exception e) {
            addError("No se pudo acceder a la direccion embebida.");
            return;
        }

        if (direccion == null) {
            addError("La direccion embebida no es valida.");
            return;
        }

        // Si no hay coordenadas, ejecutar accion que las obtenga
        if (direccion.getUbicacion() == null || direccion.getUbicacion().trim().isEmpty()) {
            executeAction("Coordenadas.ObtenerCoordenadas");
            // Refresh the direccion after getting coordinates
            try {
                Object direccionObj = obj.getClass().getMethod("getDireccion").invoke(obj);
                if (direccionObj instanceof Direccion) {
                    direccion = (Direccion) direccionObj;
                }
            } catch (Exception e) {
                // Ignore, use existing direccion
            }
        }

        // Initialize lazy-loaded entities to build direccionFormateada safely
        if (direccion.getLocalidad() != null) {
            Hibernate.initialize(direccion.getLocalidad());
        }
        if (direccion.getPartido() != null) {
            Hibernate.initialize(direccion.getPartido());
        }
        if (direccion.getProvincia() != null) {
            Hibernate.initialize(direccion.getProvincia());
        }

        // Create a detached copy with only the data needed for VerMapa view
        // This avoids lazy initialization issues
        Direccion direccionParaVista = new Direccion();
        direccionParaVista.setCalle(direccion.getCalle());
        direccionParaVista.setNumero(direccion.getNumero());
        direccionParaVista.setPiso(direccion.getPiso());
        direccionParaVista.setCodigoPostal(direccion.getCodigoPostal());
        direccionParaVista.setUbicacion(direccion.getUbicacion());
        // Copy entity references (already initialized)
        direccionParaVista.setLocalidad(direccion.getLocalidad());
        direccionParaVista.setPartido(direccion.getPartido());
        direccionParaVista.setProvincia(direccion.getProvincia());

        // Mostrar dialogo con la vista VerMapa
        showDialog();
        getView().setModelName("Direccion");
        getView().setViewName("VerMapa");
        getView().setModel(direccionParaVista);
        getView().setEditable(true);

        // Agregar acciones de diálogo estándar: Guardar y Cerrar
        addActions("Coordenadas.guardarUbicacion", "Dialog.cancel");
    }
}
