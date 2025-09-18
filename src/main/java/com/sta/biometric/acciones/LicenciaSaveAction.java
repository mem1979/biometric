
 package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.util.*;

/**
 * Acción de guardado de una licencia dentro de una colección.
 *
 * Objetivos:
 * 1) Validar días solicitados vs. restantes SOLO al CREAR (clave editable).
 * 2) Evitar revalidar al ACTUALIZAR (clave NO editable): guardar directo.
 * 3) Unificar mensajes con i18n (error, warning, éxito).
 */
public class LicenciaSaveAction extends SaveElementInCollectionAction {

    @Override
    public void execute() throws Exception {
        // === Determinar si es creación o actualización ===
        // OpenXava: si la clave es editable, es un registro nuevo (creación).
        // Si no, estamos actualizando uno existente.
        boolean creando = getCollectionElementView().isKeyEditable();
  //      System.out.println("creando: " + creando);

        if (!creando) {
            // === Modo ACTUALIZACIÓN ===
            // No revalidar: hacer el guardado tal cual y notificar éxito de actualización.
            super.execute();
             return;
        }

        // === Modo CREACIÓN ===
        // Recuperamos datos para la validación inicial.
        Integer diasSolicitados = getView().getValueInt("licencias.dias");
        Integer diasRestantes   = getView().getValueInt("licencias.diasRestantes");
        boolean editable        = getView().isEditable("licencias.diasRestantes");

        // Validación unificada para creación.
        // Si retorna false, significa que ya se informó error/warning y se debe detener.
        if (!validarDias(editable, diasSolicitados, diasRestantes)) return;

        // === Persistencia (creación) ===
        int solicitados = diasSolicitados != null ? diasSolicitados : 0;
        int restantes   = diasRestantes   != null ? diasRestantes   : 0;
        int dias = restantes - solicitados;

        // Notifica el nuevo valor en la vista (y a listeners si corresponde).
        getView().setValueNotifying("licencias.diasRestantes", dias);

        // Ejecuta el guardado real en la colección.
        super.execute();

        // Mensaje de éxito i18n (creación): {0} = días solicitados descontados.
        addMessage(XavaResources.getString("licencia_guardada_ok", solicitados));
    }

    /**
     * Valida diferencias entre días solicitados y días disponibles (restantes) solo en creación.
     *
     * Reglas:
     * - Si solicitados > restantes → ERROR (no guardar), habilitar edición del campo.
     * - Si solicitados != restantes y el campo es editable → autoajuste + WARNING (no guardar).
     * - Si coinciden (o el campo no es editable) → continuar (true).
     *
     * @return true si puede continuar el guardado; false si se emitió error/warning y se debe frenar.
     */
    private boolean validarDias(boolean editable, Integer diasSolicitados, Integer diasRestantes) throws Exception {
        int solicitados = diasSolicitados != null ? diasSolicitados : 0;
        int restantes   = diasRestantes   != null ? diasRestantes   : 0;

        // diff > 0 significa que el usuario está pidiendo más días que los disponibles.
        int diff = solicitados - restantes;

        // Caso 1: Exceso → error y detener guardado.
        if (diff > 0) {
            // Habilitar edición para que el usuario pueda corregir.
            getView().setEditable("licencias.diasRestantes", true);

            // Mensaje i18n unificado con placeholders:
            // {0}=restantes, {1}=solicitados, {2}=acción (texto corto contextual).
            addError(
                "dias_restantes_distinto_solicitados",
                restantes,
                solicitados,
                "Ajuste manualmente los días disponibles"
            );
            return false;
        }

        // Caso 2: No excede, pero difiere y es editable → autoajuste y warning (usuario confirma guardando).
        if (editable && diff != 0) {
            getView().setValue("licencias.diasRestantes", solicitados); // Ajuste automático
            addWarning(
                "dias_restantes_distinto_solicitados",
                restantes,
                solicitados,
                "Se ajustó automáticamente"
            );
            return false;
        }

        // Caso 3: Coincide o no editable → continuar normalmente.
        return true;
    }
} 
