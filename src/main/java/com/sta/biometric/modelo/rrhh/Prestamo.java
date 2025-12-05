package com.sta.biometric.modelo.rrhh;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.*; // Personal

import lombok.*;

/**
 * Representa un adelanto de sueldo o préstamo a largo plazo.
 */
@Entity
@Getter
@Setter
@View(members = "Datos Generales [ personal; fechaSolicitud ] ;" +
        "Economico [ montoTotal; cantidadCuotas; cancelado ];" +
        "Cuotas { cuotas }")
public class Prestamo extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ReferenceView("simple")
    private Personal personal;

    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    @Required
    private LocalDate fechaSolicitud;

    @Money
    @Required
    private BigDecimal montoTotal;

    @Min(1)
    @DefaultValueCalculator(value = IntegerCalculator.class, properties = @PropertyValue(name = "value", value = "1"))
    private int cantidadCuotas; // 1 = Adelanto de sueldo del mes

    @DefaultValueCalculator(FalseCalculator.class)
    @ReadOnly
    private boolean cancelado; // True si todas las cuotas están pagas

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("nroCuota, fechaVencimiento, monto, liquidada")
    @ReadOnly // Se generan por acción, no editable a mano para integridad
    private Collection<CuotaPrestamo> cuotas;

    @PrePersist
    public void generarCuotasAutomaticas() {
        if (cuotas == null)
            cuotas = new ArrayList<>();
        if (cuotas.isEmpty() && montoTotal != null && cantidadCuotas > 0) {
            BigDecimal montoCuota = montoTotal.divide(new BigDecimal(cantidadCuotas), 2, RoundingMode.HALF_UP);
            LocalDate primerVencimiento = fechaSolicitud.withDayOfMonth(1).plusMonths(1); // Descuenta mes siguiente

            for (int i = 1; i <= cantidadCuotas; i++) {
                CuotaPrestamo c = new CuotaPrestamo();
                c.setPrestamo(this);
                c.setNroCuota(i);
                c.setMonto(montoCuota);

                // Si es adelanto (1 cuota), vencimiento es el mismo mes si es antes del cierre,
                // o próx.
                // Simplificación: Vencimiento mes siguiente al pedido.
                c.setFechaVencimiento(primerVencimiento.plusMonths(i - 1));

                cuotas.add(c);
            }
        }
    }
}
