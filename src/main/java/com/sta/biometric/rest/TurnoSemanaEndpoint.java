package com.sta.biometric.rest;

import java.time.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.util.*;

@Path("/turno/semana")
public class TurnoSemanaEndpoint {
    private static final String BEARER = "Bearer ";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response turnoSemana(@HeaderParam("Authorization") String authHeader) {
        String login = extraerLogin(authHeader);
        if (login == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token invalido").build();
        }

        Personal emp = XPersistence.getManager()
                .createQuery("FROM Personal p WHERE p.usuario = :u AND p.activo = true AND p.eliminado = false",
                        Personal.class)
                .setParameter("u", login)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (emp == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Empleado no encontrado").build();
        }

        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(6);
        Map<String, Map<String, Object>> dias = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.plusDays(i);
            List<TurnosHorarios> turnos = emp.getTurnosParaFecha(fecha);

            Map<String, Object> info = new HashMap<>();
            List<Map<String, Object>> listaTurnos = new ArrayList<>();
            boolean esLaboral = false;

            for (TurnosHorarios turno : turnos) {
                DayOfWeek d = fecha.getDayOfWeek();
                boolean laboralEnEsteTurno = turno.esLaboral(d);

                if (laboralEnEsteTurno) {
                    esLaboral = true;
                    Map<String, Object> turnoInfo = new HashMap<>();

                    // Nombre del turno formateado
                    String nombreTurno = turno.getTurnoNombre() != null ? formatShiftName(turno.getTurnoNombre().name())
                            : "Turno";
                    turnoInfo.put("nombre", nombreTurno);

                    // Horarios
                    LocalTime ini = turno.getEntradaParaDia(d);
                    LocalTime fin = turno.getSalidaParaDia(d);
                    if (ini != null)
                        turnoInfo.put("horaInicio", ini.toString());
                    if (fin != null)
                        turnoInfo.put("horaFin", fin.toString());

                    // Tolerancia en minutos
                    Integer tolerancia = turno.getTolerancia();
                    turnoInfo.put("toleranciaMinutos", tolerancia != null ? tolerancia : 0);

                    // Bonificación porcentual
                    java.math.BigDecimal bonificacion = turno.getPorcentajeBonificacion();
                    turnoInfo.put("bonificacionPorcentaje", bonificacion != null ? bonificacion.doubleValue() : 0.0);

                    // Tipo (para compatibilidad)
                    turnoInfo.put("tipo", listaTurnos.isEmpty() ? "NORMAL" : "EXTRA");

                    // Descripción (para compatibilidad hacia atrás)
                    turnoInfo.put("descripcion", turno.getDetalleJornadaHoras());

                    listaTurnos.add(turnoInfo);
                }
            }

            info.put("laboral", esLaboral);
            info.put("turnos", listaTurnos);

            // Compatibilidad hacia atras
            if (!listaTurnos.isEmpty()) {
                Map<String, Object> primerTurno = listaTurnos.get(0);
                info.put("descripcion", primerTurno.get("descripcion"));
                info.put("horaInicio", primerTurno.get("horaInicio"));
                info.put("horaFin", primerTurno.get("horaFin"));
            } else {
                info.put("descripcion", "Sin turno");
            }

            dias.put(fecha.toString(), info);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("desde", hoy.toString());
        resp.put("hasta", hasta.toString());
        resp.put("dias", dias);

        return Response.ok(resp).build();
    }

    /**
     * Formatea el nombre del turno del enum a texto amigable en español
     */
    private String formatShiftName(String enumName) {
        switch (enumName) {
            case "MANANA":
                return "Mañana";
            case "TARDE":
                return "Tarde";
            case "NOCHE":
                return "Noche";
            case "ESPECIAL":
                return "Especial";
            default:
                return enumName;
        }
    }

    private String extraerLogin(String header) {
        if (header == null || !header.startsWith(BEARER))
            return null;
        return JWTUtil.validarTokenYObtenerUsuario(header.substring(BEARER.length()));
    }
}