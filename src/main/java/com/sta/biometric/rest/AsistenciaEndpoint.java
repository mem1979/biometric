package com.sta.biometric.rest;
import java.time.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.openxava.jpa.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;
import com.sta.biometric.util.*;

@Path("/asistencia")
public class AsistenciaEndpoint {

    private static final String BEARER_PREFIX = "Bearer ";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarAsistencia(
        MovimientoRequest entrada,
        @HeaderParam("Authorization") String authHeader,
        @HeaderParam("X-Device-ID") String deviceId
    ) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Token no proporcionado o mal formado").build();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        String usuario = JWTUtil.validarTokenYObtenerUsuario(token);

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Token inválido o expirado").build();
        }

        Personal empleado = XPersistence.getManager()
            .createQuery("FROM Personal e WHERE e.usuario = :usuario", Personal.class)
            .setParameter("usuario", usuario)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (empleado == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Empleado no encontrado").build();
        }

        if (deviceId == null || deviceId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Device ID no proporcionado").build();
        }

        if (!deviceId.equals(empleado.getDeviceId())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Este dispositivo no está autorizado para este usuario").build();
        }

        // === Crear registro embebido ===
        ColeccionRegistros r = new ColeccionRegistros();
        r.setFecha(entrada.getFecha());
        r.setHora(entrada.getHora());
        r.setCoordenada(entrada.getUbicacion());
        r.setObservacion("App");

        TipoMovimiento tipo = entrada.getTipoMovimiento() != null
            ? entrada.getTipoMovimiento()
            : InterpreteFichadasService.deducirTipoMovimiento(entrada.getDescripcionTipo());

        if (tipo == null && entrada.getDescripcionTipo() != null) {
            r.setObservacion("Tipo no reconocido: " + entrada.getDescripcionTipo());
        }

        r.setTipoMovimiento(tipo);

        // === Consolidar ===
        AsistenciaDiariaService.consolidarDia(empleado, entrada.getFecha(), entrada.getHora(), List.of(r));
        return Response.ok(Map.of(
            "estado", "ok",
            "fecha", TiempoUtils.formatearFecha(entrada.getFecha()),
            "hora", TiempoUtils.formatearHora(entrada.getHora()),
            "tipo", tipo != null ? tipo.name() : "NO_RECONOCIDO"
        )).build();
    }

    // DTO interno para recibir datos del frontend/app
    public static class MovimientoRequest {
        private LocalDate fecha;
        private LocalTime hora;
        private String descripcionTipo; // opcional
        private TipoMovimiento tipoMovimiento; // opcional
        private String ubicacion;

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        
    	public LocalTime getHora() {
			return hora;
		}
		public void setHora(LocalTime hora) {
			this.hora = hora;
		}

        public String getDescripcionTipo() { return descripcionTipo; }
        public void setDescripcionTipo(String descripcionTipo) { this.descripcionTipo = descripcionTipo; }

        public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
        public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
	
    }
}
