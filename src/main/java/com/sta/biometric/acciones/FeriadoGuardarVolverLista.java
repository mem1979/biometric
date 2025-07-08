package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class FeriadoGuardarVolverLista extends SaveAction {
	
	public String getNextMode() {
		return getErrors().contains()?DETAIL:LIST;
	}

}
