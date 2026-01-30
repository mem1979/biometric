package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.enums.TipoRedondeo;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.RedondeoHorasService;
import com.sta.biometric.servicios.LiquidacionJornadaService;

/**
 * Acción que aplica redondeo a los totales de una liquidación.
 * 
 * <p>
 * Calcula el ajuste necesario para redondear cada total (máx 30 min)
 * y lo distribuye entre los registros individuales.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class AplicarRedondeoMasivoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener valores del diálogo
        Integer intervalo = (Integer) getView().getValue("intervaloMinutos");
        TipoRedondeo estrategiaNormales = (TipoRedondeo) getView().getValue("estrategiaNormales");
        TipoRedondeo estrategiaExtras = (TipoRedondeo) getView().getValue("estrategiaExtras");
        TipoRedondeo estrategiaEspeciales = (TipoRedondeo) getView().getValue("estrategiaEspeciales");

        // Validaciones
        if (intervalo == null || intervalo <= 0) {
            addError("El intervalo de redondeo debe ser mayor a 0.");
            return;
        }

        if (estrategiaNormales == null || estrategiaExtras == null || estrategiaEspeciales == null) {
            addError("Debe seleccionar una estrategia para cada tipo de hora.");
            return;
        }

        // Obtener liquidación del contexto
        String liquidacionId = (String) getContext().get(getRequest(), "redondeo_liquidacion_id");
        if (liquidacionId == null) {
            addError("No se pudo recuperar la liquidación.");
            closeDialog();
            return;
        }

        LiquidacionJornadas liquidacion = XPersistence.getManager()
                .find(LiquidacionJornadas.class, liquidacionId);
        if (liquidacion == null) {
            addError("No se encontró la liquidación especificada.");
            closeDialog();
            return;
        }

        // Crear configuración
        ConfiguracionRedondeo config = new ConfiguracionRedondeo();
        config.setIntervaloMinutos(intervalo);
        config.setEstrategiaNormales(estrategiaNormales);
        config.setEstrategiaExtras(estrategiaExtras);
        config.setEstrategiaEspeciales(estrategiaEspeciales);

        // Aplicar redondeo (distribuido entre registros)
        int registrosModificados = RedondeoHorasService.aplicarRedondeoMasivo(liquidacion, config);

        // Recalcular liquidación para actualizar totales
        LiquidacionJornadaService.recalcularLiquidacion(liquidacion);

        // Commit cambios
        XPersistence.commit();

        // Limpiar contexto
        getContext().remove(getRequest(), "redondeo_liquidacion_id");

        // Cerrar diálogo y refrescar
        closeDialog();
        getView().refresh();

        // Mostrar resultado
        if (registrosModificados > 0) {
            addMessage("✅ Redondeo aplicado. " + registrosModificados
                    + " registro(s) ajustado(s). Liquidación recalculada.");
        } else {
            addMessage("ℹ️ No se requiere ajuste. Los totales ya están redondeados al intervalo seleccionado.");
        }
    }
}
