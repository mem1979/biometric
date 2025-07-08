package com.sta.biometric.acciones;
import org.openxava.actions.*;

import com.sta.biometric.servicios.*;

public class ImportarFeriadosAnioActualAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        ImportadorFeriadosService.importarFeriadosDelAnioActual();
        addMessage("Feriados del año actual importados correctamente.");
    }
}