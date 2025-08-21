package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;

public class DniEditarEnDialogoAction extends ViewBaseAction {
    public void execute() throws Exception {
    	
        Map<?, ?> clave = getView().getSubview("dni").getKeyValuesWithValue();   
         
        showDialog();                                        
        getView().setModelName("Dni");
        getView().setValues(clave);                             
        getView().findObject();
        getView().setTitle("Copia del Documento de Identidad");
        getView().setKeyEditable(false);
        addActions("TurnosHorarios.Guardar_Y_Salir");
       }
}

	
	