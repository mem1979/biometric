package com.sta.biometric.acciones;

import javax.inject.*;

import org.openxava.actions.*;
import org.openxava.tab.*;

 public class TurnosHorariosNuevo extends ViewBaseAction implements  ICustomViewAction {
	 
	    @Inject 
		private Tab tab;
		
		public void execute() throws Exception {
			
			closeDialog(); 
			getTab().setModelName(getView().getModelName());	
			
			showDialog();                                                   
	        getView().setTitle("NUEVA JORNADA SEMANAL");         // 4
	        getView().setModelName("TurnosHorarios"); 
	        addActions("TurnosHorarios.Guardar_Y_Salir", "Dialog.cancel");
	        removeActions("MiTypicalNoResetNoNavNoImport.save");
	   
	    
		}
		
		public String getCustomView() {
			return PREVIOUS_VIEW; 
		}

		public Tab getTab() {
			return tab;
		}
		public void setTab(Tab tab) {
			this.tab = tab;
		}
		
		

	   
	}
