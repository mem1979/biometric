package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción para actualizar el dashboard cuando cambian los filtros de fecha.
 * Fuerza un refresco completo de la vista para que se recalculen todas las
 * propiedades, gráficos y listas dependientes de los filtros.
 */
public class ActualizarDashboardAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        // Forzar refresco completo de la vista
        // Esto recalculará todos los @LargeDisplay, @Chart y @SimpleList
        getView().refreshCollections();
    }

}
