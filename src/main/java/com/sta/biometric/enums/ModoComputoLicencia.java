package com.sta.biometric.enums;
public enum ModoComputoLicencia {
    
	DIAS_HABILES("DIAS HABILES"),    // Lunes a viernes, excluye feriados
	DIAS_LABORALES("TURNO LABORAL"),   // Según turno asignado, excluye feriados
	DIAS_CORRIDOS("DIAS CORRIDOS"); // Todos los dias, sin excluir fines de semana ni feriados
   
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