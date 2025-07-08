package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;

public class LicenciasVerCalendarioAction extends CollectionBaseAction {           
 
    public void execute() throws Exception {
    	
    	 Object obj = getView().getRoot().getEntity();
        Map<?, ?> clave = getView().getKeyValues();
         
    
      
           
         // Mostrar dialogo con la vista del calendario anual
         showDialog();
         getView().setTitle("Calendario Laboral y Feriados");
         getView().setModel(obj);
         getView().setViewName("VerCalendario"); // Vista
         getView().setValues(clave);
         getView().findObject();
        
     
    }
 
}
	