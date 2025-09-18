package com.sta.biometric.acciones;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.view.*;

public class EditarLicenciaCondicional extends EditElementInCollectionAction {

    @SuppressWarnings("unused")
	private static final Set<String> PERMITIDOS = Set.of("observacion", "certificado");

    @Override
    public void execute() throws Exception {
        super.execute(); // abre el diálogo

        View v = getCollectionElementView();

        addActions("Licencia.ImprimirConstancia");

        // Deshabilitar todos los miembros de manera explícita
        for (Object o : v.getMembersNames().keySet()) {
            String prop = String.valueOf(o);
            v.setEditable(prop, false);
        }

        // Habilitar solo los permitidos
        v.setEditable("observacion", true);
        v.setEditable("certificado", true);

        // Debug
        System.out.println("[EditarLicenciaCondicional] Miembros en diálogo: " + v.getMembersNames().keySet());
        System.out.println("[EditarLicenciaCondicional] 'observacion' editable=" + v.isEditable("observacion"));
        System.out.println("[EditarLicenciaCondicional] 'certificado' editable=" + v.isEditable("certificado"));
    }
}