package com.sta.biometric.enums;
public enum TipoMovimiento  {

    ENTRADA("INICIO DE JORNADA"),
    SALIDA("FIN DE JORNADA"),              
    PAUSA_INICIO("INICIO DE PAUSA LABORAL"),  
    PAUSA_FIN("FIN DE PAUSA LABORAL"),      
    UBICACION("UBICACION"),       
    MANUAL("REGISTRO MANUAL");


	 private final String descripcion;

	 TipoMovimiento(String descripcion) {
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