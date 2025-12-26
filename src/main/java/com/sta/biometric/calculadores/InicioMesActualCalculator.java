package com.sta.biometric.calculadores;

import java.time.*;

import org.openxava.calculators.*;

/**
 * Calculador que obtiene el primer día del mes actual.
 * Usado como valor por defecto en campos de fecha de período.
 */
public class InicioMesActualCalculator implements ICalculator {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Object calculate() throws Exception {
        return LocalDate.now().withDayOfMonth(1);
    }
}
