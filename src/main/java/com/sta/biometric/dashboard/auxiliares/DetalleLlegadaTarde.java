package com.sta.biometric.dashboard.auxiliares;

import java.time.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleLlegadaTarde {
    private LocalDate fecha; // Fecha del registro
    private LocalTime horaEsperada; // Hora de entrada planificada
    private LocalTime horaReal; // Hora de entrada real
    private int minutosRetraso; // Minutos de retraso
    private boolean justificado; // Si está justificado
}
