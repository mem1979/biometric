package com.sta.biometric.rest;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

@Path("/auth")
public class AuthEndpoint {

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("usuario") String usuario, @FormParam("contrasena") String contrasena) {
        try {
            Personal empleado = XPersistence.getManager()
            	.createQuery("FROM Personal e WHERE e.usuario = :usuario AND e.contrasena = :contrasena", Personal.class)
            	.setParameter("usuario", usuario)
            	.setParameter("contrasena", contrasena)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (empleado == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales inválidas").build();
            }

            String token = JWTUtil.generarToken(usuario);
            return Response.ok(Map.of("token", token)).build();

        } catch (Exception e) {
            e.printStackTrace(); // Ãºtil para debugging
            return Response.serverError().entity("Error al autenticar").build();
        }
    }
}
