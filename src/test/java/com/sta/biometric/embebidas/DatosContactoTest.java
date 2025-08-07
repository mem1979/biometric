package com.sta.biometric.embebidas;

import org.junit.Test;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.*;

public class DatosContactoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void webNoDebeExceder100Caracteres() {
        DatosContacto datos = new DatosContacto();
        String longWeb = "https://example.com/" + "a".repeat(90);
        datos.setWeb(longWeb);

        Set<ConstraintViolation<DatosContacto>> violations = validator.validate(datos);
        boolean found = violations.stream()
            .anyMatch(v -> "web".equals(v.getPropertyPath().toString())
                    && v.getMessage().contains("100"));
        assertTrue("Debe existir una violación por tamaño máximo", found);
    }
}
