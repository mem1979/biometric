
package com.sta.biometric.auxiliares;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.sta.biometric.acciones.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@View(members = "tipo, modoComputo, justificado;" +
	  			"fechaInicio, fechaFin, dias, diasRestantes;" +
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
   
   @ReadOnly
   @DisplaySize(5)
   private Integer diasRestantes;
   
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
    
    /**
     * Año correspondiente a la licencia, derivado de la fecha desde.
     * Se usa para cálculos y agrupamientos por año calendario.
     */
    @Hidden
    public int getAnio() {
        return fechaInicio != null ? fechaInicio.getYear() : LocalDate.now().getYear();
    }

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
    
    @PrePersist
    @PreUpdate
    private void validarSolapamientoLicencias() {
        if (empleado == null || fechaInicio == null || fechaFin == null) return;

        EntityManager em = XPersistence.getManager();

        Licencia licenciaConflictiva = em.createQuery(
            "select l from Licencia l " +
            "where l.empleado = :emp " +
            "and l.id <> :id " +
            "and ( " +
            "    (:inicio between l.fechaInicio and l.fechaFin) or " +
            "    (:fin between l.fechaInicio and l.fechaFin) or " +
            "    (l.fechaInicio between :inicio and :fin) or " +
            "    (l.fechaFin between :inicio and :fin) " +
            ")",
            Licencia.class)
            .setParameter("emp", empleado)
            .setParameter("id", getId() == null ? "" : getId())
            .setParameter("inicio", fechaInicio)
            .setParameter("fin", fechaFin)
            .setMaxResults(1)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (licenciaConflictiva != null) {
            throw new javax.validation.ValidationException(
                String.format(
                    "Ya existe una licencia para %s desde el %s hasta el %s (Tipo: %s).",
                    empleado.getNombreCompleto(),
                    TiempoUtils.formatearFecha(licenciaConflictiva.getFechaInicio()),
                    TiempoUtils.formatearFecha(licenciaConflictiva.getFechaFin()),
                    licenciaConflictiva.getTipo() != null ? licenciaConflictiva.getTipo().name() : "Sin tipo"
                )
            );
        }
    }
    
   @PreRemove
    private void validarAntesDeEliminar() {
        if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
            throw new ValidationException(
                XavaResources.getString("no_puede_eliminar_licencia_finalizada")
            );
        }
    }

 }