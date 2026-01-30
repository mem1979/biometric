package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de contrato laboral según modalidad de contratación.
 * 
 * @author Sistema STARH
 * @version 1.0
 * @see com.sta.biometric.modelo.ContratoLaboral
 */
@Getter
@RequiredArgsConstructor
public enum TipoContrato {

    TIEMPO_COMPLETO("Tiempo Completo",
            "Jornada laboral estándar según convenio colectivo"),

    MEDIO_TIEMPO("Medio Tiempo",
            "Jornada reducida al 50% de la jornada completa"),

    TIEMPO_PARCIAL("Tiempo Parcial",
            "Jornada reducida personalizada (diferente al 50%)"),

    EVENTUAL("Eventual / Temporario",
            "Contrato por tiempo determinado para cubrir necesidades temporales"),

    PASANTIA("Pasantía",
            "Contrato de formación para estudiantes o recién graduados"),

    POR_OBRA("Por Obra",
            "Contrato hasta completar una obra, proyecto o servicio determinado"),

    FREELANCE("Freelance / Monotributista",
            "Trabajador independiente que factura por sus servicios");

    private final String nombre;
    private final String descripcion;

    @Override
    public String toString() {
        return nombre;
    }
}
