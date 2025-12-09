package com.sta.biometric.auxiliares;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;

import com.sta.biometric.anotaciones.*;

import lombok.*;

@Entity
@Getter
@Setter
public class Provincias {

    @Id
    @ReadOnly
    @Column(length = 6, nullable = false) // Hacemos que sea obligatorio
    private int numero;

    @PrePersist // Ejecutado justo antes de grabar el objeto por primera vez
    private synchronized void calcularNumero() {
        // Si el numero ya está asignado (ej: carga de datos seed), no lo recalcula
        if (this.numero > 0) {
            return;
        }
        Query query = XPersistence.getManager().createQuery(
                "select max(e.numero) from " + getClass().getSimpleName() + " e");
        Integer ultimoNumero = (Integer) query.getSingleResult();
        this.numero = (ultimoNumero == null) ? 1 : ultimoNumero + 1;
    }

    @Mayuscula
    @Column(length = 50)
    @Required
    @SearchKey
    private String nombre;

}