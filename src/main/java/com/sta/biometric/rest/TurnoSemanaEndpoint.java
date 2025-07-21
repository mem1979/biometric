package com.sta.biometric.rest;

import java.time.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

/**
 *  GET /turno/semana
 *  -----------------
 *  Responde la planificación de turnos para los próximos 7 días.
 *
 *  Ejemplo:
 *  {
 *    "desde" : "2025-07-21",
 *    "hasta" : "2025-07-27",
 *    "dias"  : {
 *      "2025-07-21": { "laboral":true,  "descripcion":"Turno Mañana","horaInicio":"08:00","horaFin":"16:00" },
 *      "2025-07-22": { "laboral":false, "descripcion":"Día libre"                                 },
 *      
 *    }
 *  }
 */
@Path("/turno/semana")
public class TurnoSemanaEndpoint {

    private static final String BEARER = "Bearer ";


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response turnoSemana(
        @HeaderParam("Authorization") String authHeader
    ) {

        /*  Validar token y obtener login ------------------------ */
        String login = extraerLogin(authHeader);
        if (login == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Token inválido").build();
        }

        /*  Obtener empleado ------------------------------------ */
        Personal emp = XPersistence.getManager()
            .createQuery("FROM Personal p WHERE p.usuario = :u", Personal.class)
            .setParameter("u", login)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (emp == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Empleado no encontrado").build();
        }

        /*  Iterar próximos 7 días ------------------------------ */
        LocalDate hoy   = LocalDate.now();
        LocalDate hasta = hoy.plusDays(6);  // total 7 días

        Map<String, Map<String, Object>> dias = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.plusDays(i);
            TurnosHorarios turno = emp.getTurnoParaFecha(fecha);

            Map<String, Object> info = new HashMap<>();
            if (turno == null) {
                info.put("laboral", false);
                info.put("descripcion", "Sin turno");
            } else {
                DayOfWeek d = fecha.getDayOfWeek();
                boolean laboral = turno.esLaboral(d);

                info.put("laboral", laboral);
                info.put("descripcion", turno.getDetalleJornadaHoras());
                if (laboral) {
                    LocalTime ini = turno.getEntradaParaDia(d);
                    LocalTime fin = turno.getSalidaParaDia(d);
                    if (ini != null) info.put("horaInicio", ini.toString());
                    if (fin != null) info.put("horaFin",    fin.toString());
                }
            }
            dias.put(fecha.toString(), info);
        }

        /*  Respuesta ------------------------------------------- */
        Map<String, Object> resp = new HashMap<>();
        resp.put("desde", hoy.toString());
        resp.put("hasta", hasta.toString());
        resp.put("dias",  dias);

        return Response.ok(resp).build();
    }

    /* Helper para extraer login del JWT */
    private String extraerLogin(String header) {
        if (header == null || !header.startsWith(BEARER)) return null;
        return JWTUtil.validarTokenYObtenerUsuario(header.substring(BEARER.length()));
    }
}

