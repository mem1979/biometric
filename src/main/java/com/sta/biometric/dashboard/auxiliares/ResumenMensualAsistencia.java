package com.sta.biometric.dashboard.auxiliares;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumenMensualAsistencia {
    private String mes; // "Ene 2025"
    private int diasTrabajados; // Días con evaluación COMPLETA
    private int diasLicencia; // Días con evaluación LICENCIA
    private int diasAusente; // Días con evaluación AUSENTE
}
