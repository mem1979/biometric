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
    /* GET /asistencia/hoy */
    /* ========================================================= */
    /**
     * Devuelve si el empleado ya registró ENTRADA en la fecha actual.
     * --
     * Respuesta ejemplo:
     * {
     * "fecha": "2024-01-15",
     * "yaFichoEntrada": true,
     * "horaEntrada": "08:03"
     * }
     */
    @GET
    @Path("/hoy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verificarEntradaDeHoy(
            @HeaderParam("Authorization") String authHeader) {
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
        LocalDate ayer = hoy.minusDays(1);
        Map<String, Object> out = new HashMap<>();
        out.put("fecha", TiempoUtils.formatearFecha(hoy));

        /* === SOPORTE JORNADAS NOCTURNAS === */
        /* Verificar si hay jornada nocturna abierta del día anterior */
        try {
            AuditoriaRegistros jornadaNocturnaAbierta = XPersistence.getManager()
                    .createQuery(
                            "SELECT a FROM AuditoriaRegistros a " +
                                    "WHERE a.empleado = :emp " +
                                    "AND a.fecha = :ayer " +
                                    "AND a.esJornadaNocturna = true " +
                                    "AND a.evaluacion = :estado",
                            AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("ayer", ayer)
                    .setParameter("estado", EvaluacionJornada.EN_CURSO)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (jornadaNocturnaAbierta != null) {
                // Hay jornada nocturna abierta del día anterior
                out.put("jornadaNocturnaAbierta", true);
                out.put("fechaJornadaNocturna", TiempoUtils.formatearFecha(ayer));
                out.put("yaFichoEntrada", true); // Ya tiene entrada de ayer
                out.put("yaFichoSalida", false); // Falta la salida

                // Obtener hora de entrada de ayer
                ColeccionRegistros entradaAyer = jornadaNocturnaAbierta.getRegistros().stream()
                        .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                        .findFirst()
                        .orElse(null);
                if (entradaAyer != null) {
                    out.put("horaEntrada", TiempoUtils.formatearHora(entradaAyer.getHora()));
                }
                out.put("horaSalida", null);
                out.put("mensajeNocturno", "Jornada nocturna iniciada ayer. Registre su SALIDA.");

                // Agregar info de licencia y feriado de hoy
                out.put("tieneLicencia", Licencia.tieneLicenciaEnFecha(empleado, hoy));
                out.put("esFeriado", Feriados.existeParaFecha(hoy));

                return Response.ok(out).build();
            }
        } catch (Exception e) {
            // Si hay error, continuar con lógica normal
            System.err.println("[AsistenciaEndpoint] Error buscando jornada nocturna: " + e.getMessage());
        }
        out.put("jornadaNocturnaAbierta", false);
        /* === FIN SOPORTE NOCTURNAS === */

        String qCount = "SELECT COUNT(r) " +
                "FROM ColeccionRegistros r " +
                "WHERE r.asistenciaDiaria.empleado = :emp " +
                "AND   r.fecha                   = :hoy " +
                "AND   r.tipoMovimiento          = :tipo";
        boolean yaFicho = XPersistence.getManager()
                .createQuery(qCount, Long.class)
                .setParameter("emp", empleado)
                .setParameter("hoy", hoy)
                .setParameter("tipo", TipoMovimiento.ENTRADA)
                .getSingleResult() > 0;
        out.put("yaFichoEntrada", yaFicho);
        /* 4. Si fichó, recuperar la hora de la primera ENTRADA */
        if (yaFicho) {
            try {
                String qHora = "SELECT r.hora " +
                        "FROM ColeccionRegistros r " +
                        "WHERE r.asistenciaDiaria.empleado = :emp " +
                        "AND   r.fecha                   = :hoy " +
                        "AND   r.tipoMovimiento          = :tipo";
                LocalTime hora = XPersistence.getManager()
                        .createQuery(qHora, LocalTime.class)
                        .setParameter("emp", empleado)
                        .setParameter("hoy", hoy)
                        .setParameter("tipo", TipoMovimiento.ENTRADA)
                        .setMaxResults(1)
                        .getSingleResult();
                out.put("horaEntrada", TiempoUtils.formatearHora(hora));
            } catch (NoResultException ignore) {
                // No debería ocurrir, pero prevenimos fallos
            } /* 4.5. Verificar si ya fichó SALIDA */
            String qCountSalida = "SELECT COUNT(r) FROM ColeccionRegistros r " +
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
    /* POST /asistencia */
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
            @HeaderParam("X-Device-ID") String deviceId) {
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
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        LocalTime ahora = LocalTime.now();

        /* === SOPORTE JORNADAS NOCTURNAS (POST) === */
        /* Si es SALIDA y hay jornada nocturna abierta de ayer, agregar a esa jornada */
        TipoMovimiento tipoSolicitado = body.getTipoMovimiento() != null
                ? body.getTipoMovimiento()
                : InterpreteFichadasService.deducirTipoMovimiento(body.getDescripcionTipo());

        AuditoriaRegistros dia = null;
        boolean esAjusteNocturno = false;

        if (tipoSolicitado == TipoMovimiento.SALIDA) {
            // Buscar jornada nocturna abierta de ayer
            AuditoriaRegistros jornadaNocturnaAbierta = XPersistence.getManager()
                    .createQuery(
                            "SELECT a FROM AuditoriaRegistros a " +
                                    "WHERE a.empleado = :emp " +
                                    "AND a.fecha = :ayer " +
                                    "AND a.esJornadaNocturna = true " +
                                    "AND a.evaluacion = :estado",
                            AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("ayer", ayer)
                    .setParameter("estado", EvaluacionJornada.EN_CURSO)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (jornadaNocturnaAbierta != null) {
                dia = jornadaNocturnaAbierta;
                esAjusteNocturno = true;
                System.out.println("[AsistenciaEndpoint] SALIDA para jornada nocturna de ayer: " +
                        empleado.getNombreCompleto());
            }
        }

        /* 4. Obtener o crear la Auditoría del día (si no es ajuste nocturno) */
        if (dia == null) {
            dia = XPersistence.getManager()
                    .createQuery(
                            "FROM AuditoriaRegistros a WHERE a.empleado = :emp AND a.fecha = :fecha",
                            AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
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
        }
        /* === FIN SOPORTE NOCTURNAS (POST) === */

        /* === VALIDACIÓN DE FICHADAS DUPLICADAS === */
        if (tipoSolicitado == TipoMovimiento.ENTRADA || tipoSolicitado == TipoMovimiento.SALIDA) {
            boolean yaExisteFichada = dia.getRegistros().stream()
                    .anyMatch(r -> r.getTipoMovimiento() == tipoSolicitado);

            if (yaExisteFichada) {
                String tipoStr = tipoSolicitado == TipoMovimiento.ENTRADA ? "ENTRADA" : "SALIDA";
                Map<String, Object> error = new HashMap<>();
                error.put("error", "FICHADA_DUPLICADA");
                error.put("mensaje", "Ya existe un registro de " + tipoStr + " para esta jornada.");
                return Response.status(Response.Status.CONFLICT).entity(error).build();
            }
        }
        /* === FIN VALIDACIÓN DUPLICADAS === */

        /* 5. Crear y configurar ColeccionRegistros */
        ColeccionRegistros reg = new ColeccionRegistros();
        // Para jornadas nocturnas, la fecha del registro SALIDA es HOY aunque la
        // jornada sea de ayer
        reg.setFecha(hoy);
        reg.setHora(ahora);
        reg.setCoordenada(body.getUbicacion());
        String observacion = body.getNota() != null && !body.getNota().isBlank()
                ? body.getNota()
                : (esAjusteNocturno ? "SALIDA jornada nocturna" : "Registro desde App");
        reg.setObservacion(observacion);

        /* 5.1 Usar tipo ya calculado (arriba) */
        TipoMovimiento tipo = tipoSolicitado;
        reg.setTipoMovimiento(tipo);

        /* 5.2 Asociar a la auditoría */
        reg.setAsistenciaDiaria(dia);
        dia.getRegistros().add(reg); // relación bidireccional

        /* 6. Consolidar */
        dia.consolidarDesdeRegistros();

        /* 6.1 Actualizar nota de la jornada si se envió */
        if (body.getNota() != null && !body.getNota().isBlank()) {
            dia.actualizarNotaSegunEvaluacion();
        }

        /* 7. Responder con datos pensados para la app móvil */
        Map<String, Object> resp = new HashMap<>();
        resp.put("estado", "ok");

        // Fecha y hora del SERVIDOR
        resp.put("fecha", TiempoUtils.formatearFecha(hoy));
        resp.put("hora", TiempoUtils.formatearHora(ahora));

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
    /* Utils privados */
    /* ========================================================= */

    /** Devuelve login si el header contiene un JWT válido; si no, null. */
    private String extraerLogin(String header) {
        if (header == null || !header.startsWith(BEARER))
            return null;
        String token = header.substring(BEARER.length());
        return JWTUtil.validarTokenYObtenerUsuario(token);
    }

    /**
     * Busca el empleado por login. Solo devuelve empleados activos y no eliminados.
     */
    private Personal obtenerEmpleado(String login) {
        return XPersistence.getManager()
                .createQuery("FROM Personal p WHERE p.usuario = :u AND p.activo = true AND p.eliminado = false",
                        Personal.class)
                .setParameter("u", login)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /* ========================================================= */
    /* DTO MovimientoRequest */
    /* ========================================================= */
    public static class MovimientoRequest {
        private LocalDate fecha;
        private LocalTime hora;
        private String descripcionTipo;
        private TipoMovimiento tipoMovimiento;
        private String ubicacion;
        private String nota;

        /* Getters y setters (requeridos por Jackson) */
        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public LocalTime getHora() {
            return hora;
        }

        public void setHora(LocalTime hora) {
            this.hora = hora;
        }

        public String getDescripcionTipo() {
            return descripcionTipo;
        }

        public void setDescripcionTipo(String descripcionTipo) {
            this.descripcionTipo = descripcionTipo;
        }

        public TipoMovimiento getTipoMovimiento() {
            return tipoMovimiento;
        }

        public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
            this.tipoMovimiento = tipoMovimiento;
        }

        public String getUbicacion() {
            return ubicacion;
        }

        public void setUbicacion(String ubicacion) {
            this.ubicacion = ubicacion;
        }

        public String getNota() {
            return nota;
        }

        public void setNota(String nota) {
            this.nota = nota;
        }
    }
}
