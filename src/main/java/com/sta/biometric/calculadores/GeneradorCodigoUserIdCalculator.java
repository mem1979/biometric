package com.sta.biometric.calculadores;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;

public class GeneradorCodigoUserIdCalculator implements ICalculator {

    private static final long serialVersionUID = 1L;

    @Override
    public Object calculate() throws Exception {
        // Usamos JPQL para extraer el maximo valor numerico despues del prefijo 'A'
        Query query = XPersistence.getManager().createQuery(
            "select max(cast(substring(p.userId, 2) as integer)) " +
            "from Personal p where p.userId like 'A%'"
        );

        Integer ultimoNumero = (Integer) query.getSingleResult();
        int nuevoNumero = (ultimoNumero == null) ? 1 : ultimoNumero + 1;

        return "A" + nuevoNumero;
    }
}