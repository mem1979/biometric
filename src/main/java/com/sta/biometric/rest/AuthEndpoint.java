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
 *
 *  - POST /auth/login         → Login con usuario, contraseña y deviceId
 *  - POST /auth/cambiarClave  → Cambiar contraseña usando token JWT
 */
@Path("/auth")
public class AuthEndpoint {

    /* ---------- LOGIN ---------- */
	@POST @Path("/login")
	public Response login(@FormParam("usuario") String usuario,
	                      @FormParam("contrasena") String contrasena,
	                      @HeaderParam("X-Device-ID") String deviceId) {

	    if (deviceId == null || deviceId.isBlank())
	        return error(Response.Status.BAD_REQUEST, "Falta X-Device-ID");

	    /* Autenticamos usuario + contraseña */
	    Personal p = buscarEmpleado(usuario, contrasena);
	    if (p == null)
	        return error(Response.Status.UNAUTHORIZED, "Credenciales inválidas");

	    /* Verificamos / registramos el dispositivo */
	    if (p.getDeviceId() == null || p.getDeviceId().isBlank()) {
	        // Primer login desde un dispositivo nuevo lo asociamos
	        p.setDeviceId(deviceId);
	        XPersistence.getManager().merge(p);
	    } else if (!deviceId.equals(p.getDeviceId())) {
	        // Ya tenía uno distinto lo rechazamos
	        return error(Response.Status.UNAUTHORIZED,
	                     "Dispositivo no autorizado para este usuario");
	    }

	    /* Generamos token y respuesta */
	    // Contraseña por defecto: 1234  (ajustado a tu requerimiento)
	    boolean passPorDefecto = "1234".equals(contrasena);
	    String token = JWTUtil.generarToken(usuario);

	    return Response.ok(Map.of(
	            "token",           token,
	            "usuario",         usuario,
	            "deviceId",        deviceId,
	            "passwordDefault", passPorDefecto))
	         .build();
	}

    /* ---------- CAMBIAR CLAVE ---------- */
    @POST @Path("/cambiarClave")
    public Response changePassword(@HeaderParam("Authorization") String authHeader,
                                   @FormParam("nueva") String nuevaClave) {

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return error(Response.Status.UNAUTHORIZED, "Falta token");

        String usuario = JWTUtil.validarTokenYObtenerUsuario(authHeader.substring(7));
        if (usuario == null)
            return error(Response.Status.UNAUTHORIZED, "Token inválido");

        if (nuevaClave == null || nuevaClave.length() < 8)
            return error(Response.Status.BAD_REQUEST, "La clave debe tener al menos 8 caracteres");

        /*   Buscamos por login (no por PK) */
        Personal p = buscarEmpleadoSoloPorUsuario(usuario);
        if (p == null)
            return error(Response.Status.NOT_FOUND, "Empleado no encontrado");

        p.setContrasena(nuevaClave);
        XPersistence.getManager().merge(p);

        return Response.ok(Map.of("success", true)).build();
    }
    
    /* ========================================================= */
    /* GET /auth/me                                              */
    /* ========================================================= */
    /**
     * Devuelve información básica del empleado autenticado.
     * No devuelve deviceId, por pedido de Marcelo.
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@HeaderParam("Authorization") String authHeader) {

        // 1. Validar header Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Falta token"))
                    .build();
        }

        // 2. Obtener usuario desde el JWT
        String token = authHeader.substring("Bearer ".length());
        String usuario = JWTUtil.validarTokenYObtenerUsuario(token);

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token inválido"))
                    .build();
        }

        // 3. Buscar empleado por login
        Personal p = buscarEmpleadoSoloPorUsuario(usuario);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Empleado no encontrado"))
                    .build();
        }

        // 4. Construir respuesta ME para datos básicos
        return Response.ok(Map.of(
                "usuario",        p.getUsuario(),
                "nombreCompleto", p.getNombreCompleto(),
                "turnoActivoHoy", p.getTurnoActivoHoy(),
                "aceptaPausa",    p.isAceptaPausa()  // NUEVO
        )).build();
    }

    /* ----------------------- Métodos auxiliares ----------------------- */

    /** Busca al empleado por usuario y contraseña en texto plano. */
    private Personal buscarEmpleado(String usuario, String contrasena) {
        try {
            return XPersistence.getManager()
                    .createQuery(
                        "FROM Personal e WHERE e.usuario = :u AND e.contrasena = :c",
                        Personal.class)
                    .setParameter("u", usuario)
                    .setParameter("c", contrasena)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** Busca al empleado solo por usuario (login). */
    private Personal buscarEmpleadoSoloPorUsuario(String usuario) {
        try {
            TypedQuery<Personal> q = XPersistence.getManager()
                    .createQuery("FROM Personal e WHERE e.usuario = :u", Personal.class);
            return q.setParameter("u", usuario).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** Devuelve respuesta JSON con un mensaje de error. */
    private Response error(Response.Status status, String mensaje) {
        return Response.status(status)
                       .entity(Map.of("error", mensaje))
                       .build();
    }
}
