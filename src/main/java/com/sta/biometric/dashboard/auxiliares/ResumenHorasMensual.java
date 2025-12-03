package com.sta.biometric.dashboard.auxiliares;

import java.math.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumenHorasMensual {
    private String mes; // "Ene 2025"
    private BigDecimal horasNormales; // Horas normales trabajadas
    private BigDecimal horasExtras; // Horas extras trabajadas
    private BigDecimal horasEspeciales; // Horas especiales (feriados)
}
