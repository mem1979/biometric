package com.sta.biometric.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Modalidad de trabajo según el lugar de desempeño.
 * 
 * @author Sistema STARH
 * @version 1.0
 * @see com.sta.biometric.modelo.ContratoLaboral
 */
@Getter
@RequiredArgsConstructor
public enum ModalidadTrabajo {

    PRESENCIAL("Presencial",
            "Trabajo 100% en las instalaciones de la empresa"),

    REMOTO("Remoto / Teletrabajo",
            "Trabajo 100% desde ubicación remota (home office)"),

    HIBRIDO("Híbrido",
            "Combinación de días presenciales y remotos"),

    ITINERANTE("Itinerante",
            "Trabajo en diferentes ubicaciones o con viajes frecuentes"),

    CAMPO("Trabajo en Campo",
            "Trabajo fuera de oficina (vendedores, técnicos de servicio, etc.)");

    private final String nombre;
    private final String descripcion;

    @Override
    public String toString() {
        return nombre;
    }
}
