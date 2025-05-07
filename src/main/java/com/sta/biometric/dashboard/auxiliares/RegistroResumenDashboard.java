package com.sta.biometric.dashboard.auxiliares;
import javax.persistence.*;

import com.sta.biometric.enums.*;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResumenDashboard {

    
    private String sucursal;

    
    private String empleado;

    
    private String turnoHoy;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;

  
    private String evaluacion;
}