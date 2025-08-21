package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.view.*;

public class CrearLicenciaAction extends CreateNewElementInCollectionAction {

    @Override
    public void execute() throws Exception {
        super.execute();
        View view = getCollectionElementView();
        // En creación, permitimos todos los campos hasta que guarde
        setAllEditable(view, true);
        view.setEditable("diasRestantes", false);
    }

    @SuppressWarnings("unchecked")
	private void setAllEditable(View view, boolean editable) throws Exception {
        Set<Object> props = view.getMembersNames().keySet();
        for (Object propObj : props) {
            view.setEditable(propObj.toString(), editable);
        }
    }
}