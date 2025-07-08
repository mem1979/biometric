package com.sta.biometric.embebidas;
import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;

import com.sta.biometric.auxiliares.*;

import lombok.*;

@Embeddable
@Getter @Setter
public class JornadaAsignada {

	@Required
	@ReadOnly
	@ManyToOne(fetch = FetchType.LAZY)
    private TurnosHorarios turno;

    @Required
    @Stereotype("FECHA")
    private LocalDate fechaInicio;

    @Stereotype("FECHA")
    private LocalDate fechaFin; // null = indefinido
}
