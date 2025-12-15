package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;

/**
 * Acción que muestra el diálogo para seleccionar el período de liquidación.
 * 
 * <p>
 * Se ejecuta desde el botón "Nueva liquidación" de la colección de
 * liquidaciones
 * en Personal. Al ser una acción de colección, extiende de
 * {@link CollectionElementViewBaseAction}
 * para tener acceso a la vista padre (Personal) y poder obtener el ID del
 * empleado.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class MostrarDialogoLiquidacionAction extends CollectionElementViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener el ID del empleado desde la vista padre (Personal)
        String empleadoId = getParentView().getValueString("id");

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

        // Configurar el modelo del diálogo (reutilizamos RangoFechas)
        getView().setModelName("RangoFechas");

        // Asignar el controlador para las acciones del diálogo
        setControllers("PeriodoLiquidacion");

        // Inicializar la vista y establecer valores por defecto del período (mes
        // actual)
        getView().reset();
        LocalDate hoy = LocalDate.now();
        getView().setValue("periodoDesde", hoy.withDayOfMonth(1));
        getView().setValue("periodoHasta", hoy.withDayOfMonth(hoy.lengthOfMonth()));
    }
}
