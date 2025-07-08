package com.sta.biometric.auxiliares;
import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;

import lombok.*;

@Entity
@Getter @Setter
@View(members = "fecha, tipo, motivo;" +
			    "infoAdicional"	)

@Tab(name = "feriadosAnuales", editors = "Calendar",
		properties = "motivo, tipo, fecha")



public class Feriados extends Identifiable {

    
    @Required
    @Column(unique = true)
    private LocalDate fecha;

    @Required
    @Mayuscula
    @Column(length = 30)
    private String tipo; // Ej: "inamovible", "trasladable", "puente", etc.

    @Required
    @Column(length = 80)
    private String motivo;

    @TextArea
    private String infoAdicional;

    public static boolean existeParaFecha(LocalDate fecha) {
        EntityManager em = XPersistence.getManager();
        Long count = em.createQuery("select count(f) from Feriados f where f.fecha = :fecha", Long.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
        return count > 0;
    }
}
