package com.sta.biometric.modelo;

import org.openxava.annotations.*;

import lombok.*;

/**
 * Modelo transitorio para el diálogo de cambio de legajo.
 */
@Getter
@Setter
@View(members = "nuevoLegajo")
public class CambiarLegajoDialogo {

    @Required
    @DisplaySize(10)
    private String nuevoLegajo;
}
