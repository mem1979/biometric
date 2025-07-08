package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class FeriadoAgregarNuevoFeriado  extends NewAction {

	private boolean irALista = false;
	
	public void execute() throws Exception {
		if ("true".equals(getRequest().getParameter("firstRequest"))) {
			irALista = true;
			return;
		}
		super.execute();
	}
	public String getNextMode() {
		return irALista?IChangeModeAction.LIST:IChangeModeAction.DETAIL;
	}
}