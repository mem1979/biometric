package com.sta.biometric.dashboard.auxiliares;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DistribucionJornada {
    private String tipoJornada; // "Completa", "Incompleta", "Licencia", etc.
    private int cantidad; // Número de ocurrencias
}
