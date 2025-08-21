package com.sta.biometric.calculadores;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;

public class NacionalidadPorDefectoCalculator implements ICalculator {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Object calculate() throws Exception {
        Query query = XPersistence.getManager()
            .createQuery("from Nacionalidades n where n.nacionalidad = :nombre");
        query.setParameter("nombre", "Argentino/a");
        return query.getSingleResult(); // Asegúrate de que existe en la base
    }
}