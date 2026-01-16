package com.sta.biometric.acciones;

import org.openxava.actions.IChangeModuleAction;
import org.openxava.actions.ViewBaseAction;

/**
 * Acción para volver al módulo de Personal desde la Papelera.
 * 
 * <p>
 * Usa PREVIOUS_MODULE para volver al módulo anterior (Personal)
 * según lo requiere OpenXava para evitar reentrada de módulos.
 * </p>
 * 
 * @author Sistema STARH - Mosquera, Marcelo
 * @version 1.0
 */
public class VolverAPersonalAction extends ViewBaseAction implements IChangeModuleAction {

    @Override
    public void execute() throws Exception {
        // No necesita hacer nada, solo implementar la interfaz
    }

    @Override
    public String getNextModule() {
        // Usar la constante PREVIOUS_MODULE para volver al módulo anterior
        return PREVIOUS_MODULE;
    }

    @Override
    public boolean hasReinitNextModule() {
        // No reinicializar el módulo al volver
        return false;
    }

}
