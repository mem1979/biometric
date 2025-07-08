package com.sta.biometric.calculadores;

import org.openxava.calculators.*;

public class CalculadorCodigoAreaDefault implements ICalculator {

   	private static final long serialVersionUID = 1L;
   	
    private String tipoTelefono;

    // Setter para recibir el tipo de teléfono
    public void setTipoTelefono(String tipoTelefono) {
        this.tipoTelefono = tipoTelefono;
    }

    @Override
    public Object calculate() throws Exception {
      
    	// Retorna el código de área dependiendo del tipo de teléfono
        if ("CELULAR".equalsIgnoreCase(tipoTelefono)) {
            return " +54 9 11"; // Código de área para celulares (por ejemplo)
        } else if ("TELEFONO".equalsIgnoreCase(tipoTelefono)) {
            return " 54"; // Código de área para teléfonos fijos (por ejemplo)
        } else {
            return ""; // Valor por defecto si no se reconoce el tipo
        }
    }
}