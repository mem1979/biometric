package com.sta.biometric.acciones;

import org.openxava.actions.*;

import com.sta.biometric.enums.TipoRedondeo;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.ConfiguracionesPreferencias;

/**
 * Acción que abre el diálogo de configuración de redondeo para un registro
 * individual.
 * 
 * <p>
 * Se ejecuta desde la vista de detalle de AuditoriaRegistros para permitir
 * al usuario configurar y aplicar redondeo a un solo registro.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class MostrarDialogoRedondeoIndividualAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Verificar que estamos en el contexto correcto
        Object entity = getView().getEntity();
        if (!(entity instanceof AuditoriaRegistros)) {
            addError("Esta acción solo puede ejecutarse desde un registro de Auditoría.");
            return;
        }

        AuditoriaRegistros registro = (AuditoriaRegistros) entity;

        // Guardar ID de registro en contexto
        getContext().put(getRequest(), "redondeo_registro_id", registro.getId());

        // Mostrar diálogo
        showDialog();
        getView().setTitle("⚙️ Redondeo: " + registro.getEmpleado().getNombreCompleto() + " - " + registro.getFecha());
        getView().setModelName("ConfiguracionRedondeo");

        // Establecer valores por defecto desde configuración
        int intervaloDefault = ConfiguracionesPreferencias.obtenerValor(
                "redondeo.intervalo.minutos", 30, Integer.class);
        TipoRedondeo estrategiaDefault = ConfiguracionesPreferencias.obtenerValor(
                "redondeo.estrategia", TipoRedondeo.MATEMATICO, TipoRedondeo.class);

        getView().setValue("intervaloMinutos", intervaloDefault);
        getView().setValue("estrategiaNormales", estrategiaDefault);
        getView().setValue("estrategiaExtras", estrategiaDefault);
        getView().setValue("estrategiaEspeciales", estrategiaDefault);

        // Asignar controlador del diálogo (reutilizar el existente con otra acción)
        setControllers("ConfiguracionRedondeoIndividual");
    }
}
