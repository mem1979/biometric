package com.sta.biometric.modelo.rrhh;

import java.math.*;
import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import lombok.*;

/**
 * Cuota individual de un préstamo.
 * Se marca como 'liquidada' cuando se incluye en un recibo cerrado.
 */
@Entity
@Getter
@Setter
public class CuotaPrestamo extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY)
    private Prestamo prestamo;

    private int nroCuota;

    @Required
    private LocalDate fechaVencimiento; // Mes/Año en que debería descontarse

    @Money
    @Required
    private BigDecimal monto;

    @ReadOnly
    private boolean liquidada;

    // Futuro: Relación con la Liquidación que la pagó
    // @ManyToOne private Liquidacion liquidacionPago;
}
