package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;

public class FiltroMesActualFechaAction  extends TabBaseAction {
	

    @Override
    public void execute() throws Exception {

       	LocalDate desde = LocalDate.now();
        getTab().setConditionValue("fecha", desde);
    
    }
} 