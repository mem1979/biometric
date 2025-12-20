package com.sta.biometric.modelo;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;

import lombok.*;

/**
 * Representa una fila en la tabla de cálculos de horas.
 * Usado con @ElementCollection en AuditoriaRegistros.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilaCalculo {

    /**
     * Tipo de hora: "⏰ Normales", "⏰+ Extras", "⭐ Especiales"
     * 
     */
	@ReadOnly
    private String tipo;

    /**
     * Valor hora (snapshot histórico)
     */
    @Money @ReadOnly
    private BigDecimal valorHora;

    /**
     * Horas registradas antes del ajuste (formato HH:MM)
     */
    @ReadOnly
    private String horasRegistradas;

    /**
     * Ajuste aplicado (formato +/-HH:MM)
     */
    @ReadOnly
    private String ajuste;

    /**
     * Total monetario calculado
     */
    @Money
    @ReadOnly
    private BigDecimal total;
}
