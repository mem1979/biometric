package com.sta.biometric.validadores;

import org.openxava.jpa.XPersistence;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

import javax.persistence.Query;

/**
 * Validador de entidad que verifica que el userId sea único en la tabla
 * Personal.
 * 
 * <p>
 * Se ejecuta antes de guardar para mostrar un mensaje amigable
 * en lugar del error críptico de la base de datos.
 * </p>
 * 
 * <p>
 * Uso: @EntityValidator en Personal.java
 * </p>
 */
public class ValidadorUserIdUnico implements IValidator {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String id; // ID del registro actual (para excluirlo de la búsqueda al actualizar)

    @Override
    public void validate(Messages errors) throws Exception {
        if (userId == null || userId.trim().isEmpty()) {
            return; // El @Required se encargará de validar si está vacío
        }

        String userIdTrimmed = userId.trim();

        // Consultar si existe otro Personal con el mismo userId
        String jpql = "SELECT COUNT(p) FROM Personal p WHERE p.userId = :userId";
        if (id != null && !id.isEmpty()) {
            jpql += " AND p.id != :id";
        }

        Query query = XPersistence.getManager().createQuery(jpql);
        query.setParameter("userId", userIdTrimmed);
        if (id != null && !id.isEmpty()) {
            query.setParameter("id", id);
        }

        Long count = (Long) query.getSingleResult();

        if (count > 0) {
            errors.add("userId_duplicado", userIdTrimmed);
        }
    }

    // Setters requeridos por OpenXava para inyectar los valores
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }
}
