package com.sta.biometric.calculadores;

import java.time.LocalDateTime;

import org.openxava.calculators.ICalculator;

/**
 * 
 * @author System
 *
 */
public class CurrentLocalDateTimeCalculator implements ICalculator {

    private static final long serialVersionUID = 1L;

    public Object calculate() throws Exception {
        return LocalDateTime.now().withNano(0);
    }

}
