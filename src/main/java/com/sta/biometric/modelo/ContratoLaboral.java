package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.calculadores.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.embebidas.*;
import com.sta.biometric.enums.*;

import lombok.*;

/**
 * Contrato laboral de un empleado.
 * 
 * <p>
 * Define el puesto, categoría, nivel jerárquico y la configuración
 * económica (sueldo, valores hora, porcentajes).
 * </p>
 * 
 * <p>
 * Permite mantener historial: múltiples contratos con fechas de vigencia
 * distintas para el mismo empleado.
 * </p>
 * 
 * <p>
 * <b>Cálculos automáticos:</b>
 * </p>
 * <ul>
 * <li>Horas semanales/mensuales desde jornadas asignadas del empleado</li>
 * <li>Valor hora desde sueldo mensual acordado</li>
 * <li>Valores hora extra y especial con porcentajes configurables</li>
 * </ul>
 * 
 * @author Sistema STARH
 * @version 1.0
 * @see Personal
 * @see JornadaAsignada
 * @see TurnosHorarios
 */
@Entity
@Table(name = "contrato_laboral")
@Getter
@Setter
@View(members =
// ═══════════════════════════════════════════════════════════════════
// RESUMEN: Vista rápida del contrato
// ═══════════════════════════════════════════════════════════════════
"Resumen [" +
        "  tipoContrato," +
        "  fechaVigenciaDesde; " +
        "  puesto; " +
        "  sueldoMensualAcordado, valorHoraEfectivo; " +
        "]; " +

        // ═══════════════════════════════════════════════════════════════════
        // DATOS DEL PUESTO: Información del cargo y modalidad
        // ═══════════════════════════════════════════════════════════════════
        "DatosDelPuesto { " +
        "  modalidadTrabajo, nivelJerarquico; " +
        "  descripcionFunciones; " +
        "}; " +

        // ═══════════════════════════════════════════════════════════════════
        // CONFIGURACIÓN ECONÓMICA: Valores y porcentajes
        // ═══════════════════════════════════════════════════════════════════
        "ConfiguracionEconomica { " +
        "  HorasEsperadas [horasSemanalesEsperadas, horasMensualesEsperadas]; " +
        "  ValorHora [valorHoraCalculado, valorHoraAjustado]; " +
        "  HorasAdicionales [" +
        "    porcentajeHoraExtra, valorHoraExtra; " +
        "    porcentajeHoraEspecial, valorHoraEspecial" +
        "  ]; " +
        "}; " +

        // ═══════════════════════════════════════════════════════════════════
        // VIGENCIA: Período y observaciones
        // ═══════════════════════════════════════════════════════════════════
        "Vigencia { " +
        "  vigente; fechaVigenciaHasta; " +
        "  motivoFinalizacion; " +
        "  observaciones; " +
        "}")
@Tab(properties = "empleado.nombreCompleto, puesto, nivelJerarquico, sueldoMensualAcordado, fechaVigenciaDesde, vigente", defaultOrder = "${fechaVigenciaDesde} desc")
public class ContratoLaboral extends Identifiable {

    // =========================================================================
    // CONSTANTES
    // =========================================================================

    /** Semanas promedio por mes (52 semanas / 12 meses) */
    private static final BigDecimal SEMANAS_POR_MES = new BigDecimal("4.00");

    // =========================================================================
    // RELACIÓN CON EMPLEADO
    // =========================================================================

    /** Empleado al que pertenece este contrato */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    @ReferenceView("simple")
    @NoFrame
    @ReadOnly
    private Personal empleado;

    // =========================================================================
    // DATOS DEL PUESTO
    // =========================================================================

    /** Tipo de contrato (Tiempo completo, Medio tiempo, Eventual...) */
    // @LabelFormat(LabelFormatType.SMALL)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoContrato tipoContrato;

    /** Modalidad de trabajo (Presencial, Remoto, Híbrido...) */
    @LabelFormat(LabelFormatType.SMALL)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModalidadTrabajo modalidadTrabajo;

    /** Nivel en la estructura organizacional (enum universal) */
    @LabelFormat(LabelFormatType.SMALL)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelJerarquico nivelJerarquico;

    /** Título o nombre del cargo */
    @Capitalizar
    @Column(length = 100)
    @DisplaySize(50)
    private String puesto;

    /** Descripción detallada de las funciones del puesto */
    @TextArea
    @Column(length = 1000)
    private String descripcionFunciones;

    // =========================================================================
    // CONFIGURACIÓN ECONÓMICA
    // =========================================================================

    /** Sueldo bruto mensual acordado */
    @Money
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoMensualAcordado;

    /**
     * Valor hora ajustado manualmente.
     * 
     * <p>
     * Si tiene valor > 0, sobrescribe el valorHoraCalculado.
     * Si es null o 0, se usa el calculado automáticamente.
     * </p>
     * 
     * <p>
     * Casos de uso:
     * </p>
     * <ul>
     * <li>Empleado sin turno asignado (calculado = 0)</li>
     * <li>Bonificación o descuento especial acordado</li>
     * <li>Valor pactado diferente al calculado</li>
     * </ul>
     */
    @Money
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraAjustado;

    /** Porcentaje adicional para horas extras (ej: 50 = 50%) */
    @DefaultValueCalculator(value = CalculadorDefaultFromProperties.class, properties = {
            @PropertyValue(name = "propiedad", value = "porcentaje.hora.extra.default"),
            @PropertyValue(name = "valorPorDefecto", value = "50"),
            @PropertyValue(name = "tipo", value = "bigdecimal")
    })
    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(200)
    @Column(precision = 4, scale = 1)
    private BigDecimal porcentajeHoraExtra;

    /** Porcentaje adicional para horas especiales/feriados (ej: 100 = 100%) */
    @DefaultValueCalculator(value = CalculadorDefaultFromProperties.class, properties = {
            @PropertyValue(name = "propiedad", value = "porcentaje.hora.especial.default"),
            @PropertyValue(name = "valorPorDefecto", value = "100"),
            @PropertyValue(name = "tipo", value = "bigdecimal")
    })
    @Digits(integer = 3, fraction = 1)
    @Min(0)
    @Max(200)
    @Column(precision = 4, scale = 1)
    private BigDecimal porcentajeHoraEspecial;

    // =========================================================================
    // VIGENCIA E HISTORIAL
    // =========================================================================

    /** Fecha desde la cual el contrato está vigente */
    @Required
    @Stereotype("FECHA")
    @LabelFormat(LabelFormatType.SMALL)
    private LocalDate fechaVigenciaDesde;

    /** Fecha hasta la cual el contrato está vigente (null = activo) */
    private LocalDate fechaVigenciaHasta;

    /** Motivo de finalización del contrato (si aplica) */
    @Column(length = 200)
    @DisplaySize(50)
    private String motivoFinalizacion;

    /** Observaciones adicionales */
    @TextArea
    @Column(length = 500)
    private String observaciones;

    /** Fecha de última modificación */
    @ReadOnly
    private LocalDateTime fechaModificacion;

    // =========================================================================
    // MÉTODOS DE CÁLCULO DE HORAS
    // =========================================================================

    /**
     * Calcula las horas semanales esperadas desde las jornadas del empleado.
     * 
     * <p>
     * Lógica:
     * </p>
     * <ul>
     * <li>Si tiene UN turno vigente → usa sus horas semanales</li>
     * <li>Si tiene MÚLTIPLES turnos rotativos → promedia las horas</li>
     * </ul>
     * 
     * @return Horas semanales como BigDecimal
     */
    @Transient
    public BigDecimal calcularHorasSemanales() {
        if (empleado == null || empleado.getJornadasAsignadas() == null) {
            return BigDecimal.ZERO;
        }

        LocalDate hoy = LocalDate.now();
        List<JornadaAsignada> vigentes = empleado.getJornadasAsignadas().stream()
                .filter(j -> j.isVigenteParaFecha(hoy))
                .collect(Collectors.toList());

        if (vigentes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Sumar horas de todos los turnos vigentes
        BigDecimal sumaHoras = vigentes.stream()
                .filter(j -> j.getTurno() != null)
                .map(j -> j.getTurno().getTotalHorasDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Promediar si hay múltiples turnos
        return sumaHoras.divide(
                BigDecimal.valueOf(vigentes.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula las horas mensuales esperadas.
     * Fórmula: horasSemanales × 4.33
     * 
     * @return Horas mensuales como BigDecimal
     */
    @Transient
    public BigDecimal calcularHorasMensuales() {
        return calcularHorasSemanales()
                .multiply(SEMANAS_POR_MES)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================================
    // GETTERS CALCULADOS PARA VISUALIZACIÓN - HORAS
    // =========================================================================

    /**
     * Horas semanales esperadas formateadas para UI.
     */
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-outline")
    public String getHorasSemanalesEsperadas() {
        BigDecimal horas = calcularHorasSemanales();
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/sem";
    }

    /**
     * Horas mensuales esperadas formateadas para UI.
     */
    @Label
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "calendar-clock")
    public String getHorasMensualesEsperadas() {
        BigDecimal horas = calcularHorasMensuales();
        return horas.setScale(1, RoundingMode.HALF_UP) + " Hs/mes";
    }

    // =========================================================================
    // GETTERS CALCULADOS - VALORES MONETARIOS
    // =========================================================================

    /**
     * Valor hora calculado automáticamente desde sueldo mensual.
     * Fórmula: sueldoMensual / horasMensualesEsperadas
     * 
     * @return Valor hora calculado (solo lectura, referencia)
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado")
    public BigDecimal getValorHoraCalculado() {
        BigDecimal horasMensuales = calcularHorasMensuales();
        if (sueldoMensualAcordado == null ||
                horasMensuales.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return sueldoMensualAcordado.divide(horasMensuales, 2, RoundingMode.HALF_UP);
    }

    /**
     * Valor hora efectivo: usa el ajustado si existe, sino el calculado.
     * 
     * <p>
     * ESTE ES EL VALOR QUE SE USA EN:
     * </p>
     * <ul>
     * <li>Liquidaciones de jornadas</li>
     * <li>Cálculo de horas extras y especiales</li>
     * <li>Descuentos por ausencias</li>
     * <li>Snapshots en AuditoriaRegistros</li>
     * </ul>
     * 
     * @return Valor hora efectivo a aplicar
     */
    @Transient
    @Money
    @MiLabel(medida = "grande", negrita = true, recuadro = true, icon = "currency-usd")
    public BigDecimal getValorHoraEfectivo() {
        if (valorHoraAjustado != null &&
                valorHoraAjustado.compareTo(BigDecimal.ZERO) > 0) {
            return valorHoraAjustado;
        }
        return getValorHoraCalculado();
    }

    /**
     * Valor hora extra: valorHoraEfectivo × (1 + porcentajeExtra/100)
     * 
     * @return Valor hora con bonificación por hora extra
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado, valorHoraAjustado, porcentajeHoraExtra")
    public BigDecimal getValorHoraExtra() {
        BigDecimal base = getValorHoraEfectivo();
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeHoraExtra == null) {
            return base;
        }
        BigDecimal adicional = base.multiply(porcentajeHoraExtra)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(adicional);
    }

    /**
     * Valor hora especial (feriados): valorHoraEfectivo × (1 +
     * porcentajeEspecial/100)
     * 
     * @return Valor hora con bonificación por día especial
     */
    @Label
    @Money
    @Depends("sueldoMensualAcordado, valorHoraAjustado, porcentajeHoraEspecial")
    public BigDecimal getValorHoraEspecial() {
        BigDecimal base = getValorHoraEfectivo();
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeHoraEspecial == null) {
            return base;
        }
        BigDecimal adicional = base.multiply(porcentajeHoraEspecial)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(adicional);
    }

    /**
     * Indica si el contrato está vigente actualmente.
     * 
     * @return true si la fecha actual está dentro del rango de vigencia
     */
    @Label
    @Depends("fechaVigenciaDesde, fechaVigenciaHasta")
    public boolean isVigente() {
        // Verificar estado del empleado: Solo si está ELIMINADO deja de estar vigente
        // Inactivo (activo=false) NO implica fin de contrato (puede ser licencia,
        // suspensión, etc.)
        if (empleado != null && empleado.isEliminado()) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        if (fechaVigenciaDesde == null || hoy.isBefore(fechaVigenciaDesde)) {
            return false;
        }
        return fechaVigenciaHasta == null || !hoy.isAfter(fechaVigenciaHasta);
    }

    // =========================================================================
    // CALLBACKS JPA
    // =========================================================================

    @PrePersist
    @PreUpdate
    private void antesDeGuardar() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
