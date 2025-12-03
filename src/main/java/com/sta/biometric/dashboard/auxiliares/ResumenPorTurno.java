package com.sta.biometric.dashboard.auxiliares;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumenPorTurno {
    private String turnoNombre; // Código del turno
    private int cantidadDias; // Días trabajados en ese turno
}
