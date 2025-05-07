package com.sta.biometric.enums;
public enum ModoComputoLicencia {
    
	DIAS_LABORALES("Turno Laboral"),   // Según turno asignado, excluye feriados
	DIAS_HABILES("HABILES"),  // Lunes a viernes, excluye feriados
    DIAS_CORRIDOS("CORRIDOS"); // Todos los dias, sin excluir fines de semana ni feriados
   
   private final String descripcion;

    ModoComputoLicencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}