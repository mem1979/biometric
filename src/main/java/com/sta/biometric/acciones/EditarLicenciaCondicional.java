package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.view.*;

public class EditarLicenciaCondicional extends EditElementInCollectionAction {

    @SuppressWarnings("unchecked")
	@Override
    public void execute() throws Exception {
        super.execute();

        View view = getCollectionElementView();
        addActions("Licencia.ImprimirConstancia");
    
            Set<Object> props = view.getMembersNames().keySet();
            for (Object propObj : props) {
                String prop = propObj.toString();
                view.setEditable(prop, "observacion".equals(prop));
            }
        }
    
}

