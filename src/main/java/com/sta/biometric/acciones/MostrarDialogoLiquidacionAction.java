package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción que muestra el diálogo para seleccionar el período de liquidación.
 * 
 * <p>
 * Abre un diálogo con campos fecha desde/hasta, configura el título
 * y asigna el controlador para las acciones dentro del diálogo.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class MostrarDialogoLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Guardar el ID del empleado en el contexto del request
        String empleadoId = getView().getValueString("id");

        if (empleadoId == null || empleadoId.isEmpty()) {
            addError("Debe guardar el empleado antes de generar una liquidación");
            return;
        }

        // Guardar en el contexto para la acción de confirmación
        getContext().put(getRequest(), "liquidacion_empleado_id", empleadoId);

        // Mostrar el diálogo con el formulario de selección de período
        showDialog();

        // Configurar título del diálogo
        getView().setTitle("📅 Seleccionar Período de Liquidación");

        // Configurar el modelo y controlador del diálogo (reutilizamos RangoFechas)
        getView().setModelName("RangoFechas");
        getView().setViewName("");

        // Asignar el controlador para las acciones del diálogo
        setControllers("PeriodoLiquidacion");

        getView().reset();
    }
}
