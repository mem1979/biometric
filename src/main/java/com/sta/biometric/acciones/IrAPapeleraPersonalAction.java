package com.sta.biometric.acciones;

import org.openxava.actions.ChangeModuleAction;

/**
 * Acción para navegar al módulo de Papelera de Personal.
 * 
 * <p>
 * Esta acción se muestra en la barra de herramientas del módulo Personal
 * y permite acceder rápidamente al módulo de papelera para ver y restaurar
 * los legajos eliminados.
 * </p>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @version 1.0
 */
public class IrAPapeleraPersonalAction extends ChangeModuleAction {

    @Override
    public void execute() throws Exception {
        // Establecer el módulo destino
        setNextModule("PapeleraPersonal");
        super.execute();
    }

}
