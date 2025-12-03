package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class PersonalAlIniciarAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Evaluar y asignar label para el campo 'activo'
        Boolean activo = (Boolean) getView().getValue("activo");
        if (activo != null) {
            if (activo) {
                getView().setLabelId("activo", "🔓 HABILITADO");
            } else {
                getView().setLabelId("activo", "🔒 DESABILITADO");
            }
        }

        // Evaluar y asignar label para el campo 'aceptaPausa'
        Boolean aceptaPausa = (Boolean) getView().getValue("aceptaPausa");
        if (aceptaPausa != null) {
            if (aceptaPausa) {
                getView().setLabelId("aceptaPausa", "⏸️ CON PAUSAS");
            } else {
                getView().setLabelId("aceptaPausa", "▶️ SIN PAUSAS");
            }
        }
    }
}
