package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class ExedeDiasLicenciaAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        
        int dias =  getView().getValueInt("dias");
        int diasRestantes = getNewValue() != null ? ((Number) getNewValue()).intValue() : 0;

        if (diasRestantes > dias) {
            addError("dias_restantes_mayor_que_dias"); // Definir este mensaje en i18n
            getView().setValue("diasRestantes", 0); // Anula el valor ingresado
        }
    }
}