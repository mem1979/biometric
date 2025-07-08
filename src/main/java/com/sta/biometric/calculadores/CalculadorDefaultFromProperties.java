package com.sta.biometric.calculadores;
import java.util.*;

import org.openxava.calculators.*;

import com.sta.biometric.servicios.*;

import lombok.*;

/**
 * Calculador unificado que obtiene un valor desde biometricConfiguracion.properties.
 * Soporta Integer, Boolean y String.
 */
@Getter @Setter

public class CalculadorDefaultFromProperties implements ICalculator {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Clave en el archivo biometricConfiguracion.properties.
     */
    private String propiedad;

    /**
     * Valor por defecto en caso de que no se encuentre la propiedad.
     */
    private String valorPorDefecto;

    /**
     * Tipo de dato esperado: "int", "boolean", "string".
     */
    private String tipo;

    @Override
    public Object calculate() throws Exception {
        Properties props = ConfiguracionesPreferencias.getInstance().getProperties();
        String valor = props.getProperty(propiedad, valorPorDefecto);

        switch (tipo.toLowerCase()) {
            case "int":
                return Integer.parseInt(valor);
            case "boolean":
                return Boolean.parseBoolean(valor);
            case "string":
            default:
                return valor;
        }
    }
}