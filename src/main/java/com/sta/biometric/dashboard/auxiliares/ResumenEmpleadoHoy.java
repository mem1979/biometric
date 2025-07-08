package com.sta.biometric.dashboard.auxiliares;

import javax.persistence.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResumenEmpleadoHoy {
    private Personal empleado;
    private boolean debeTrabajar;
    private boolean conLicencia;
    private boolean ingresoRealizado;
    private boolean llegadaTarde;
    private boolean salidaAnticipada;
    private EvaluacionJornada evaluacion;

    @Transient
    public String getIngresoRealizadoStr() {
        return debeTrabajar ? (ingresoRealizado ? "Sí" : "No") : "N/A";
    }

    @Transient
    public String getLlegadaTardeStr() {
        return debeTrabajar && ingresoRealizado ? (llegadaTarde ? "Sí" : "No") : "N/A";
    }

    @Transient
    public String getSalidaAnticipadaStr() {
        return debeTrabajar && ingresoRealizado ? (salidaAnticipada ? "Sí" : "No") : "N/A";
    }
    
    @Transient
    public String getEmpleadoNombre() {
        return empleado != null ? empleado.getNombreCompleto() : "";
    }

    @Transient
    public String getSucursalNombre() {
        return (empleado != null && empleado.getSucursal() != null) ? empleado.getSucursal().getNombre() : "";
    }
}