package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class TurnosHorariosNuevo extends ViewBaseAction {           
 
    public void execute() throws Exception {
        showDialog();                                                   
        getView().setTitle("NUEVA JORNADA SEMANAL");         // 4
        getView().setModelName("TurnosHorarios");                      // 5
        setControllers("TurnosHorariosCreation"); 
    }
 
}
