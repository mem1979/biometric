package com.sta.biometric.dashboard.auxiliares;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResumenAnualGrafico {
    private String mesEtiqueta;  // "Ene", "Feb", ...
    private int completas;
    private int incompletas;
    private int licencias;
    private int ausentes;
    private int feriadosTrabajados; // FERIADO_TRABAJADO
}