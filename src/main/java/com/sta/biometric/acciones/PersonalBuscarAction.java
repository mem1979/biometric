package com.sta.biometric.acciones;

import org.openxava.actions.*;

public class PersonalBuscarAction extends SearchByViewKeyAction {

    @Override
    public void execute() throws Exception {
        super.execute(); // Ejecuta la búsqueda estándar

        // Después de buscar, actualizar los labels según el estado
        actualizarLabels();
    }

    private void actualizarLabels() {
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
