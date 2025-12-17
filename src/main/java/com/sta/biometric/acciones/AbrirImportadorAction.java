package com.sta.biometric.acciones;

import org.openxava.actions.*;

/**
 * Acción que abre el diálogo de importación de fichadas.
 * 
 * <p>
 * Configura la vista del modelo ImportadorFichadas y establece
 * el controlador correspondiente.
 * </p>
 * 
 * @author Sistema STARH
 * @version 2.0
 */
public class AbrirImportadorAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        showDialog();
        getView().setTitle("Importar Registros de Fichadas");
        getView().setModelName("ImportadorFichadas");
        getView().setKeyEditable(true);
        getView().setEditable(true);
        getView().setValue("tieneEncabezados", true);
        setControllers("ImportadorFichadas");
    }
}
