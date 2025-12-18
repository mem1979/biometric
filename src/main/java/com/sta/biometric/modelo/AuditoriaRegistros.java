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
        "Normales [ valorHoraNormalDisplay, horasTrabajadasTurno, ajusteNormalesDisplay, totalHorasTurno ]; " +
        "Extras [ valorHoraExtraDisplay, horasExtras, ajusteExtrasDisplay, totalHorasExtras ]; " +
        "Especiales [ valorHoraEspecialDisplay, horasEspeciales, ajusteEspecialesDisplay, totalHorasEspeciales ]; " +
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
    @Hidden
    private int ajusteMinutosNormales; // Minutos a sumar/restar a normales

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Hidden
    private int ajusteMinutosExtras; // Minutos a sumar/restar a extras

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Hidden
    private int ajusteMinutosEspeciales; // Minutos a sumar/restar a especiales

    @Stereotype("MEMO")
    @Column(length = 2000)
    private String nota; // Observaciones generales

    // ==================================================================================
    // LÓGICA PRINCIPAL DE NEGOCIO
    // ==================================================================================

    /**
     * Método central que procesa la información y determina el estado de la
     * jornada.
     * 
     * <p>
     * Se ejecuta cada vez que se agregan fichadas o se recalcula el registro.
     * Realiza las siguientes operaciones:
     * </p>
     * <ol>
     * <li>Inicializa turno y condiciones</li>
     * <li>Calcula duraciones (minutos trabajados)</li>
     * <li>Evalúa el estado de la jornada</li>
     * <li>Actualiza notas según evaluación</li>
     * </ol>
     * 
     * @see #inicializarTurnoYCondiciones()
     * @see #calcularDuraciones()
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
     * 
     * <p>
     * Asegura la inmutabilidad histórica guardando una "foto" de los valores
     * al momento del registro (valor hora, tolerancia, bonificaciones).
     * </p>
     * 
     * @see Personal#getTurnoParaFecha(LocalDate)
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
     * 
     * <p>
     * Descuenta pausas si están registradas.
     * </p>
     */
    private void calcularDuraciones() {
        registros.sort(Comparator.comparing(ColeccionRegistros::getHora));

        LocalTime inicio = registros.get(0).getHora();
        LocalTime fin = registros.get(registros.size() - 1).getHora();

        minutosTrabajados = TiempoUtils.calcularMinutosLocalTime(inicio, fin);
        minutosExtras = Math.max(0, minutosTrabajados - minutosEsperados);
    }

    /**
     * Evalúa la jornada cuando no hay fichadas registradas.
     * 
     * <p>
     * Determina si es:
     * </p>
     * <ul>
     * <li>FERIADO - si la fecha es feriado nacional</li>
     * <li>LICENCIA - si hay licencia activa</li>
     * <li>AUSENTE - si era día laboral sin registros</li>
     * <li>DIA_LIBRE - si no era día laboral</li>
     * </ul>
     * 
     * @see EvaluacionJornada
     */
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
            // Si el día es hoy y aún no terminó la jornada, es PENDIENTE
            if (fecha.equals(LocalDate.now())) {
                evaluacion = EvaluacionJornada.PENDIENTE;
            } else {
                evaluacion = EvaluacionJornada.AUSENTE;
            }
        }
    }

    /**
     * Evalúa la jornada cuando hay fichadas registradas.
     * 
     * <p>
     * Determina:
     * </p>
     * <ul>
     * <li>COMPLETA - si cumplió las horas del turno</li>
     * <li>INCOMPLETA - si trabajó pero no completó las horas</li>
     * <li>FERIADO_TRABAJADO - si trabajó en un día feriado</li>
     * </ul>
     * 
     * @see EvaluacionJornada
     */
    private void evaluarConRegistros() {
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        boolean esLaboral = turno != null && turno.esLaboral(fecha.getDayOfWeek());

        // Verificar si tiene entrada pero no salida
        boolean tieneEntrada = registros.stream()
                .anyMatch(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA);
        boolean tieneSalida = registros.stream()
                .anyMatch(r -> r.getTipoMovimiento() == TipoMovimiento.SALIDA);

        if (licencia) {
            evaluacion = EvaluacionJornada.LICENCIA;
        } else if (feriado) {
            evaluacion = EvaluacionJornada.FERIADO_TRABAJADO;
        } else if (!esLaboral) {
            evaluacion = EvaluacionJornada.DIA_NO_LABORAL_TRABAJADO;
        } else if (tieneEntrada && !tieneSalida && fecha.equals(LocalDate.now())) {
            // Tiene entrada pero no salida, y es hoy → EN_CURSO
            evaluacion = EvaluacionJornada.EN_CURSO;
        } else if (minutosTrabajados >= (minutosEsperados - toleranciaMinutos)) {
            evaluacion = EvaluacionJornada.COMPLETA;
        } else {
            evaluacion = EvaluacionJornada.INCOMPLETA;
        }
    }

    /**
     * Genera notas automáticas basadas en la evaluación de la jornada.
     * 
     * <p>
     * Proporciona información detallada según el estado: horarios, tiempos
     * trabajados, diferencias, motivos de licencia/feriado, etc.
     * </p>
     */
    public void actualizarNotaSegunEvaluacion() {
        if (evaluacion == null) {
            setNota("Sin evaluación disponible.");
            return;
        }

        switch (evaluacion) {
            case LICENCIA:
                generarNotaLicencia();
                break;

            case FERIADO:
                generarNotaFeriado();
                break;

            case FERIADO_TRABAJADO:
                generarNotaFeriadoTrabajado();
                break;

            case DIA_NO_LABORAL:
                generarNotaDiaNoLaboral();
                break;

            case DIA_NO_LABORAL_TRABAJADO:
                generarNotaDiaNoLaboralTrabajado();
                break;

            case PENDIENTE:
                generarNotaPendiente();
                break;

            case EN_CURSO:
                generarNotaEnCurso();
                break;

            case COMPLETA:
                generarNotaCompleta();
                break;

            case INCOMPLETA:
                generarNotaIncompleta();
                break;

            case AUSENTE:
                generarNotaAusente();
                break;

            case SIN_TURNO_ASIGNADO:
                setNota("El empleado no tiene un turno asignado para esta fecha.");
                break;

            case SIN_DATOS:
                setNota("No hay datos de asistencia registrados para procesar.");
                break;

            default:
                setNota("Estado: " + evaluacion.getDescripcion());
        }
    }

    // ==================================================================================
    // GENERADORES DE NOTAS DETALLADAS POR ESTADO
    // ==================================================================================

    private void generarNotaLicencia() {
        Licencia licenciaDetalle = Licencia.getLicenciaEnFecha(empleado, fecha);
        if (licenciaDetalle != null) {
            String tipoDesc = licenciaDetalle.getTipo() != null
                    ? licenciaDetalle.getTipo().toString()
                    : "No especificado";
            String justificadaStr = licenciaDetalle.isJustificado() ? "Justificada" : "No justificada";
            String observacion = licenciaDetalle.getObservacion() != null && !licenciaDetalle.getObservacion().isBlank()
                    ? " - " + licenciaDetalle.getObservacion()
                    : "";
            setNota(String.format("📋 Licencia %s (%s)%s", tipoDesc, justificadaStr, observacion));
        } else {
            setNota("📋 Licencia activa para esta fecha.");
        }
    }

    private void generarNotaFeriado() {
        String observacion = getObservacionFeriado();
        if (observacion != null && !observacion.isBlank()) {
            setNota("🎉 " + observacion + " - Día no laboral.");
        } else {
            setNota("🎉 Día feriado nacional. No se requiere asistencia.");
        }
    }

    private void generarNotaFeriadoTrabajado() {
        String observacion = getObservacionFeriado();
        String horasTrabajadas = TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados);
        StringBuilder sb = new StringBuilder("🌟 Feriado trabajado");
        if (observacion != null && !observacion.isBlank()) {
            sb.append(" (").append(observacion).append(")");
        }
        sb.append(". Horas especiales: ").append(horasTrabajadas);
        sb.append(". Se aplica bonificación de horas especiales.");
        setNota(sb.toString());
    }

    private void generarNotaDiaNoLaboral() {
        String dia = TiempoUtils.obtenerNombreDia(fecha);
        setNota("🏖️ " + dia + " no es día laboral según el turno asignado. No se requiere asistencia.");
    }

    private void generarNotaDiaNoLaboralTrabajado() {
        String dia = TiempoUtils.obtenerNombreDia(fecha);
        String horasTrabajadas = TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados);
        setNota("🌟 Trabajo en día no laboral (" + dia + "). Horas especiales registradas: " + horasTrabajadas
                + ". Se aplica bonificación.");
    }

    private void generarNotaPendiente() {
        StringBuilder sb = new StringBuilder("⏳ Pendiente de ingreso.");
        if (horaEsperadaEntrada != null) {
            sb.append(" Turno programado: ");
            sb.append(TiempoUtils.formatearHora(horaEsperadaEntrada));
            if (horaEsperadaSalida != null) {
                sb.append(" a ").append(TiempoUtils.formatearHora(horaEsperadaSalida));
            }
            sb.append(".");
        }
        if (nombreTurno != null) {
            sb.append(" (").append(nombreTurno).append(")");
        }
        setNota(sb.toString());
    }

    private void generarNotaEnCurso() {
        StringBuilder sb = new StringBuilder("🔄 Jornada en curso.");

        // Obtener hora de entrada registrada
        Optional<LocalTime> entrada = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                .map(ColeccionRegistros::getHora)
                .min(LocalTime::compareTo);

        if (entrada.isPresent()) {
            sb.append(" Ingreso: ").append(TiempoUtils.formatearHora(entrada.get())).append(".");

            // Evaluar si llegó a horario o tarde
            if (horaEsperadaEntrada != null) {
                long diferencia = java.time.Duration.between(horaEsperadaEntrada, entrada.get()).toMinutes();
                if (diferencia > toleranciaMinutos) {
                    sb.append(" ⚠️ Llegada tarde: +").append(diferencia).append(" min.");
                } else if (diferencia < -toleranciaMinutos) {
                    sb.append(" ✓ Llegada anticipada: ").append(Math.abs(diferencia)).append(" min antes.");
                } else {
                    sb.append(" ✓ Llegada en horario.");
                }
            }
        }

        if (horaEsperadaSalida != null) {
            sb.append(" Salida esperada: ").append(TiempoUtils.formatearHora(horaEsperadaSalida)).append(".");
        }

        setNota(sb.toString());
    }

    private void generarNotaCompleta() {
        StringBuilder sb = new StringBuilder("✅ Jornada completa.");

        String horasTrabajadas = TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados);
        String horasEsperadas = TiempoUtils.formatearMinutosComoHHMM(minutosEsperados);
        sb.append(" Trabajó ").append(horasTrabajadas);
        sb.append(" de ").append(horasEsperadas).append(" esperadas.");

        if (minutosExtras > 0) {
            String horasExtrasStr = TiempoUtils.formatearMinutosComoHHMM(minutosExtras);
            sb.append(" ⏰ Horas extras: +").append(horasExtrasStr).append(".");
        }

        // Agregar horario real
        Optional<LocalTime> entrada = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                .map(ColeccionRegistros::getHora)
                .min(LocalTime::compareTo);
        Optional<LocalTime> salida = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.SALIDA)
                .map(ColeccionRegistros::getHora)
                .max(LocalTime::compareTo);

        if (entrada.isPresent() && salida.isPresent()) {
            sb.append(" Horario: ").append(TiempoUtils.formatearHora(entrada.get()));
            sb.append(" - ").append(TiempoUtils.formatearHora(salida.get())).append(".");
        }

        setNota(sb.toString());
    }

    private void generarNotaIncompleta() {
        StringBuilder sb = new StringBuilder("⚠️ Jornada incompleta.");

        String horasTrabajadas = TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados);
        String horasEsperadas = TiempoUtils.formatearMinutosComoHHMM(minutosEsperados);
        int faltantes = minutosEsperados - minutosTrabajados;
        String horasFaltantes = TiempoUtils.formatearMinutosComoHHMM(Math.max(0, faltantes));

        sb.append(" Trabajó ").append(horasTrabajadas);
        sb.append(" de ").append(horasEsperadas).append(" esperadas.");
        sb.append(" Faltan: ").append(horasFaltantes).append(".");

        // Agregar horario real
        Optional<LocalTime> entrada = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                .map(ColeccionRegistros::getHora)
                .min(LocalTime::compareTo);
        Optional<LocalTime> salida = registros.stream()
                .filter(r -> r.getTipoMovimiento() == TipoMovimiento.SALIDA)
                .map(ColeccionRegistros::getHora)
                .max(LocalTime::compareTo);

        if (entrada.isPresent() && salida.isPresent()) {
            sb.append(" Horario registrado: ").append(TiempoUtils.formatearHora(entrada.get()));
            sb.append(" - ").append(TiempoUtils.formatearHora(salida.get())).append(".");

            // Detectar causa probable
            if (horaEsperadaEntrada != null) {
                long llegadaTarde = java.time.Duration.between(horaEsperadaEntrada, entrada.get()).toMinutes();
                if (llegadaTarde > toleranciaMinutos) {
                    sb.append(" Llegada tarde: +").append(llegadaTarde).append(" min.");
                }
            }
            if (horaEsperadaSalida != null) {
                long salidaAnticipada = java.time.Duration.between(salida.get(), horaEsperadaSalida).toMinutes();
                if (salidaAnticipada > toleranciaMinutos) {
                    sb.append(" Salida anticipada: -").append(salidaAnticipada).append(" min.");
                }
            }
        }

        setNota(sb.toString());
    }

    private void generarNotaAusente() {
        StringBuilder sb = new StringBuilder("❌ Ausente.");

        if (nombreTurno != null && horaEsperadaEntrada != null && horaEsperadaSalida != null) {
            sb.append(" Turno asignado era ");
            sb.append(nombreTurno).append(": ");
            sb.append(TiempoUtils.formatearHora(horaEsperadaEntrada));
            sb.append(" a ").append(TiempoUtils.formatearHora(horaEsperadaSalida)).append(".");
        }

        if (justificado) {
            sb.append(" (Justificado)");
        } else {
            sb.append(" Sin justificación registrada.");
        }

        setNota(sb.toString());
    }

    /**
     * Retorna las horas trabajadas dentro del horario normal del turno.
     * 
     * @return Horas normales en formato "HH:MM"
     */
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

    /**
     * Retorna las horas extras trabajadas (fuera del horario normal).
     * 
     * @return Horas extras en formato "HH:MM"
     */
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

    /**
     * Retorna las horas trabajadas en días especiales (feriados, domingos).
     * 
     * @return Horas especiales en formato "HH:MM"
     */
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

    /**
     * Calcula el monto total por horas normales trabajadas.
     * Siempre calcula dinámicamente usando las horas (con ajustes) × valorHora.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("horasTrabajadasTurno,ajusteMinutosNormales")
    public BigDecimal getTotalHorasTurno() {
        // Usar valor hora del turno (snapshot) o base
        BigDecimal valorHora = valorHoraTurnoSnapshot != null ? valorHoraTurnoSnapshot
                : valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;

        return calcularTotalMonetario(getHorasTrabajadasTurno(), valorHora);
    }

    /**
     * Calcula el monto total por horas extras.
     * Siempre calcula dinámicamente usando las horas (con ajustes) ×
     * valorHoraExtra.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("horasExtras,ajusteMinutosExtras")
    public BigDecimal getTotalHorasExtras() {
        // Calcular valor hora extra desde snapshot base
        BigDecimal baseHora = valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;
        if (baseHora.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        // Porcentaje extra: usar del empleado o default 50%
        BigDecimal porcentajeExtra = getEmpleado() != null && getEmpleado().getPorcentajeHoraExtra() != null
                ? getEmpleado().getPorcentajeHoraExtra()
                : new BigDecimal("50");

        BigDecimal valorHoraExtra = baseHora.multiply(
                BigDecimal.ONE.add(porcentajeExtra.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        return calcularTotalMonetario(getHorasExtras(), valorHoraExtra);
    }

    /**
     * Calcula el monto total por horas especiales.
     * Siempre calcula dinámicamente usando las horas (con ajustes) ×
     * valorHoraEspecial.
     */
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("horasEspeciales,ajusteMinutosEspeciales")
    public BigDecimal getTotalHorasEspeciales() {
        // Calcular valor hora especial desde snapshot base
        BigDecimal baseHora = valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;
        if (baseHora.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        // Porcentaje especial: usar del empleado o default 100%
        BigDecimal porcentajeEspecial = getEmpleado() != null && getEmpleado().getPorcentajeHoraEspecial() != null
                ? getEmpleado().getPorcentajeHoraEspecial()
                : new BigDecimal("100");

        BigDecimal valorHoraEspecial = baseHora.multiply(
                BigDecimal.ONE.add(porcentajeEspecial.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        return calcularTotalMonetario(getHorasEspeciales(), valorHoraEspecial);
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES
    // ==================================================================================

    /**
     * Retorna la descripción del feriado si la fecha corresponde a uno.
     * 
     * @return Descripción del feriado o cadena vacía
     */
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

    /**
     * Verifica si la jornada corresponde a un día especial (solo feriados).
     * 
     * <p>
     * Según Ley de Contrato de Trabajo Argentina (Art. 201):
     * - Solo los feriados nacionales trabajados aplican como horas especiales
     * (extras al 100%)
     * - Los días sin turno asignado trabajados se computan como horas extras
     * normales (50%)
     * </p>
     * 
     * @return true si es feriado trabajado con bonificación especial
     */
    @Transient
    private boolean esJornadaEspecial() {
        return evaluacion == EvaluacionJornada.FERIADO_TRABAJADO;
    }

    /**
     * Muestra el rango horario real basado en las fichadas.
     * 
     * <p>
     * Formato: "HH:MM - HH:MM" (entrada - salida)
     * </p>
     * 
     * @return Rango horario o mensaje de estado si faltan fichadas
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

    /**
     * Retorna el día de la semana en español.
     * 
     * @return Nombre del día (ej: "LUNES", "MARTES")
     */
    @Transient
    @ReadOnly
    @Depends("fecha")
    public String getDiaSemana() {
        return TiempoUtils.obtenerNombreDia(fecha);
    }

    /**
     * Retorna la descripción del turno planificado para la fecha.
     * 
     * <p>
     * Incluye código del turno, horario esperado y tolerancia.
     * </p>
     * 
     * @return Descripción del turno o "Sin turno asignado"
     */
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

    /**
     * Calcula el monto monetario dado un tiempo y valor hora.
     * 
     * @param horasEnFormatoHHmm Tiempo en formato "HH:MM"
     * @param valorPorHora       Valor monetario por hora
     * @return Monto calculado (horas × valor)
     */
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
     * 
     * <p>
     * Permite identificar rápidamente registros que necesitan revisión o tienen
     * situaciones especiales.
     * </p>
     * 
     * <p>
     * Ejemplos de salida:
     * </p>
     * <ul>
     * <li>"⏰ +2h30m Extras" - horas extras trabajadas</li>
     * <li>"⚠️ -1h Faltan" - horas pendientes</li>
     * <li>"✅ Completa" - jornada cumplida</li>
     * <li>"🏖️ Feriado" - día feriado</li>
     * </ul>
     * 
     * @return String con emoji e información del estado
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
     * <p>
     * Los ajustes manuales permiten corregir errores de fichado
     * o agregar tiempo no registrado automáticamente.
     * </p>
     * 
     * @return true si hay ajustes en minutos normales, extras o especiales
     * @see AjusteHorasManual
     */
    @Transient
    private boolean tieneAjustesManuales() {
        return ajusteMinutosNormales != 0 || ajusteMinutosExtras != 0 || ajusteMinutosEspeciales != 0;
    }

    /**
     * Formatea minutos a formato compacto para mostrar en lista.
     * 
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     * <li>150 → "2h30m"</li>
     * <li>60 → "1h"</li>
     * <li>45 → "45m"</li>
     * </ul>
     * 
     * @param minutos número de minutos a formatear
     * @return String formateado
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
    // PROPIEDADES DE VISUALIZACIÓN PARA TABLA (CALCULOS Y AJUSTES)
    // ==================================================================================

    /**
     * Muestra el valor hora normal para este registro (snapshot histórico).
     * Usa valorHoraTurnoSnapshot que incluye bonificación del turno.
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    public BigDecimal getValorHoraNormalDisplay() {
        // Usar snapshot si existe, fallback al snapshot base
        if (valorHoraTurnoSnapshot != null) {
            return valorHoraTurnoSnapshot;
        }
        return valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;
    }

    /**
     * Muestra el valor hora extra para este registro (calculado desde snapshot).
     * Calcula: valorHoraSnapshot × (1 + porcentajeExtra/100)
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    public BigDecimal getValorHoraExtraDisplay() {
        BigDecimal baseHora = valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;
        if (baseHora.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        // Porcentaje extra típico: 50% según LCT Argentina
        BigDecimal porcentajeExtra = getEmpleado() != null && getEmpleado().getPorcentajeHoraExtra() != null
                ? getEmpleado().getPorcentajeHoraExtra()
                : new BigDecimal("50");

        BigDecimal multiplicador = BigDecimal.ONE
                .add(porcentajeExtra.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP));
        return baseHora.multiply(multiplicador).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Muestra el valor hora especial para este registro (calculado desde snapshot).
     * Calcula: valorHoraSnapshot × (1 + porcentajeEspecial/100)
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    public BigDecimal getValorHoraEspecialDisplay() {
        BigDecimal baseHora = valorHoraSnapshot != null ? valorHoraSnapshot : BigDecimal.ZERO;
        if (baseHora.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        // Porcentaje especial típico: 100% según LCT Argentina
        BigDecimal porcentajeEspecial = getEmpleado() != null && getEmpleado().getPorcentajeHoraEspecial() != null
                ? getEmpleado().getPorcentajeHoraEspecial()
                : new BigDecimal("100");

        BigDecimal multiplicador = BigDecimal.ONE
                .add(porcentajeEspecial.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP));
        return baseHora.multiply(multiplicador).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Muestra el ajuste de minutos normales formateado.
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @DisplaySize(8)
    public String getAjusteNormalesDisplay() {
        return TiempoUtils.formatearMinutosConSigno(ajusteMinutosNormales);
    }

    /**
     * Muestra el ajuste de minutos extras formateado.
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @DisplaySize(8)
    public String getAjusteExtrasDisplay() {
        return TiempoUtils.formatearMinutosConSigno(ajusteMinutosExtras);
    }

    /**
     * Muestra el ajuste de minutos especiales formateado.
     */
    @Transient
    @ReadOnly
    @LabelFormat(LabelFormatType.SMALL)
    @DisplaySize(8)
    public String getAjusteEspecialesDisplay() {
        return TiempoUtils.formatearMinutosConSigno(ajusteMinutosEspeciales);
    }

}
