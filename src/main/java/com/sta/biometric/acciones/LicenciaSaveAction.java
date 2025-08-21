
package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class LicenciaSaveAction extends SaveElementInCollectionAction {

    @Override
    public void execute() throws Exception {
    	
        
        Integer diasSolicitados = getView().getValueInt("licencias.dias");
        Integer diasRestantes = getView().getValueInt("licencias.diasRestantes");
        
        

        if (diasSolicitados == null || diasRestantes == null) {
            addError("Los campos 'Días solicitados' y 'Días restantes' deben estar completos.");
            return;
        }

        if (diasSolicitados > diasRestantes) {
        	getView().setEditable("licencias.diasRestantes", true);
            int excedente = diasSolicitados - diasRestantes;
            addError("La solicitud excede los días disponibles por " + excedente + " día(s). "
                   + "Días disponibles: " + diasRestantes + ", solicitados: " + diasSolicitados + ".");
            
            return; // No guarda si hay exceso
        }

        // Resta de d�as y guardado normal
        int dias = diasRestantes - diasSolicitados;
        getView().setValueNotifying("licencias.diasRestantes", dias);
        super.execute(); // Contin�a con el guardado
        addMessage("Licencia guardada correctamente. Se han descontado " + diasSolicitados + " día(s).");
    }
}
