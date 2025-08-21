package com.sta.biometric.enums;

public enum EstadoCivil {
    SOLTERO_A("Soltero/a"),
    CASADO_A("Casado/a"),
    DIVORCIADO_A("Divorciado/a"),
    VIUDO_A("Viudo/a"),
    SEPARADO_A("Separado/a"),
    UNION_HECHO("Unión de Hecho"),
    OTRO("Otro");

    private String estadoCivil;

    // Constructor
    EstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    // Método para obtener la descripción
    public String getEstadoCivil() {
        return estadoCivil;
    }

    // Sobrescribimos el método toString para devolver la descripción
    @Override
    public String toString() {
        return estadoCivil;
    }
}
