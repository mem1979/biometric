package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class LicenciaSaveAction extends SaveElementInCollectionAction {

    @Override
    public void execute() throws Exception {
    	
        
        Integer diasSolicitados = getView().getValueInt("licencias.dias");
        Integer diasRestantes = getView().getValueInt("licencias.diasRestantes");
        
        

        if (diasSolicitados == null || diasRestantes == null) {
            addError("Los campos 'D�as solicitados' y 'D�as restantes' deben estar completos.");
            return;
        }

        if (diasSolicitados > diasRestantes) {
            int excedente = diasSolicitados - diasRestantes;
            addError("La solicitud excede los d�as disponibles por " + excedente + " d�a(s). "
                   + "D�as disponibles: " + diasRestantes + ", solicitados: " + diasSolicitados + ".");
            return; // No guarda si hay exceso
        }

        // Resta de d�as y guardado normal
        int dias = diasRestantes - diasSolicitados;
        getView().setValueNotifying("licencias.diasRestantes", dias);
        super.execute(); // Contin�a con el guardado
        addMessage("Licencia guardada correctamente. Se han descontado " + diasSolicitados + " d�a(s).");
    }
}
