package com.sta.biometric.calculadores;
import java.time.*;

import org.openxava.calculators.*;

public class CurrentLocalTimeCalculator implements ICalculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object calculate() throws Exception {
		// Truncamos a segundos
				return LocalTime.now().withNano(0);
			}
}