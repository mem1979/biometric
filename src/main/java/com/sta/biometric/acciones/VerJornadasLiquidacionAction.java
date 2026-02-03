package com.sta.biometric.acciones;

import java.util.*;
import org.openxava.actions.*;
import com.sta.biometric.modelo.*;

/**
 * Acción para ver las jornadas de una liquidación desde la lista de la
 * colección.
 * 
 * <p>
 * Funciona tanto desde @RowAction (fila específica) como @ListAction
 * (selección).
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.1
 */
public class VerJornadasLiquidacionAction extends CollectionBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener el objeto seleccionado (funciona para @RowAction y @ListAction)
        List<?> selectedObjects = getSelectedObjects();

        if (selectedObjects == null || selectedObjects.isEmpty()) {
            addError("Debe seleccionar una liquidación");
            return;
        }

        // Tomamos el primer elemento seleccionado
        LiquidacionJornadas liquidacion = (LiquidacionJornadas) selectedObjects.get(0);

        if (liquidacion == null) {
            addError("No se pudo cargar la liquidación seleccionada");
            return;
        }

        // Abrimos diálogo modal
        showDialog();

        // Configuramos el diálogo para mostrar la vista "SoloJornadas"
        getView().setModelName("LiquidacionJornadas");
        getView().setViewName("SoloJornadas");

        // Cargamos la liquidación por su ID
        Map<String, Object> key = new HashMap<>();
        key.put("id", liquidacion.getId());
        getView().setValues(key);
        getView().findObject();
        getView().setEditable(false);

        // Título del diálogo
        getView().setTitleId("LiquidacionJornadas.jornadasDelPeriodo");

        // Botones del diálogo (solo cerrar)
        setControllers("Dialog");
    }
}
