package com.sta.biometric.modelo;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.auxiliares.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;

import lombok.*;

/**
 * ======================================================================================
 * ENTIDAD: AuditoriaRegistros
 * ======================================================================================
 * Representa el registro consolidado de asistencia de un empleado para un día
 * específico.
 * 
 * OBJETIVO:
 * - Centralizar toda la información de la jornada laboral (fichadas, turno,
 * resultados).
 * - Persistir datos históricos para que no cambien si la configuración del
 * empleado cambia.
 * - Permitir ajustes manuales y recálculo de horas.
 * 
 * FUNCIONAMIENTO:
 * - Se genera/actualiza mediante la acción de "Consolidar" o al importar
 * fichadas.
 * - Calcula automáticamente horas trabajadas, extras y estado (Presente, Tarde,
 * Ausente).
 * - Guarda una "foto" (snapshot) de los valores monetarios del momento.
 */
@Entity
@Getter
@Setter
@View(members = "empleado;" +
        "DetalleTurno { " +
        "turnoPlanificado, evaluacion; observacionFeriado; " +
        "Registros { registros; estadoJornada };" +
        "};" +
        "Calculos_Y_Ajustes { " +
        "horasTrabajadasTurno, ajusteMinutosNormales, totalHorasTurno ; " +
        "horasExtras, ajusteMinutosExtras, totalHorasExtras; " +
        "horasEspeciales, ajusteMinutosEspeciales, totalHorasEspeciales; " +
        "botonAjustarHoras; " +
        "ajustesRealizados " +
        "};" +
        "Notas { nota };")

@Tab(editors = "List", properties = "empleado.nombreCompleto, diaSemana, fecha, horario, evaluacion, estadoJornada, empleado.sucursal.nombre", defaultOrder = "${fecha} desc, ${empleado.sucursal.nombre} asc, ${empleado.nombreCompleto} asc", rowStyles = {
        @RowStyle(style = "estilo-gris-claro", property = "evaluacion", value = "PENDIENTE"),
        @RowStyle(style = "estilo-gris-intenso", property = "evaluacion", value = "EN_CURSO"),
        @RowStyle(style = "estilo-verde-intenso", property = "evaluacion", value = "COMPLETA"),
        @RowStyle(style = "estilo-amarillo-claro", property = "evaluacion", value = "INCOMPLETA"),
        @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "AUSENTE"),
        @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "LICENCIA"),
        @RowStyle(style = "estilo-azul-claro", property = "evaluacion", value = "FERIADO"),
        @RowStyle(style = "estilo-azul-intenso", property = "evaluacion", value = "FERIADO_TRABAJADO"),
        @RowStyle(style = "estilo-verde-claro", property = "evaluacion", value = "DIA_NO_LABORAL"),
        @RowStyle(style = "estilo-azul-intenso", property = "evaluacion", value = "DIA_NO_LABORAL_TRABAJADO"),
        @RowStyle(style = "estilo-verde-claro", property = "evaluacion", value = "SIN_TURNO_ASIGNADO"),
        @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "SIN_DATOS")
})
public class AuditoriaRegistros extends Identifiable {

    // ==================================================================================
    // 1. IDENTIFICACIÓN DEL REGISTRO
    // ==================================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @ReferenceView("simple")
    @NoFrame
    @ReadOnly
    private Personal empleado; // Empleado al que pertenece este registro

    @Stereotype("FECHA")
    private LocalDate fecha; // Fecha de la jornada (clave lógica junto con empleado)

    // ==================================================================================
    // 2. CONFIGURACIÓN DEL TURNO (PERSISTENCIA HISTÓRICA)
    // ==================================================================================
    // Estos campos guardan la configuración del turno COMO ERA en el momento del
    // registro.
    // Esto evita que cambios futuros en el turno afecten registros pasados.

    @ReadOnly
    @Enumerated(EnumType.STRING)
    private Turnos nombreTurno; // Nombre del turno persistido

    @Stereotype("HORA")
    private LocalTime horaEsperadaEntrada; // Hora de entrada planificada

    @Stereotype("HORA")
    private LocalTime horaEsperadaSalida; // Hora de salida planificada

    private int minutosEsperados; // Duración total esperada en minutos

    @ReadOnly
    private int toleranciaMinutos; // Tolerancia del turno en minutos (snapshot)

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal porcentajeBonificacionSnapshot; // % de bonificación del turno (0-100)

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal valorHoraTurnoSnapshot; // Valor hora con bonificación aplicada

    // ==================================================================================
    // 3. RESULTADOS DEL PROCESAMIENTO (FICHADAS Y CÁLCULOS)
    // ==================================================================================

    @EditOnly
    @NoDefaultActions
    @RowStyle(style = "estilo-verde-claro", property = "evaluacion", value = "ENTRADA EN HORARIO")
    @RowStyle(style = "estilo-verde-claro", property = "evaluacion", value = "SALIDA EN HORARIO")
    @RowStyle(style = "estilo-verde-intenso", property = "evaluacion", value = "ENTRADA ANTICIPADA")
    @RowStyle(style = "estilo-amarillo-intenso", property = "evaluacion", value = "SALIDA ANTICIPADA")
    @RowStyle(style = "estilo-amarillo-intenso", property = "evaluacion", value = "ENTRADA TARDE")
    @RowStyle(style = "estilo-verde-intenso", property = "evaluacion", value = "SALIDA TARDIA")
    @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "SIN HORARIO DE ENTRADA")
    @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "SIN HORARIO DE SALIDA")
    @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "DIA NO LABORAL")
    @RowStyle(style = "estilo-rojo-claro", property = "evaluacion", value = "SIN TURNO ASIGNADO")
    @RowStyle(style = "estilo-azul-claro", property = "evaluacion", value = "INICIO PAUSA")
    @RowStyle(style = "estilo-azul-claro", property = "evaluacion", value = "FIN PAUSA")
    @RowStyle(style = "estilo-azul-claro", property = "evaluacion", value = "UBICACION")
    @RowStyle(style = "estilo-azul-intenso", property = "evaluacion", value = "REGISTRO MANUAL")
    @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "ERROR DE REGISTRO - SIN ASISTENCIA DIARIA")
    @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "ERROR DE REGISTRO - SIN DATOS")
    @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "ERROR DE REGISTRO - SIN EMPLEADO")
    @RowStyle(style = "estilo-rojo-intenso", property = "evaluacion", value = "REGISTRO NO VALIDADO - TIPO DE MOVIMIENTO INCORRECTO")
    @OneToMany(mappedBy = "asistenciaDiaria", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("tipoMovimiento,diaSemana, fecha, hora, evaluacion")
    private List<ColeccionRegistros> registros = new ArrayList<>(); // Lista de fichadas crudas

    private int minutosTrabajados; // Total de minutos trabajados reales
    private int minutosExtras; // Total de minutos extras calculados

    @Stereotype("BOLD_LABEL")
    @LabelFormat(LabelFormatType.NO_LABEL)
    private EvaluacionJornada evaluacion; // Estado final (COMPLETA, AUSENTE, etc.)

    // ==================================================================================
    // 4. BANDERAS DE ESTADO (CONDICIONES ESPECIALES)
    // ==================================================================================

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    @DefaultValueCalculator(TrueCalculator.class)
    private boolean justificado; // Indica si una ausencia/llegada tarde está justificada

    private boolean feriado; // Indica si la fecha cae en un feriado persistido

    private boolean licencia; // Indica si el empleado tiene licencia activa persistida

    // ==================================================================================
    // 5. VALORES MONETARIOS (SNAPSHOTS)
    // ==================================================================================
    // Guardamos el valor monetario calculado para no depender del valor hora actual
    // del empleado.

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal valorHoraSnapshot; // Valor hora del empleado en ese momento

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal montoTeoricoTurno; // $ Calculado por horas normales

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal montoTeoricoExtras; // $ Calculado por horas extras

    @Column(scale = 2)
    @ReadOnly
    private BigDecimal montoTeoricoEspeciales; // $ Calculado por horas especiales (feriados)

    // ==================================================================================
    // 6. AJUSTES MANUALES
    // ==================================================================================
    // Permite al supervisor corregir horas sin alterar las fichadas originales.

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int ajusteMinutosNormales; // Minutos a sumar/restar a normales

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int ajusteMinutosExtras; // Minutos a sumar/restar a extras

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private int ajusteMinutosEspeciales; // Minutos a sumar/restar a especiales

    @Stereotype("MEMO")
    private String nota; // Observaciones generales

    // ==================================================================================
    // LÓGICA PRINCIPAL DE NEGOCIO
    // ==================================================================================

    /**
     * Método central que procesa la información y determina el estado de la
     * jornada.
     * Se llama cada vez que se agregan fichadas o se recalcula.
     */
    public void consolidarDesdeRegistros() {
        if (empleado == null || fecha == null)
            return;

        // 1. Obtener y persistir configuración del turno
        inicializarTurnoYCondiciones();

        // 2. Verificar condiciones especiales (Feriados, Licencias)
        feriado = Feriados.existeParaFecha(fecha);
        licencia = Licencia.tieneLicenciaEnFecha(empleado, fecha);

        // 3. Calcular tiempos según fichadas
        if (registros == null || registros.isEmpty()) {
            evaluarSinRegistros();
        } else {
            calcularDuraciones();
            evaluarConRegistros();
        }

        // 4. Calcular y persistir valores monetarios (Snapshot)
        if (empleado != null) {
            this.valorHoraSnapshot = empleado.getValorHora();

            // Calculamos montos usando el valor hora con bonificación del turno
            BigDecimal valorHoraTurno = valorHoraTurnoSnapshot != null ? valorHoraTurnoSnapshot
                    : empleado.getValorHora();

            this.montoTeoricoTurno = calcularTotalMonetario(getHorasTrabajadasTurno(), valorHoraTurno);
            this.montoTeoricoExtras = calcularTotalMonetario(getHorasExtras(), empleado.getValorHoraExtra());
            this.montoTeoricoEspeciales = calcularTotalMonetario(getHorasEspeciales(), empleado.getValorHoraEspecial());
        }

        // 5. Actualizar nota automática
        actualizarNotaSegunEvaluacion();
    }

    /**
     * Busca el turno correspondiente y guarda sus parámetros en este registro.
     * Esto asegura la inmutabilidad histórica.
     */
    public void inicializarTurnoYCondiciones() {
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        DayOfWeek dia = fecha.getDayOfWeek();

        if (turno != null) {
            horaEsperadaEntrada = turno.getEntradaParaDia(dia);
            horaEsperadaSalida = turno.getSalidaParaDia(dia);
            minutosEsperados = TiempoUtils.calcularMinutosLocalTime(horaEsperadaEntrada, horaEsperadaSalida);
            // Guardamos nombre y código LIMPIOS para referencia futura
            this.nombreTurno = turno.getTurnoNombre();
            // Guardamos tolerancia (snapshot)
            this.toleranciaMinutos = turno.getTolerancia() != null ? turno.getTolerancia() : 0;

            // Guardamos bonificación del turno (snapshot)
            this.porcentajeBonificacionSnapshot = turno.getPorcentajeBonificacion() != null
                    ? turno.getPorcentajeBonificacion()
                    : BigDecimal.ZERO;

            // Calculamos y guardamos valor hora con bonificación
            if (empleado != null) {
                this.valorHoraTurnoSnapshot = empleado.getValorHoraTurno(turno);
            }
        } else {
            horaEsperadaEntrada = null;
            horaEsperadaSalida = null;
            minutosEsperados = 0;
            this.nombreTurno = null;
            this.toleranciaMinutos = 0;

            // Valores por defecto si no hay turno
            this.porcentajeBonificacionSnapshot = BigDecimal.ZERO;
            this.valorHoraTurnoSnapshot = empleado != null ? empleado.getValorHora() : null;
        }
    }

    /**
     * Calcula minutos trabajados basándose en la primera entrada y última salida.
     */
    private void calcularDuraciones() {
        registros.sort(Comparator.comparing(ColeccionRegistros::getHora));

        LocalTime inicio = registros.get(0).getHora();
        LocalTime fin = registros.get(registros.size() - 1).getHora();

        minutosTrabajados = TiempoUtils.calcularMinutosLocalTime(inicio, fin);
        minutosExtras = Math.max(0, minutosTrabajados - minutosEsperados);
    }

    private void evaluarSinRegistros() {
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        boolean esLaboral = turno != null && turno.esLaboral(fecha.getDayOfWeek());

        if (licencia) {
            evaluacion = EvaluacionJornada.LICENCIA;
        } else if (feriado) {
            evaluacion = EvaluacionJornada.FERIADO;
        } else if (!esLaboral) {
            evaluacion = EvaluacionJornada.DIA_NO_LABORAL;
        } else {
            evaluacion = EvaluacionJornada.AUSENTE;
        }
    }

    private void evaluarConRegistros() {
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        boolean esLaboral = turno != null && turno.esLaboral(fecha.getDayOfWeek());

        if (licencia) {
            evaluacion = EvaluacionJornada.LICENCIA;
        } else if (feriado) {
            evaluacion = EvaluacionJornada.FERIADO_TRABAJADO;
        } else if (!esLaboral) {
            evaluacion = EvaluacionJornada.DIA_NO_LABORAL_TRABAJADO;
        } else if (minutosTrabajados >= (minutosEsperados - toleranciaMinutos)) {
            evaluacion = EvaluacionJornada.COMPLETA;
        } else {
            evaluacion = EvaluacionJornada.INCOMPLETA;
        }
    }

    public void actualizarNotaSegunEvaluacion() {
        if (evaluacion == EvaluacionJornada.LICENCIA) {
            // Obtener detalles de la licencia
            Licencia licenciaDetalle = Licencia.getLicenciaEnFecha(empleado, fecha);
            if (licenciaDetalle != null) {
                String tipoDesc = licenciaDetalle.getTipo() != null ? licenciaDetalle.getTipo().toString()
                        : "No especificado";
                String justificadaStr = licenciaDetalle.isJustificado() ? "Justificada" : "No justificada";
                setNota(String.format("Licencia: %s (%s)", tipoDesc, justificadaStr));
            } else {
                setNota("Licencia activa para hoy.");
            }
        } else if (evaluacion == EvaluacionJornada.FERIADO) {
            if (getNota() == null || getNota().isBlank()) {
                setNota(getObservacionFeriado());
            }
        } else if (evaluacion == EvaluacionJornada.DIA_NO_LABORAL) {
            setNota("Día sin turno asignado.");
        } else if (evaluacion == EvaluacionJornada.EN_CURSO) {
            if (getNota() == null || getNota().isBlank()) {
                setNota("Jornada en curso.");
            }
        } else {
            if ("Pendiente de ingreso.".equals(getNota())) {
                setNota(null);
            }
        }
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm")
    public String getHorasTrabajadasTurno() {
        if (esJornadaEspecial())
            return "00:00";

        // Minutos base:
        // Si trabajó al menos lo esperado MENOS la tolerancia, se considera jornada
        // completa (minutosEsperados).
        // Si no, se toma lo trabajado real (topeado por lo esperado).
        int minutosNormalesBase;
        if (minutosTrabajados >= (minutosEsperados - toleranciaMinutos)) {
            minutosNormalesBase = minutosEsperados;
        } else {
            minutosNormalesBase = Math.min(minutosTrabajados, minutosEsperados);
        }

        int totalMinutos = Math.max(0, minutosNormalesBase + ajusteMinutosNormales);
        return TiempoUtils.formatearMinutosComoHHMM(totalMinutos);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm-plus")
    public String getHorasExtras() {
        if (esJornadaEspecial())
            return "00:00";
        // Minutos extras calculados + Ajuste manual
        int totalExtras = Math.max(0, minutosExtras + ajusteMinutosExtras);
        return TiempoUtils.formatearMinutosComoHHMM(totalExtras);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm-multiple")
    public String getHorasEspeciales() {
        // En feriados/días no laborales, todo el tiempo es especial
        int base = esJornadaEspecial() ? minutosTrabajados : 0;
        int total = Math.max(0, base + ajusteMinutosEspeciales);
        return TiempoUtils.formatearMinutosComoHHMM(total);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHora,horasTrabajadasTurno")
    public BigDecimal getTotalHorasTurno() {
        // Priorizar valor persistido si existe
        if (montoTeoricoTurno != null)
            return montoTeoricoTurno;

        // Fallback: cálculo dinámico usando valor hora con bonificación
        BigDecimal valorHora = valorHoraTurnoSnapshot != null ? valorHoraTurnoSnapshot
                : (getEmpleado() != null ? getEmpleado().getValorHora() : null);

        return calcularTotalMonetario(getHorasTrabajadasTurno(), valorHora);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHoraExtras,horasExtras")
    public BigDecimal getTotalHorasExtras() {
        if (montoTeoricoExtras != null)
            return montoTeoricoExtras;
        return calcularTotalMonetario(getHorasExtras(),
                getEmpleado() != null ? getEmpleado().getValorHoraExtra() : null);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHoraEspeciales,horasEspeciales")
    public BigDecimal getTotalHorasEspeciales() {
        if (montoTeoricoEspeciales != null)
            return montoTeoricoEspeciales;
        return calcularTotalMonetario(getHorasEspeciales(),
                getEmpleado() != null ? getEmpleado().getValorHoraEspecial() : null);
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES
    // ==================================================================================

    @Transient
    @Label
    @LabelFormat(LabelFormatType.NO_LABEL)
    @Depends("sucursalSeleccionada, fechaHoraActual")
    public String getObservacionFeriado() {
        try {
            Feriados feriado = XPersistence.getManager()
                    .createQuery("SELECT f FROM Feriados f WHERE f.fecha = :fecha", Feriados.class)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
            return feriado.getTipo().toUpperCase() + " - " + feriado.getMotivo();
        } catch (NoResultException e) {
            return "";
        }
    }

    @Transient
    private boolean esJornadaEspecial() {
        return evaluacion == EvaluacionJornada.FERIADO_TRABAJADO ||
                evaluacion == EvaluacionJornada.DIA_NO_LABORAL_TRABAJADO;
    }

    /**
     * Muestra el rango horario real basado en las fichadas.
     */
    @Transient
    @ReadOnly
    public String getHorario() {
        if (registros == null || registros.isEmpty())
            return "Sin Registros";

        Optional<LocalTime> entrada = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                .map(ColeccionRegistros::getHora)
                .min(LocalTime::compareTo);

        Optional<LocalTime> salida = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.SALIDA)
                .map(ColeccionRegistros::getHora)
                .max(LocalTime::compareTo);

        if (entrada.isPresent() && salida.isPresent()) {
            return TiempoUtils.formatearHora(entrada.get()) + " < " + TiempoUtils.formatearHora(salida.get());
        } else if (entrada.isPresent()) {
            return "Entrada: " + TiempoUtils.formatearHora(entrada.get());
        } else if (salida.isPresent()) {
            return "Salida: " + TiempoUtils.formatearHora(salida.get());
        } else {
            return "Sin Registros";
        }
    }

    @Transient
    @ReadOnly
    @Depends("fecha")
    public String getDiaSemana() {
        return TiempoUtils.obtenerNombreDia(fecha);
    }

    @Transient
    @DisplaySize(100)
    @MiLabel(medida = "chica", negrita = true, recuadro = true, icon = "calendar-check", multiline = false, mayuscula = false)
    public String getTurnoPlanificado() {
        if (nombreTurno == null)
            return "SIN TURNO";

        String dia = TiempoUtils.obtenerNombreDia(fecha);
        String fechaStr = TiempoUtils.formatearFecha(fecha);
        String horario = "";

        if (horaEsperadaEntrada != null && horaEsperadaSalida != null) {
            horario = " de " + TiempoUtils.formatearHora(horaEsperadaEntrada) + " a " +
                    TiempoUtils.formatearHora(horaEsperadaSalida);
        }

        String toleranciaStr = (toleranciaMinutos > 0) ? " /Tol. " + toleranciaMinutos + "Min." : "";

        // Mostrar bonificación si existe
        String bonificacionStr = "";
        if (porcentajeBonificacionSnapshot != null &&
                porcentajeBonificacionSnapshot.compareTo(BigDecimal.ZERO) > 0) {
            bonificacionStr = " +" + porcentajeBonificacionSnapshot.setScale(0, RoundingMode.HALF_UP) + "%";
        }

        return dia + ", " + fechaStr + " - " + horario + toleranciaStr + bonificacionStr;
    }

    private BigDecimal calcularTotalMonetario(String horasEnFormatoHHmm, BigDecimal valorPorHora) {
        if (horasEnFormatoHHmm == null || valorPorHora == null)
            return BigDecimal.ZERO;
        try {
            String[] partes = horasEnFormatoHHmm.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);

            BigDecimal horasDecimal = BigDecimal.valueOf(horas).add(
                    BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

            return valorPorHora.multiply(horasDecimal).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // ==================================================================================
    // INDICADOR VISUAL PARA LISTA
    // ==================================================================================

    /**
     * Muestra un indicador visual del estado de la jornada para la vista de lista.
     * Permite identificar rápidamente registros que necesitan revisión o tienen
     * situaciones especiales.
     * 
     * @return String con emoji e información del estado (ej: "⏰ +2h30m Extras", "⚠️
     *         -1h Faltan")
     */
    @Transient

    @MiLabel(medida = "mediana", negrita = true, recuadro = false, mayuscula = false)
    public String getEstadoJornada() {
        // Si hay ajustes manuales, indicarlo siempre
        if (tieneAjustesManuales()) {
            return "📝 Ajustado";
        }

        // Según evaluación
        if (evaluacion == null) {
            return "";
        }

        switch (evaluacion) {
            case AUSENTE:
                return "❌ Ausente";
            case LICENCIA:
                return "📄 Licencia";
            case FERIADO:
                return "🎉 Feriado";
            case FERIADO_TRABAJADO:
            case DIA_NO_LABORAL_TRABAJADO:
                return "🌟 Especial";
            case INCOMPLETA:
                int faltantes = minutosEsperados - minutosTrabajados;
                if (faltantes > 0) {
                    return "⚠️ Faltan " + formatearMinutosCompacto(faltantes);
                }
                return "⚠️ Incompleta";
            case COMPLETA:
                if (minutosExtras > 0) {
                    return "⏰ +" + formatearMinutosCompacto(minutosExtras) + " Extras";
                }
                return "✅ Completa";
            case EN_CURSO:
                return "🔄 En curso";
            case PENDIENTE:
                return "⏱️ Pendiente";
            case DIA_NO_LABORAL:
            case SIN_TURNO_ASIGNADO:
                return "🏖️ No laboral";
            case SIN_DATOS:
                return "❓ Sin datos";
            default:
                return "";
        }
    }

    /**
     * Verifica si el registro tiene ajustes manuales aplicados.
     * 
     * @return true si hay ajustes en minutos normales , extras o especiales
     */
    @Transient
    private boolean tieneAjustesManuales() {
        return ajusteMinutosNormales != 0 || ajusteMinutosExtras != 0 || ajusteMinutosEspeciales != 0;
    }

    /**
     * Formatea minutos a formato compacto para mostrar en lista.
     * Ejemplos: "2h30m", "1h", "45m"
     * 
     * @param minutos número de minutos a formatear
     * @return String formateado (ej: "2h30m")
     */
    @Transient
    private String formatearMinutosCompacto(int minutos) {
        int minutosAbs = Math.abs(minutos);
        int horas = minutosAbs / 60;
        int mins = minutosAbs % 60;

        if (horas > 0 && mins > 0) {
            return horas + "h " + mins + "m";
        } else if (horas > 0) {
            return horas + "h";
        } else {
            return mins + "m";
        }
    }

    // ==================================================================================
    // BOTÓN Y VISUALIZACIÓN DE AJUSTES
    // ==================================================================================

    /**
     * Botón para abrir diálogo de ajustes manuales.
     * Se muestra al final de la sección Calculos_Y_Ajustes.
     */

    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "wrench")
    @Transient
    @Action("AuditoriaRegistros.ajustarHoras")
    public String botonAjustarHoras; // El texto lo pone la acción

    /**
     * Muestra los ajustes realizados solo si existen.
     * Formato: "Normales: +30m | Extras: -15m | Especiales: +60m"
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @Depends("ajusteMinutosNormales, ajusteMinutosExtras, ajusteMinutosEspeciales")
    public String getAjustesRealizados() {
        if (ajusteMinutosNormales == 0 && ajusteMinutosExtras == 0 && ajusteMinutosEspeciales == 0) {
            return ""; // No mostrar si no hay ajustes
        }

        StringBuilder sb = new StringBuilder("⚙️ Ajustes: ");
        boolean primero = true;

        if (ajusteMinutosNormales != 0) {
            sb.append("Normales: ");
            sb.append(ajusteMinutosNormales > 0 ? "+" : "");
            sb.append(formatearMinutosCompacto(ajusteMinutosNormales));
            primero = false;
        }

        if (ajusteMinutosExtras != 0) {
            if (!primero)
                sb.append(" | ");
            sb.append("Extras: ");
            sb.append(ajusteMinutosExtras > 0 ? "+" : "");
            sb.append(formatearMinutosCompacto(ajusteMinutosExtras));
            primero = false;
        }

        if (ajusteMinutosEspeciales != 0) {
            if (!primero)
                sb.append(" | ");
            sb.append("Especiales: ");
            sb.append(ajusteMinutosEspeciales > 0 ? "+" : "");
            sb.append(formatearMinutosCompacto(ajusteMinutosEspeciales));
        }

        return sb.toString();
    }

}
