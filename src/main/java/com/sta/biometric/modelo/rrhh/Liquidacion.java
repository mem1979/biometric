package com.sta.biometric.modelo.rrhh;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.modelo.rrhh.enums.*;

import lombok.*;

/**
 * Cabecera de la Liquidación (Recibo).
 */
@Entity
@Getter
@Setter
@View(members = "Encabezado [ periodo; contrato; tipoDocumento ];" +
        "Totales [ " +
        "   totalRemunerativo; " +
        "   totalNoRemunerativo; " +
        "   totalDeducciones; " +
        "   totalRetenciones; " +
        "   netoACobrar " +
        "];" +
        "Detalle { items }")
public class Liquidacion extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ReferenceView("simple")
    private PeriodoLiquidacion periodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    private TipoDocumentoPago tipoDocumento;

    @Stereotype("FECHA")
    private LocalDate fechaCalculo;

    // --- Totales (Persistidos para historial) ---

    @Money
    @ReadOnly
    private BigDecimal totalRemunerativo;

    @Money
    @ReadOnly
    private BigDecimal totalNoRemunerativo;

    @Money
    @ReadOnly
    private BigDecimal totalDeducciones;

    @Money
    @ReadOnly
    private BigDecimal totalRetenciones; // IIBB, Ganancias

    @Money
    @ReadOnly
    @MiLabel(medida = "grande", negrita = true, recuadro = true)
    private BigDecimal netoACobrar;

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("concepto.codigo, concepto.descripcion, unidad, cantidad, importe")
    @ReadOnly
    private Collection<ItemLiquidacion> items;

    public void recalcularTotales() {
        totalRemunerativo = BigDecimal.ZERO;
        totalNoRemunerativo = BigDecimal.ZERO;
        totalDeducciones = BigDecimal.ZERO;
        totalRetenciones = BigDecimal.ZERO;

        if (items != null) {
            for (ItemLiquidacion item : items) {
                if (item.getImporte() == null)
                    continue;

                switch (item.getConcepto().getTipo()) {
                    case REMUNERATIVO:
                        totalRemunerativo = totalRemunerativo.add(item.getImporte());
                        break;
                    case NO_REMUNERATIVO:
                        totalNoRemunerativo = totalNoRemunerativo.add(item.getImporte());
                        break;
                    case DEDUCCION:
                        totalDeducciones = totalDeducciones.add(item.getImporte());
                        break;
                    case RETENCION_IMPOSITIVA:
                        totalRetenciones = totalRetenciones.add(item.getImporte());
                        break;
                    default:
                        break;
                }
            }
        }

        netoACobrar = totalRemunerativo.add(totalNoRemunerativo)
                .subtract(totalDeducciones)
                .subtract(totalRetenciones);
    }
}
