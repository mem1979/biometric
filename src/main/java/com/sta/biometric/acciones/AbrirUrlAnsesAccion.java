package com.sta.biometric.acciones;
import org.openxava.actions.*;
import org.openxava.controller.*;
import org.openxava.util.*;

public class AbrirUrlAnsesAccion  implements IForwardAction {

    @Override
    public String getForwardURI() {
        // Redirigir directamente a la URL de ANSES
        return "https://www.anses.gob.ar/consultas/constancia-de-cuil";
    }

    @Override
    public boolean inNewWindow() {
        return true; // Abre la URL en una nueva ventana o pestaña.
    }

	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setErrors(Messages errors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Messages getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMessages(Messages messages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Messages getMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnvironment(Environment environment) {
		// TODO Auto-generated method stub
		
	}
    
}