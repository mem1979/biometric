package com.sta.biometric.enums;

public enum Parentescos {
	
	 	CONYUGE("Cónyuge"),
	    CONVIVIENTE("Conviviente"),
	    HIJO("Hijo/a"),
	    HIJASTRO("Hijastro/a"),
	    PADRE("Padre"),
	    MADRE("Madre"),
	    HERMANO("Hermano/a"),
	    HERMANASTRO("Hermanastro/a"),
	    ABUELO("Abuelo/a"),
	    NIETO("Nieto/a"),
	    OTRO("Otro");

	    private String descripcion;

	    private Parentescos(String descripcion) {
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