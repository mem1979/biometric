package com.sta.biometric.rest;

import java.util.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

/**
 * Endpoint de autenticación biométrica.
 * <p>
 * Flujo:
 * <ol>
 *   <li>Busca al usuario por login y contraseña.</li>
 *   <li>Si no tiene dispositivo asociado, registra el recibido en la cabecera {@code X-Device-ID}.</li>
 *   <li>Si ya tiene dispositivo y difiere, devuelve 401 con mensaje claro.</li>
 *   <li>Si todo es correcto, emite token JWT.</li>
 * </ol>
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class AuthEndpoint {

    @POST
    @Path("/login")
    public Response login(@FormParam("usuario") String usuario,
                          @FormParam("contrasena") String contrasena,
                          @HeaderParam("X-Device-ID") String deviceId) {

        // Validaciones básicas de entrada
        if (usuario == null || contrasena == null || deviceId == null || deviceId.isBlank()) {
            return error(Response.Status.BAD_REQUEST,
                         "Se requieren usuario, contraseña y encabezado X-Device-ID.");
        }

        try {
            Personal empleado = buscarEmpleado(usuario, contrasena);
            if (empleado == null) {
                return error(Response.Status.UNAUTHORIZED, "Credenciales inválidas.");
            }

            // --- Verificación / registro de dispositivo ---
            if (empleado.getDeviceId() == null || empleado.getDeviceId().isBlank()) {
                // Primera vez: asociamos este deviceId y persistimos
                empleado.setDeviceId(deviceId);
                XPersistence.getManager().merge(empleado);
            } else if (!empleado.getDeviceId().equals(deviceId)) {
                return error(Response.Status.UNAUTHORIZED,
                        "Dispositivo no autorizado. Este usuario ya está vinculado al dispositivo "
                        + empleado.getDeviceId() + ".");
            }

            // --- Generar token y responder ---
            String token = JWTUtil.generarToken(usuario);
            return Response.ok(Map.of("token", token,
                                      "usuario", usuario,
                                      "deviceId", deviceId))
                           .build();

        } catch (Exception e) {
            e.printStackTrace(); //  Log framework en producción
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error al autenticar.");
        }
    }

    /* ----------------------- Métodos auxiliares ----------------------- */

    /**
     * Busca al empleado por usuario y contraseña.
     * Se mantiene la firma en texto plano porque así lo solicita el proyecto,
     * pero en producción debería compararse una contraseña hasheada.
     */
    private Personal buscarEmpleado(String usuario, String contrasena) {
        try {
            return XPersistence.getManager()
                    .createQuery(
                        "FROM Personal e " +
                        "WHERE e.usuario = :usuario AND e.contrasena = :contrasena",
                        Personal.class)
                    .setParameter("usuario", usuario)
                    .setParameter("contrasena", contrasena)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** Devuelve un objeto JSON con una clave {@code error} y el mensaje proporcionado. */
    private Response error(Response.Status status, String mensaje) {
        return Response.status(status)
                       .entity(Map.of("error", mensaje))
                       .build();
    }
}
