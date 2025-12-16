package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;

/**
 * Acción para mostrar el diálogo de selección de fecha
 * antes de generar el Informe Diario de Jornadas.
 */
public class MostrarDialogoInformeDiarioAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Mostrar diálogo
        showDialog();
        getView().setTitle("📅 CREAR INFORME HISTORICO DE JORNADAS");
        getView().setModelName("FechaInformeDiarioDialog");

        // Establecer fecha por defecto (hoy)
        getView().setValue("fecha", LocalDate.now());

        // Controlador del diálogo
        setControllers("FechaInformeDiario");
    }
}
