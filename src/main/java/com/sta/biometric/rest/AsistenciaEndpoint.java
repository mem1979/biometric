/*
 * ─────────────────────────────────────────────────────────────
 /*
 * -----------------------------------------------------------
 *  AsistenciaEndpoint   (Java 11 compatible)
 *  -----------------------------------------------------------
 *  • GET  /asistencia/hoy      → ¿Ya fichó ENTRADA hoy?
 *  • POST /asistencia          → Registrar movimiento (ENTRADA, SALIDA…)
 *  
 *  Usa:
 *    ▸ AuditoriaRegistros      (jornada diaria)
 *    ▸ ColeccionRegistros      (registros individuales)
 *    ▸ JWTUtil                 (extrae login del JWT)
 *    ▸ XPersistence            (OpenXava)
 * -----------------------------------------------------------
 */

package com.sta.biometric.rest;

import java.time.*;
import java.util.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.util.*;

@Path("/asistencia")
public class AsistenciaEndpoint {

    /* Prefijo estándar del header Authorization */
    private static final String BEARER = "Bearer ";

    /* ========================================================= */
    /* GET /asistencia/hoy                                        */
    /* ========================================================= */
    /**
     * Devuelve si el empleado ya registró ENTRADA en la fecha actual.
     * --
     * Respuesta ejemplo:
     * {
     *   "fecha": "2024-01-15",
     *   "yaFichoEntrada": true,
     *   "horaEntrada": "08:03"
     * }
     */
    @GET
    @Path("/hoy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verificarEntradaDeHoy(
        @HeaderParam("Authorization") String authHeader
    ) {
        /* 1. Validar token y obtener login */
        String login = extraerLogin(authHeader);
        if (login == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Token inválido").build();
        }
        /* 2. Obtener empleado */
        Personal empleado = obtenerEmpleado(login);
        if (empleado == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Empleado no encontrado").build();
        }
        /* 3. Consultar en ColeccionRegistros si existe ENTRADA hoy */
        LocalDate hoy = LocalDate.now();
        String qCount =
              "SELECT COUNT(r) " +
              "FROM ColeccionRegistros r " +
              "WHERE r.asistenciaDiaria.empleado = :emp " +
              "AND   r.fecha                   = :hoy " +
              "AND   r.tipoMovimiento          = :tipo";
        boolean yaFicho = XPersistence.getManager()
            .createQuery(qCount, Long.class)
            .setParameter("emp",  empleado)
            .setParameter("hoy",  hoy)
            .setParameter("tipo", TipoMovimiento.ENTRADA)
            .getSingleResult() > 0;
        Map<String, Object> out = new HashMap<>();
        out.put("fecha", TiempoUtils.formatearFecha(hoy));
        out.put("yaFichoEntrada", yaFicho);
        /* 4. Si fichó, recuperar la hora de la primera ENTRADA */
        if (yaFicho) {
            try {
                String qHora =
                      "SELECT r.hora " +
                      "FROM ColeccionRegistros r " +
                      "WHERE r.asistenciaDiaria.empleado = :emp " +
                      "AND   r.fecha                   = :hoy " +
                      "AND   r.tipoMovimiento          = :tipo";
                LocalTime hora = XPersistence.getManager()
                    .createQuery(qHora, LocalTime.class)
                    .setParameter("emp",  empleado)
                    .setParameter("hoy",  hoy)
                    .setParameter("tipo", TipoMovimiento.ENTRADA)
                    .setMaxResults(1)
                    .getSingleResult();
                out.put("horaEntrada", TiempoUtils.formatearHora(hora));
            } catch (NoResultException ignore) {
                // No debería ocurrir, pero prevenimos fallos
            }/* 4.5. Verificar si ya fichó SALIDA */
            String qCountSalida =
            	      "SELECT COUNT(r) FROM ColeccionRegistros r " +
            	      "WHERE r.asistenciaDiaria.empleado = :emp " +
            	      "AND r.fecha = :hoy AND r.tipoMovimiento = :tipo";
            	boolean yaFichoSalida = XPersistence.getManager()
            	    .createQuery(qCountSalida, Long.class)
            	    .setParameter("emp", empleado)
            	    .setParameter("hoy", hoy)
            	    .setParameter("tipo", TipoMovimiento.SALIDA)
            	    .getSingleResult() > 0;
            	out.put("yaFichoSalida", yaFichoSalida);
            	if (yaFichoSalida) {
            	    try {
            	        LocalTime horaSalida = XPersistence.getManager()
            	            .createQuery("SELECT r.hora FROM ColeccionRegistros r " +
            	                        "WHERE r.asistenciaDiaria.empleado = :emp " +
            	                        "AND r.fecha = :hoy AND r.tipoMovimiento = :tipo",
            	                        LocalTime.class)
            	            .setParameter("emp", empleado)
            	            .setParameter("hoy", hoy)
            	            .setParameter("tipo", TipoMovimiento.SALIDA)
            	            .setMaxResults(1)
            	            .getSingleResult();
            	        out.put("horaSalida", TiempoUtils.formatearHora(horaSalida));
            	    } catch (NoResultException ignore) {
            	        out.put("horaSalida", null);
            	    }
            	} else {
            	    out.put("horaSalida", null);
            	}
        }
        /* 5. Verificar si tiene licencia hoy */
        boolean tieneLicencia = Licencia.tieneLicenciaEnFecha(empleado, hoy);
        out.put("tieneLicencia", tieneLicencia);
        
        if (tieneLicencia) {
            try {
                Licencia licenciaHoy = XPersistence.getManager()
                    .createQuery(
                        "SELECT l FROM Licencia l " +
                        "WHERE l.empleado = :emp " +
                        "AND :fecha BETWEEN l.fechaInicio AND l.fechaFin",
                        Licencia.class)
                    .setParameter("emp", empleado)
                    .setParameter("fecha", hoy)
                    .setMaxResults(1)
                    .getSingleResult();
                
                out.put("tipoLicencia", licenciaHoy.getTipo().name());
                out.put("descripcionLicencia", licenciaHoy.getTipo().getDescripcion());
            } catch (NoResultException e) {
                out.put("tipoLicencia", null);
                out.put("descripcionLicencia", null);
            }
        } else {
            out.put("tipoLicencia", null);
            out.put("descripcionLicencia", null);
        }
        /* 6. Verificar si es feriado */
        boolean esFeriado = Feriados.existeParaFecha(hoy);
        out.put("esFeriado", esFeriado);
        
        if (esFeriado) {
            try {
                Feriados feriadoHoy = XPersistence.getManager()
                    .createQuery("SELECT f FROM Feriados f WHERE f.fecha = :fecha", Feriados.class)
                    .setParameter("fecha", hoy)
                    .setMaxResults(1)
                    .getSingleResult();
                
                out.put("descripcionFeriado", feriadoHoy.getMotivo());
            } catch (NoResultException e) {
                out.put("descripcionFeriado", null);
            }
        } else {
            out.put("descripcionFeriado", null);
        }
        return Response.ok(out).build();
    }

    /* ========================================================= */
    /* POST /asistencia                                           */
    /* ========================================================= */
    /**
     * Registra un movimiento proveniente de la app móvil.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarAsistencia(
        MovimientoRequest body,
        @HeaderParam("Authorization") String authHeader,
        @HeaderParam("X-Device-ID")   String deviceId
    ) {
        /* 1. Validar token y obtener login */
        String login = extraerLogin(authHeader);
        if (login == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Token inválido").build();
        }

        /* 2. Obtener empleado */
        Personal empleado = obtenerEmpleado(login);
        if (empleado == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Empleado no encontrado").build();
        }

        /* 3. Validar Device-ID */
        if (deviceId == null || deviceId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Device-ID faltante").build();
        }
        if (!deviceId.equals(empleado.getDeviceId())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Dispositivo no autorizado").build();
        }

        // Fecha y hora oficiales del servidor (no del dispositivo móvil)
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        /* 4. Obtener o crear la Auditoría del día */
        AuditoriaRegistros dia = XPersistence.getManager()
            .createQuery(
                "FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                AuditoriaRegistros.class
            )
            .setParameter("emp",   empleado)
            .setParameter("fecha", hoy)
            .getResultStream()
            .findFirst()
            .orElseGet(() -> {
                AuditoriaRegistros nuevo = new AuditoriaRegistros();
                nuevo.setEmpleado(empleado);
                nuevo.setFecha(hoy);
                XPersistence.getManager().persist(nuevo);
                return nuevo;
            });

        /* 5. Crear y configurar ColeccionRegistros */
        ColeccionRegistros reg = new ColeccionRegistros();
        reg.setFecha(hoy);
        reg.setHora(ahora);
        reg.setCoordenada(body.getUbicacion());
        reg.setObservacion("App");

        /* 5.1 Deducir tipo de movimiento si no viene explícito */
        TipoMovimiento tipo = body.getTipoMovimiento() != null
            ? body.getTipoMovimiento()
            : InterpreteFichadasService.deducirTipoMovimiento(body.getDescripcionTipo());
        reg.setTipoMovimiento(tipo);

        /* 5.2 Asociar a la auditoría */
        reg.setAsistenciaDiaria(dia);
        dia.getRegistros().add(reg);   // relación bidireccional

        /* 6. Consolidar */
        dia.consolidarDesdeRegistros();

        /* 7. Responder con datos pensados para la app móvil */
        Map<String, Object> resp = new HashMap<>();
        resp.put("estado", "ok");

        // Fecha y hora del SERVIDOR
        resp.put("fecha", TiempoUtils.formatearFecha(hoy));
        resp.put("hora",  TiempoUtils.formatearHora(ahora));

        // Tipo de movimiento (ENTRADA / SALIDA / etc.)
        resp.put("tipo", tipo != null ? tipo.name() : "NO_RECONOCIDO");

        // Datos del empleado para mostrar en la app
        resp.put("nombreCompleto", empleado.getNombreCompleto());
        resp.put("turnoActivoHoy", empleado.getTurnoActivoHoy());

        // Mensaje amigable para la app según el tipo de movimiento
        String mensaje;
        if (tipo == TipoMovimiento.ENTRADA) {
            mensaje = "Se registró correctamente el INICIO de la jornada.";
        } else if (tipo == TipoMovimiento.SALIDA) {
            mensaje = "Se registró correctamente el FIN de la jornada.";
        } else {
            mensaje = "Movimiento registrado.";
        }
        resp.put("mensaje", mensaje);

        return Response.ok(resp).build();
    }

    /* ========================================================= */
    /* Utils privados                                             */
    /* ========================================================= */

    /** Devuelve login si el header contiene un JWT válido; si no, null. */
    private String extraerLogin(String header) {
        if (header == null || !header.startsWith(BEARER)) return null;
        String token = header.substring(BEARER.length());
        return JWTUtil.validarTokenYObtenerUsuario(token);
    }

    /** Busca el empleado por login. */
    private Personal obtenerEmpleado(String login) {
        return XPersistence.getManager()
            .createQuery("FROM Personal p WHERE p.usuario = :u", Personal.class)
            .setParameter("u", login)
            .getResultStream()
            .findFirst()
            .orElse(null);
    }

    /* ========================================================= */
    /* DTO MovimientoRequest                                      */
    /* ========================================================= */
    public static class MovimientoRequest {
        private LocalDate      fecha;
        private LocalTime      hora;
        private String         descripcionTipo;
        private TipoMovimiento tipoMovimiento;
        private String         ubicacion;

        /* Getters y setters (requeridos por Jackson) */
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }

        public LocalTime getHora() { return hora; }
        public void setHora(LocalTime hora) { this.hora = hora; }

        public String getDescripcionTipo() { return descripcionTipo; }
        public void setDescripcionTipo(String descripcionTipo) { this.descripcionTipo = descripcionTipo; }

        public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
        public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    }
}
