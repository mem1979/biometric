package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class TurnosHorariosBuscar extends ReferenceSearchAction {

    @Override
    public void execute() throws Exception {
        super.execute();  // Ejecuta b�squeda habitual
        addActions("TurnosHorarios.CrearTurno"); // Bot�n extra
    }
}
