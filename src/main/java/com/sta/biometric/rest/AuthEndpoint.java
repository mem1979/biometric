package com.sta.biometric.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

@Path("/auth")
public class AuthEndpoint {

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("usuario") String usuario, @FormParam("contrasenia") String contrasenia) {
        try {
            Personal empleado = XPersistence.getManager()
                .createQuery("FROM Empleado e WHERE e.usuario = :usuario AND e.contrasena = :contrasena", Personal.class)
                .setParameter("usuario", usuario)
                .setParameter("contrasena", contrasenia)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (empleado == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales inválidas").build();
            }

            String token = JWTUtil.generarToken(usuario);
            return Response.ok(token).build();

        } catch (Exception e) {
            e.printStackTrace(); // útil para debugging
            return Response.serverError().entity("Error al autenticar").build();
        }
    }
}
