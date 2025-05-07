package com.sta.biometric.dashboard.auxiliares;
import com.sta.biometric.enums.*;

import lombok.*;

@Getter @Setter
public class LicenciaResumenPorTipo {

    private TipoLicenciaAR tipo;
    private int dias;

    public LicenciaResumenPorTipo() {}

    public LicenciaResumenPorTipo(TipoLicenciaAR tipo, int dias) {
        this.tipo = tipo;
        this.dias = dias;
    }
}