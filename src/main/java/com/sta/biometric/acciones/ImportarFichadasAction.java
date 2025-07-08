package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class ImportarFichadasAction extends ViewBaseAction {

	@Override
	public void execute() throws Exception {
		showDialog();  
		getView().setTitle("Importar Archivo de Registros");
        getView().setModelName("ImportarFichadas"); 
        getView().setKeyEditable(true);
        getView().setEditable(true);
        setControllers("ImportarRegistros");
    		
	}
}
