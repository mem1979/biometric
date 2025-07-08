package com.sta.biometric.dashboard.auxiliares;

import java.time.*;

import com.sta.biometric.auxiliares.*;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoLicenciasFeriados {

	    private LocalDate fechaInicio;   // para feriados = fecha; para licencias = desde
	    private LocalDate fechaFin;      // para feriados = fecha; para licencias = hasta
	    private String    motivo;        // descripción o motivo
	    private String    tipo;          // "FERIADO" | "LICENCIA"
	    private String color;

	    /* --------- constructores de conveniencia --------- */

	    public static DtoLicenciasFeriados of(Feriados f) {
	    	DtoLicenciasFeriados e = new DtoLicenciasFeriados();
	        e.fechaInicio = f.getFecha();     // asumo getFecha()
	        e.fechaFin    = f.getFecha();
	        e.motivo      = f.getMotivo();
	        e.color       = "#f06662";
	        e.tipo        = "FERIADO";
	        return e;
	    }

	    public static DtoLicenciasFeriados of(Licencia l) {
	    	DtoLicenciasFeriados e = new DtoLicenciasFeriados();
	        e.fechaInicio = l.getFechaInicio();   
	        e.fechaFin    = l.getFechaFin();
	        e.motivo      = l.getTipo().name();        // es un Enum l.getTipo().name()
	        e.color       = "#64bfca";
	        e.tipo        = "LICENCIA";
	        return e;
	    }
	
}
