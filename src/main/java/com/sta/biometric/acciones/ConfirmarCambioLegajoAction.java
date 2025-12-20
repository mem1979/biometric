package com.sta.biometric.acciones;

import javax.persistence.Query;

import org.openxava.actions.ViewBaseAction;
import org.openxava.jpa.XPersistence;

import com.sta.biometric.modelo.Personal;

/**
 * Acción que confirma el cambio de legajo, valida y guarda.
 */
public class ConfirmarCambioLegajoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        String nuevoLegajo = (String) getView().getValue("nuevoLegajo");

        // Recuperar datos del contexto
        String personalId = (String) getContext().get(getRequest(), "cambiarLegajo_personalId");
        String legajoActual = (String) getContext().get(getRequest(), "cambiarLegajo_legajoActual");

        // Validar que tengamos los datos necesarios
        if (personalId == null || personalId.isEmpty()) {
            addError("No se pudo recuperar la información del empleado. Intente nuevamente.");
            closeDialog();
            return;
        }

        // Validar que no esté vacío
        if (nuevoLegajo == null || nuevoLegajo.trim().isEmpty()) {
            addError("Debe ingresar un legajo.");
            return;
        }

        nuevoLegajo = nuevoLegajo.trim();

        // Validar longitud máxima (10 caracteres)
        if (nuevoLegajo.length() > 10) {
            addError("El legajo no puede tener más de 10 caracteres.");
            return;
        }

        // Validar que no sea igual al actual
        if (nuevoLegajo.equals(legajoActual)) {
            addWarning("El legajo ingresado es igual al actual.");
            closeDialog();
            return;
        }

        // Validar que no exista en otro empleado
        if (existeLegajo(nuevoLegajo, personalId)) {
            addError("El legajo '" + nuevoLegajo + "' ya está en uso por otro empleado.");
            return;
        }

        // Actualizar la entidad directamente
        Personal empleado = XPersistence.getManager().find(Personal.class, personalId);
        if (empleado == null) {
            addError("No se encontró el empleado.");
            closeDialog();
            return;
        }

        empleado.setUserId(nuevoLegajo);
        XPersistence.commit();

        closeDialog();
        getView().refresh();

        addMessage("Legajo cambiado exitosamente a '" + nuevoLegajo + "'.");
    }

    /**
     * Verifica si el legajo ya existe en otro empleado.
     */
    private boolean existeLegajo(String userId, String excludeId) {
        String jpql = "SELECT COUNT(p) FROM Personal p WHERE p.userId = :userId AND p.id != :id";
        Query query = XPersistence.getManager().createQuery(jpql);
        query.setParameter("userId", userId);
        query.setParameter("id", excludeId);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
}
