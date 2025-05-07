
package com.sta.biometric.auxiliares;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.acciones.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@View(members = "tipo, modoComputo;" +
	  			"fechaInicio, fechaFin, dias, justificado;" +
	  			"observacion"
)

@Tab(properties = "empleado.nombreCompleto, tipo, fechaInicio, fechaFin, dias, justificado")

@Entity
@Getter @Setter
public class Licencia extends Identifiable {

	@Required
	@NoFrame
	@ReferenceView("simple")
	@ManyToOne(fetch = FetchType.LAZY)
	private Personal empleado;
		
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    @OnChange(CompletarObservacionLicenciaAction.class)
    private LocalDate fechaInicio;

    @Required
    @LabelFormat(LabelFormatType.SMALL)
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    @OnChange(CompletarObservacionLicenciaAction.class)
    private LocalDate fechaFin;
    
   @AssertTrue(message = "La fecha de inicio no puede ser posterior a la fecha de fin")
    public boolean isFechasValidas() {
        if (fechaInicio == null || fechaFin == null) return true;
        return !fechaInicio.isAfter(fechaFin); } 

   @ReadOnly
   @DisplaySize(5)
   @LabelFormat(LabelFormatType.SMALL)
   private Integer dias;
   
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    @OnChange(CompletarObservacionLicenciaAction.class)
    @Enumerated(EnumType.STRING)
    private TipoLicenciaAR tipo;
    
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    @OnChange(CompletarObservacionLicenciaAction.class)
    @Enumerated(EnumType.STRING)
    private ModoComputoLicencia modoComputo;

   
    @DefaultValueCalculator(TrueCalculator.class)
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean justificado;
    

    @Stereotype("TEXT_AREA")
    private String observacion;

    public static boolean tieneLicenciaEnFecha(Personal empleado, LocalDate fecha) {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery(
            "select count(l) from Licencia l where l.empleado = :emp " +
            "and :fecha between l.fechaInicio and l.fechaFin", Long.class)
            .setParameter("emp", empleado)
            .setParameter("fecha", fecha)
            .getSingleResult();
        return count > 0;
    }
    

 }