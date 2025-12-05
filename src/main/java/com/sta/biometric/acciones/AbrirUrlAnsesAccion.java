package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class AbrirUrlAnsesAccion extends ViewBaseAction implements IForwardAction {

    @Override
    public void execute() throws Exception {
        // No se requiere logica adicional, solo redirigir
    }

    @Override
    public String getForwardURI() {
        // Redirigir directamente a la URL de ANSES
        return "https://www.anses.gob.ar/consultas/constancia-de-cuil";
    }

    @Override
    public boolean inNewWindow() {
        return true; // Abre la URL en una nueva ventana o pestana.
    }
}