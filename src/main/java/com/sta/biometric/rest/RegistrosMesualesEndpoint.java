package com.sta.biometric.rest;

import java.time.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

/**
 *  GET /asistencia/mes/{anio}/{mes}
 *  Ej.: /asistencia/mes/2025/07
 */
@Path("/asistencia/mes")
public class RegistrosMesualesEndpoint {

    private static final String BEARER = "Bearer ";

    @GET
    @Path("/{anio}/{mes}")                 //  path params
    @Produces(MediaType.APPLICATION_JSON)
    public Response historialMensual(
        @PathParam("anio")  int anio,
        @PathParam("mes")   int mes,
        @HeaderParam("Authorization") String authHeader
    ) {
        /* Validar JWT  obtener login */
        String login = extraerLogin(authHeader);
        if (login == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();

        /* Obtener empleado */
        Personal emp = XPersistence.getManager()
            .createQuery("FROM Personal p WHERE p.usuario = :u", Personal.class)
            .setParameter("u", login)
            .getResultStream()
            .findFirst()
            .orElse(null);
        if (emp == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Empleado no encontrado").build();

        /* Rango de fechas del mes */
        LocalDate ini = LocalDate.of(anio, mes, 1);
        LocalDate fin = ini.withDayOfMonth(ini.lengthOfMonth());

        /* Traer registros */
        String jpql =
              "FROM ColeccionRegistros r " +
              "WHERE r.asistenciaDiaria.empleado = :emp " +
              "AND   r.fecha BETWEEN :ini AND :fin " +
              "ORDER BY r.fecha, r.hora";
        List<ColeccionRegistros> lista = XPersistence.getManager()
            .createQuery(jpql, ColeccionRegistros.class)
            .setParameter("emp", emp)
            .setParameter("ini", ini)
            .setParameter("fin", fin)
            .getResultList();

        /* Agrupar por día */
        Map<String,List<Map<String,Object>>> dias = new HashMap<>();
        for (ColeccionRegistros r : lista) {
            String key = r.getFecha().toString();                  // yyyy-MM-dd
            Map<String,Object> dto = new HashMap<>();
            dto.put("hora", TiempoUtils.formatearHora(r.getHora()));
            dto.put("tipo", r.getTipoMovimiento() != null ? r.getTipoMovimiento().name()
                                                         : TipoMovimiento.UBICACION.name());
            dias.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
        }

        /* Respuesta */
        Map<String,Object> resp = new HashMap<>();
        resp.put("empleado", emp.getUsuario());
        resp.put("mes", String.format("%04d-%02d", anio, mes));
        resp.put("dias", dias);

        return Response.ok(resp).build();
    }

    /* -------- Helpers -------- */
    private String extraerLogin(String header) {
        if (header == null || !header.startsWith(BEARER)) return null;
        return JWTUtil.validarTokenYObtenerUsuario(header.substring(BEARER.length()));
    }
}

