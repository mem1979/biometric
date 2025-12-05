package com.sta.biometric.modelo.rrhh;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import lombok.*;

/**
 * Detalle (renglón) del recibo de sueldo.
 */
@Entity
@Getter
@Setter
@View(members = "concepto; unidad, cantidad, importe")
public class ItemLiquidacion extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY)
    private Liquidacion liquidacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ReferenceView("simple") // Codigo y Descripcion
    private ConceptoRecibo concepto;

    @Column(length = 10)
    private String unidad; // "Hs", "Dias", "%"

    @Column(scale = 2)
    private BigDecimal cantidad; // 30 dias, 160 hs, 11%

    @Money
    private BigDecimal importe;

}
