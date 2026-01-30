package com.sta.biometric.enums;

/**
 * Estrategias de redondeo para ajuste automático de horas.
 * 
 * <p>
 * El redondeo se aplica según el intervalo configurado (ej: 30 minutos):
 * </p>
 * <ul>
 * <li>MATEMATICO: Redondea al múltiplo más cercano (9h45→10h00, 9h14→9h00)</li>
 * <li>A_FAVOR_EMPLEADO: Siempre hacia arriba (9h01→9h30)</li>
 * <li>A_FAVOR_EMPRESA: Siempre hacia abajo (9h29→9h00)</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public enum TipoRedondeo {

    MATEMATICO("⚖️ Matemático (al más cercano)"),
    A_FAVOR_EMPLEADO("↑ A favor del empleado"),
    A_FAVOR_EMPRESA("↓ A favor de la empresa");

    private final String descripcion;

    TipoRedondeo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
