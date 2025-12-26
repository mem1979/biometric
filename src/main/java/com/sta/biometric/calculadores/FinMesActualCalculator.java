package com.sta.biometric.calculadores;

import java.time.*;

import org.openxava.calculators.*;

/**
 * Calculador que obtiene el último día del mes actual.
 * Usado como valor por defecto en campos de fecha de período.
 */
public class FinMesActualCalculator implements ICalculator {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Object calculate() throws Exception {
        LocalDate hoy = LocalDate.now();
        return hoy.withDayOfMonth(hoy.lengthOfMonth());
    }
}
