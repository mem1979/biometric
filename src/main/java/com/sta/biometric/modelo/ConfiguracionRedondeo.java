package com.sta.biometric.modelo;

import org.openxava.annotations.*;

import com.sta.biometric.enums.TipoRedondeo;

import lombok.*;

/**
 * Modelo transitorio para el diálogo de configuración de redondeo automático.
 * 
 * <p>
 * Permite al usuario configurar:
 * </p>
 * <ul>
 * <li>Intervalo de redondeo (ej: 30 minutos)</li>
 * <li>Estrategia por tipo de hora (normales, extras, especiales)</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
@Getter
@Setter
@View(members = "intervaloMinutos;" +
        "Estrategias_por_Tipo [" +
        "  estrategiaNormales;" +
        "  estrategiaExtras;" +
        "  estrategiaEspeciales" +
        "]")
public class ConfiguracionRedondeo {

    /**
     * Intervalo de redondeo en minutos (ej: 5, 10, 15, 30).
     * El total de cada tipo de hora se redondea al múltiplo más cercano de este
     * valor.
     * El ajuste máximo aplicado será de 30 minutos (media hora).
     */
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private int intervaloMinutos = 30;

    /**
     * Estrategia de redondeo para horas normales.
     */
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private TipoRedondeo estrategiaNormales = TipoRedondeo.MATEMATICO;

    /**
     * Estrategia de redondeo para horas extras.
     */
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private TipoRedondeo estrategiaExtras = TipoRedondeo.MATEMATICO;

    /**
     * Estrategia de redondeo para horas especiales.
     */
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private TipoRedondeo estrategiaEspeciales = TipoRedondeo.MATEMATICO;
}
