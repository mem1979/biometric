package com.sta.biometric.dashboard.auxiliares;

import java.time.*;

import com.sta.biometric.auxiliares.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoLicenciasFeriados {

	    private LocalDate fechaInicio;   // para feriados = fecha; para licencias = desde
	    private LocalDate fechaFin;      // para feriados = fecha; para licencias = hasta
	    private String    motivo;        // descripción o motivo
	    private String    tipo;          // "FERIADO" | "LICENCIA"
	    private String 	  color;
	    private String    titulo;
	    private boolean   allDay;
	    
	    /* --------- constructores de conveniencia --------- */

	    public static DtoLicenciasFeriados of(Feriados f) {
	    	DtoLicenciasFeriados e = new DtoLicenciasFeriados();
	        e.fechaInicio = f.getFecha();     // asumo getFecha()
	        e.fechaFin    = f.getFecha();
	        e.motivo      = f.getMotivo();
	        e.color       = "#f0666282";
	        e.tipo        = "FERIADO";
	        return e;
	    }

	    public static DtoLicenciasFeriados of(Licencia l) {
	    	DtoLicenciasFeriados e = new DtoLicenciasFeriados();
	        e.fechaInicio = l.getFechaInicio();   
	        e.fechaFin    = l.getFechaFin();
	        e.motivo      = l.getTipo().name();        // es un Enum l.getTipo().name()
	        e.color       = "#43A047"; // verde
	        e.tipo        = "LICENCIA";
	        return e;
	    }
	
	 // Ajustá imports y nombres a tu DTO real
	    public static DtoLicenciasFeriados ofCompleta(AuditoriaRegistros a) {
	        DtoLicenciasFeriados dto = new DtoLicenciasFeriados();
	        dto.setTitulo("Completa");
	        dto.setTipo("COMPLETA");
	        dto.setFechaInicio(a.getFecha()); // LocalDate; si es Date, convertí a LocalDate
	        dto.setFechaFin(a.getFecha());
	        dto.setAllDay(true);
	        dto.setColor("#3574f0"); // azul
	        dto.motivo = "Jornada completa";
	        return dto;
	    }
	    public static DtoLicenciasFeriados ofIncompleta(AuditoriaRegistros a) {
	        DtoLicenciasFeriados dto = new DtoLicenciasFeriados();
	        dto.setTitulo("Incompleta");
	        dto.setTipo("INCOMPLETA");
	        dto.setFechaInicio(a.getFecha());
	        dto.setFechaFin(a.getFecha());
	        dto.setAllDay(true);
	        dto.setColor("#FB8C00"); // naranja
	        dto.motivo = "Jornada por debajo de lo esperado";
	        return dto;
	    }
	    public static DtoLicenciasFeriados ofAusente(AuditoriaRegistros a) {
	        DtoLicenciasFeriados dto = new DtoLicenciasFeriados();
	        dto.setTitulo("Ausente");
	        dto.setTipo("AUSENTE");
	        dto.setFechaInicio(a.getFecha());
	        dto.setFechaFin(a.getFecha());
	        dto.setAllDay(true);
	        dto.setColor("#E53935"); // rojo
	        dto.motivo = "Día laborable sin registros";
	        return dto;
	    }
	    public static DtoLicenciasFeriados ofFeriadoTrabajado(AuditoriaRegistros a) {
	        DtoLicenciasFeriados dto = new DtoLicenciasFeriados();
	        dto.setTitulo("Feriado trabajado");
	        dto.setTipo("FERIADO_TRABAJADO");
	        dto.setFechaInicio(a.getFecha());
	        dto.setFechaFin(a.getFecha());
	        dto.setAllDay(true);
	        dto.setColor("#8E24AA"); // violeta
	        dto.motivo = "Trabajo en día feriado";
	        return dto;
	    }

}
