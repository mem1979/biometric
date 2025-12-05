package com.sta.biometric.modelo.rrhh;

import java.math.*;
import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.*; // Personal
import com.sta.biometric.modelo.rrhh.enums.*; // Enums

import lombok.*;

/**
 * Representa el contrato laboral o vinculación entre la empresa y el prestador
 * (Empleado/Tercero).
 * 
 * Centraliza la configuración de liquidación:
 * - A quién se liquida (Personal).
 * - Cómo se liquida (Régimen LCT, Informal, Factura).
 * - Qué convenio aplica (si corresponde).
 * - Valores monetarios pactados (Básico, Valor Hora).
 */
@Entity
@Getter
@Setter
@View(members = "Estado [ activo, fechaInicio, fechaFin ];" +
        "Vinculacion [ regimen, modalidadLiquidacion ];" +
        "Empleado [ personal ];" +
        "Convenio [ convenio; categoria ];" +
        "Remuneracion [" +
        "   basicoPactado, valorHoraPactado;" +
        "   adicionalACuentaFuturosAumentos;" +
        "   CBU, banco;" +
        "]")
@Tab(properties = "personal.nombreCompleto, regimen, convenio.sigla, categoria.nombre, activo, fechaInicio")
public class Contrato extends Identifiable {

    // --- Estado del Contrato ---

    @DefaultValueCalculator(TrueCalculator.class)
    private boolean activo;

    @Required
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate fechaInicio;

    private LocalDate fechaFin; // Null si es indeterminado

    // --- Definición de Reglas ---

    @Required
    @Enumerated(EnumType.STRING)
    @OnChange(ContratoOnChangeRegimenAction.class) // Para ocultar/mostrar convenio si es Informal
    private RegimenContratacion regimen;

    @Required
    @Enumerated(EnumType.STRING)
    private ModalidadLiquidacion modalidadLiquidacion;

    // --- Vinculación ---

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ReferenceView("simple") // Info básica del empleado
    private Personal personal;

    // --- Encuadre Convencional (Puede ser null si es Fuera de Convenio o Informal)
    // ---

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    private ConvenioColectivo convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList(depends = "convenio", condition = "${convenio.id} = ?")
    private Categoria categoria;

    // --- Valores Monetarios ---
    // Pueden venir de la categoría (si es null acá) o ser un pactado específico.

    @Money
    private BigDecimal basicoPactado; // Override del básico de convenio o valor para F.C.

    @Money
    private BigDecimal valorHoraPactado; // Override del valor hora

    @Money
    private BigDecimal adicionalACuentaFuturosAumentos;

    // --- Datos de Pago ---

    @Column(length = 22)
    private String CBU;

    @Column(length = 50)
    private String banco;

}
