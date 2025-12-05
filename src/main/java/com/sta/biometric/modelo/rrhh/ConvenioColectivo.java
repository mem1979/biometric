package com.sta.biometric.modelo.rrhh;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;

import lombok.*;

/**
 * Representa un Convenio Colectivo de Trabajo (CCT).
 * Ej: CCT 130/75 (Comercio), CCT 76/75 (UOCRA).
 * 
 * Agrupa categorías y define adicionales específicos.
 */
@Entity
@Getter
@Setter
@View(members = "Datos Generales [ codigo, nombre; sigla ];" +
        "Adicionales Convenio { " +
        "   porcentajeAntiguedadPorAno; " +
        "   porcentajePresentismo; " +
        "   aplicaZonaDesfavorable, porcentajeZona; " +
        "};" +
        "Categorias { categorias }")
public class ConvenioColectivo extends Identifiable {

    @Column(length = 20, unique = true)
    @SearchKey
    @Required
    private String codigo; // Ej: "130/75"

    @Column(length = 100)
    @Required
    @Capitalizar
    private String nombre; // Ej: "Empleados de Comercio"

    @Column(length = 10)
    @Capitalizar
    private String sigla; // Ej: "FAECYS"

    // --- Adicionales Estándar del Convenio ---

    @Column(scale = 2)
    private BigDecimal porcentajeAntiguedadPorAno; // Ej: 1.00%

    @Column(scale = 2)
    private BigDecimal porcentajePresentismo; // Ej: 8.33%

    private boolean aplicaZonaDesfavorable;

    @Column(scale = 2)
    private BigDecimal porcentajeZona; // Ej: 5.00%

    // --- Relación con Categorías ---

    @OneToMany(mappedBy = "convenio", cascade = CascadeType.ALL)
    @ListProperties("codigo, nombre, basicoMensual, valorHora")
    private Collection<Categoria> categorias;
}
