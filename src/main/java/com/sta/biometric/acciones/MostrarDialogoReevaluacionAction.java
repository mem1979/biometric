package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Muestra el diálogo para ingresar fechas de reevaluación.
 */
public class MostrarDialogoReevaluacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        showDialog();
        getView().setModelName("DialogoReevaluacion");
        getView().setTitle("Reevaluar Jornadas (Recuperación)");
        setControllers("DialogoReevaluacionController"); // Controlador con Aceptar/Cancelar
    }
}
