package com.sta.biometric.modelo;

import lombok.*;
import org.openxava.annotations.*;

/**
 * Clase transitoria para mostrar el resumen de horas trabajadas y extras.
 * Se utiliza para visualizar el resultado de la acción CalcularHorasAction.
 */
@Getter
@Setter
@View(members = "diasTotales; totalHorasNormales; totalHorasExtras; totalHorasEspeciales")
public class ResumenHoras {

    private int diasTotales;

    private String totalHorasNormales;

    private String totalHorasExtras;

    private String totalHorasEspeciales;

}
