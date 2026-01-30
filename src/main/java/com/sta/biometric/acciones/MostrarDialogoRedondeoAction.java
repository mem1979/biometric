package com.sta.biometric.acciones;

import org.openxava.actions.*;

import com.sta.biometric.enums.TipoRedondeo;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.ConfiguracionesPreferencias;

/**
 * Acción que abre el diálogo de configuración de redondeo automático.
 * 
 * <p>
 * Se ejecuta desde la vista de LiquidacionJornadas para permitir
 * al usuario configurar y aplicar redondeo masivo a todos los registros.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class MostrarDialogoRedondeoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Verificar que estamos en el contexto correcto
        Object entity = getView().getEntity();
        if (!(entity instanceof LiquidacionJornadas)) {
            addError("Esta acción solo puede ejecutarse desde Liquidación de Jornadas.");
            return;
        }

        LiquidacionJornadas liquidacion = (LiquidacionJornadas) entity;

        // Guardar ID de liquidación en contexto
        getContext().put(getRequest(), "redondeo_liquidacion_id", liquidacion.getId());

        // Mostrar diálogo
        showDialog();
        getView().setTitle("⚙️ Configurar Redondeo Automático");
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

        // Asignar controlador del diálogo
        setControllers("ConfiguracionRedondeo");
    }
}
