package com.sta.biometric.modelo.rrhh;

import org.openxava.actions.*;

/**
 * Acción OnChange para el campo 'regimen' en Contrato.
 * 
 * Lógica:
 * - Si es INFORMAL o LOCACION_SERVICIOS, quizás no aplica Convenio.
 * - (Por ahora simple refresh, se expandirá con lógica de UI).
 */
public class ContratoOnChangeRegimenAction extends OnChangePropertyBaseAction {

    @Override
    public void execute() throws Exception {
        // En Fase 1 solo refrescamos la vista para que funcionen los depends si los
        // hubiera.
        // Futuro: setear convenio = null si regimen == INFORMAL
        getView().refresh();
    }

}
