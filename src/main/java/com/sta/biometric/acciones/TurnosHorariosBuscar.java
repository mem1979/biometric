package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class TurnosHorariosBuscar extends ReferenceSearchAction {

    @Override
    public void execute() throws Exception {
        super.execute();  // Ejecuta búsqueda habitual
        addActions("TurnosHorarios.CrearTurno"); // Botón extra
    }
}
