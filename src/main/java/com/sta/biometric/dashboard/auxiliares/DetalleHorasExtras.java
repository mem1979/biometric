package com.sta.biometric.dashboard.auxiliares;

import java.math.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleHorasExtras {
    private LocalDate fecha; // Fecha del registro
    private String diaSemana; // Nombre del día
    private String turnoNombre; // Código del turno
    private String horasExtras; // Horas extras en formato "HH:MM"
    private BigDecimal montoExtras; // Valor monetario de las extras
}
