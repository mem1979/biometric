package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.enums.*;

import lombok.*;

/**
 * Entidad que representa una liquidación de jornadas para un empleado en un
 * período.
 * 
 * <p>
 * Centraliza el cálculo y almacenamiento de horas trabajadas (normales, extras
 * y especiales)
 * y sus valores monetarios correspondientes para un rango de fechas
 * determinado.
 * </p>
 * 
 * <p>
 * <b>Responsabilidades:</b>
 * </p>
 * <ul>
 * <li>Almacenar totales de horas por tipo (normales, extras, especiales)</li>
 * <li>Guardar snapshot de valores monetarios al momento de la liquidación</li>
 * <li>Calcular montos totales por tipo de hora y gran total</li>
 * <li>Mantener trazabilidad de generación y recálculos</li>
 * </ul>
 * 
 * <p>
 * <b>Ciclo de vida:</b>
 * </p>
 * <ol>
 * <li>Se genera consultando {@link AuditoriaRegistros} del período</li>
 * <li>Puede recalcularse mientras esté ABIERTO</li>
 * <li>Se cierra al finalizar el período (ej: fin de mes)</li>
 * </ol>
 * 
 * @author Sistema STARH
 * @version 1.0
 * @since 2.0
 * @see Personal
 * @see AuditoriaRegistros
 * @see EstadoLiquidacion
 */
@Entity
@Getter
@Setter
@Table(name = "liquidacion_jornadas", uniqueConstraints = @UniqueConstraint(columnNames = { "personal_id",
        "periodo_desde", "periodo_hasta" }))
@View(members = "empleado;" +
        "Periodo { periodoDesde, periodoHasta; estadoPeriodo; };" +
        "ResumenHoras { " +
        "  horasNormalesFormatted, horasExtrasFormatted, horasEspecialesFormatted;" +
        "};" +
        "ValoresMonetarios { " +
        "  valorHoraSnapshot, valorHoraExtraSnapshot, valorHoraEspecialSnapshot;" +
        "  montoTotalNormales, montoTotalExtras, montoTotalEspeciales;" +
        "  montoGranTotal;" +
        "};" +
        "Metadatos { fechaGeneracion, fechaUltimoRecalculo; observaciones; }")

// Vista unificada sin pestañas para el diálogo desde colección en Personal
@View(name = "DetalleCompleto",
   members = "periodoDesde, periodoHasta, estadoPeriodo;" +
		   	 "horasNormalesFormatted, horasExtrasFormatted, horasEspecialesFormatted, montoGranTotal;" +
		   	 "jornadasDelPeriodo;" +
			 "Metadatos { fechaGeneracion, fechaUltimoRecalculo; observaciones; }")

@Tab(properties = "empleado.nombreCompleto, periodoDesde, periodoHasta, estadoPeriodo, horasNormalesFormatted, horasExtrasFormatted, montoGranTotal", defaultOrder = "${periodoDesde} desc, ${empleado.nombreCompleto} asc")
public class LiquidacionJornadas extends Identifiable {

    // ==================================================================================
    // IDENTIFICACIÓN
    // ==================================================================================

    /**
     * Empleado al que pertenece esta liquidación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    @ReferenceView("simple")
    @NoFrame
    @ReadOnly
    private Personal empleado;

    // ==================================================================================
    // PERÍODO
    // ==================================================================================

    /**
     * Fecha de inicio del período de liquidación.
     */
    @Required
    @Stereotype("FECHA")
    @Column(name = "periodo_desde")
    private LocalDate periodoDesde;

    /**
     * Fecha de fin del período de liquidación.
     */
    @Required
    @Stereotype("FECHA")
    @Column(name = "periodo_hasta")
    private LocalDate periodoHasta;

    /**
     * Estado actual de la liquidación.
     * 
     * @see EstadoLiquidacion
     */
    @Required
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoLiquidacion estadoPeriodo = EstadoLiquidacion.ABIERTO;

    // ==================================================================================
    // HORAS CALCULADAS (en minutos para precisión)
    // ==================================================================================

    /**
     * Total de minutos normales trabajados en el período.
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int totalMinutosNormales;

    /**
     * Total de minutos extras trabajados en el período.
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int totalMinutosExtras;

    /**
     * Total de minutos especiales (feriados/días no laborales) trabajados.
     */
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int totalMinutosEspeciales;

    // ==================================================================================
    // VALORES SNAPSHOT (capturados al momento de generar)
    // ==================================================================================

    /**
     * Valor hora normal del empleado al momento de la liquidación.
     */
    @Money
    @ReadOnly
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraSnapshot;

    /**
     * Valor hora extra calculada al momento de la liquidación.
     */
    @Money
    @ReadOnly
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraExtraSnapshot;

    /**
     * Valor hora especial calculada al momento de la liquidación.
     */
    @Money
    @ReadOnly
    @Column(precision = 10, scale = 2)
    private BigDecimal valorHoraEspecialSnapshot;

    // ==================================================================================
    // MONTOS CALCULADOS
    // ==================================================================================

    /**
     * Monto total por horas normales.
     * Fórmula: (totalMinutosNormales / 60) × valorHoraSnapshot
     */
    @Money
    @ReadOnly
    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotalNormales;

    /**
     * Monto total por horas extras.
     * Fórmula: (totalMinutosExtras / 60) × valorHoraExtraSnapshot
     */
    @Money
    @ReadOnly
    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotalExtras;

    /**
     * Monto total por horas especiales.
     * Fórmula: (totalMinutosEspeciales / 60) × valorHoraEspecialSnapshot
     */
    @Money
    @ReadOnly
    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotalEspeciales;

    /**
     * Gran total de la liquidación.
     * Suma de montoTotalNormales + montoTotalExtras + montoTotalEspeciales
     */
    @Money
    @ReadOnly
    @MiLabel(medida = "grande", negrita = true, recuadro = true, icon = "currency-usd")
    @Column(precision = 12, scale = 2)
    private BigDecimal montoGranTotal;

    // ==================================================================================
    // METADATOS
    // ==================================================================================

    /**
     * Fecha y hora en que se generó la liquidación.
     */
    @ReadOnly
    private LocalDateTime fechaGeneracion;

    /**
     * Fecha y hora del último recálculo (null si nunca se recalculó).
     */
    @ReadOnly
    private LocalDateTime fechaUltimoRecalculo;

    /**
     * Observaciones o notas sobre la liquidación.
     */
    @TextArea
    private String observaciones;

    // ==================================================================================
    // MÉTODOS CALCULADOS PARA VISUALIZACIÓN
    // ==================================================================================

    /**
     * Retorna las horas normales formateadas como HH:MM.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-outline")
    public String getHorasNormalesFormatted() {
        return formatearMinutosComoHHMM(totalMinutosNormales);
    }

    /**
     * Retorna las horas extras formateadas como HH:MM.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "clock-plus-outline")
    public String getHorasExtrasFormatted() {
        return formatearMinutosComoHHMM(totalMinutosExtras);
    }

    /**
     * Retorna las horas especiales formateadas como HH:MM.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "star-outline")
    public String getHorasEspecialesFormatted() {
        return formatearMinutosComoHHMM(totalMinutosEspeciales);
    }

    /**
     * Formatea minutos a formato HH:MM.
     */
    private String formatearMinutosComoHHMM(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%d:%02d", horas, mins);
    }

    // ==================================================================================
    // COLECCIÓN DE JORNADAS DEL PERÍODO (para vista DetalleCompleto)
    // ==================================================================================

    /**
     * Retorna los registros de AuditoriaRegistros del empleado dentro del período
     * de esta liquidación.
     * 
     * @return Lista de jornadas del período con sus datos y montos
     */
    @Transient
  //  @ReadOnly
  //  @NoDefaultActions
    @ListProperties("empleado.nombreCompleto, fecha, turnoPlanificado, evaluacion, " +
            "horasTrabajadasTurno, montoTeoricoTurno+, " +
            "horasExtras, montoTeoricoExtras+, " +
            "horasEspeciales, montoTeoricoEspeciales+")
    @RowStyle(style = "estilo-verde-intenso", property = "evaluacion", value = "COMPLETA")
    @RowStyle(style = "estilo-amarillo-claro", property = "evaluacion", value = "INCOMPLETA")
    @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "AUSENTE")
    @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "LICENCIA")
    @RowStyle(style = "estilo-azul-claro", property = "evaluacion", value = "FERIADO")
    @RowStyle(style = "estilo-azul-intenso", property = "evaluacion", value = "FERIADO_TRABAJADO")
    @RowStyle(style = "estilo-verde-claro", property = "evaluacion", value = "DIA_NO_LABORAL")
   // @ListAction("Print.generatePdf")
    public java.util.List<AuditoriaRegistros> getJornadasDelPeriodo() {
        if (empleado == null || periodoDesde == null || periodoHasta == null) {
            return java.util.Collections.emptyList();
        }

        try {
            return org.openxava.jpa.XPersistence.getManager()
                    .createQuery(
                            "SELECT a FROM AuditoriaRegistros a " +
                                    "WHERE a.empleado = :emp " +
                                    "AND a.fecha >= :desde " +
                                    "AND a.fecha <= :hasta " +
                                    "ORDER BY a.fecha ASC",
                            AuditoriaRegistros.class)
                    .setParameter("emp", empleado)
                    .setParameter("desde", periodoDesde)
                    .setParameter("hasta", periodoHasta)
                    .getResultList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    // ==================================================================================
    // MÉTODOS DE NEGOCIO
    // ==================================================================================

    /**
     * Calcula los montos totales basándose en las horas y valores snapshot.
     * Debe llamarse después de actualizar las horas o valores.
     */
    public void calcularMontos() {
        // Calcular monto normales
        if (valorHoraSnapshot != null) {
            BigDecimal horasNormales = BigDecimal.valueOf(totalMinutosNormales)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            this.montoTotalNormales = horasNormales.multiply(valorHoraSnapshot)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.montoTotalNormales = BigDecimal.ZERO;
        }

        // Calcular monto extras
        if (valorHoraExtraSnapshot != null) {
            BigDecimal horasExtras = BigDecimal.valueOf(totalMinutosExtras)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            this.montoTotalExtras = horasExtras.multiply(valorHoraExtraSnapshot)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.montoTotalExtras = BigDecimal.ZERO;
        }

        // Calcular monto especiales
        if (valorHoraEspecialSnapshot != null) {
            BigDecimal horasEspeciales = BigDecimal.valueOf(totalMinutosEspeciales)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            this.montoTotalEspeciales = horasEspeciales.multiply(valorHoraEspecialSnapshot)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.montoTotalEspeciales = BigDecimal.ZERO;
        }

        // Calcular gran total
        this.montoGranTotal = (montoTotalNormales != null ? montoTotalNormales : BigDecimal.ZERO)
                .add(montoTotalExtras != null ? montoTotalExtras : BigDecimal.ZERO)
                .add(montoTotalEspeciales != null ? montoTotalEspeciales : BigDecimal.ZERO);
    }

    /**
     * Captura los valores monetarios actuales del empleado.
     */
    public void capturarValoresSnapshot() {
        if (empleado != null) {
            this.valorHoraSnapshot = empleado.getValorHora();
            this.valorHoraExtraSnapshot = empleado.getValorHoraExtra();
            this.valorHoraEspecialSnapshot = empleado.getValorHoraEspecial();
        }
    }

    /**
     * Verifica si la liquidación puede ser modificada.
     * 
     * @return true si está ABIERTO
     */
    @Transient
    public boolean esModificable() {
        return estadoPeriodo == EstadoLiquidacion.ABIERTO;
    }

    /**
     * Cierra la liquidación, marcándola como definitiva.
     */
    public void cerrar() {
        this.estadoPeriodo = EstadoLiquidacion.CERRADO;
    }

    /**
     * Marca la liquidación como recalculada.
     */
    public void marcarRecalculado() {
        this.estadoPeriodo = EstadoLiquidacion.RECALCULADO;
        this.fechaUltimoRecalculo = LocalDateTime.now();
    }

    // ==================================================================================
    // CALLBACKS JPA
    // ==================================================================================

    @PrePersist
    @PreUpdate
    private void antesDeGuardar() {
        // Asegurar que los montos estén calculados
        calcularMontos();
    }
}
