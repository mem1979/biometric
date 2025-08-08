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

@Entity
@Getter @Setter
@View(members=
    "empleado;" +
    "DetalleTurno {turnoPlanificado, evaluacion, observacionFeriado;" +
    " Registros {registros};" +
    " Calculos_Y_Ajustes {"+
    "horasTrabajadasTurno, totalHorasTurno;"+
    "horasExtras, totalHorasExtras;"+
    "horasEspeciales, totalHorasEspeciales;};" +
    " Notas {nota};" +
    "}"
)

@Tab(editors = "List",
     rowStyles = {
    		 @RowStyle(style = "estilo-gris-claro",     property = "evaluacion", value = "PENDIENTE"),             // A la espera de fichadas
    		 @RowStyle(style = "estilo-gris-intenso",   property = "evaluacion", value = "EN_CURSO"),             // Jornada activa con registros
    		 @RowStyle(style = "estilo-verde-intenso",  property = "evaluacion", value = "COMPLETA"),              // Jornada cerrada correctamente
    		 @RowStyle(style = "estilo-amarillo-claro", property = "evaluacion", value = "INCOMPLETA"),            // Faltan fichadas para cerrar
    		 @RowStyle(style = "estilo-rojo-intenso",   property = "evaluacion", value = "AUSENTE"),               // Sin registros
    		 @RowStyle(style = "estilo-rojo-claro",     property = "evaluacion", value = "LICENCIA"),              // Día justificado con licencia
    		 @RowStyle(style = "estilo-azul-claro",     property = "evaluacion", value = "FERIADO"),               // Feriado común
    		 @RowStyle(style = "estilo-azul-intenso",   property = "evaluacion", value = "FERIADO_TRABAJADO"),     // Feriado pero con actividad
    		 @RowStyle(style = "estilo-verde-claro",    property = "evaluacion", value = "DIA_NO_LABORAL"),        // Día no laborable según turno
    		 @RowStyle(style = "estilo-verde-claro",    property = "evaluacion", value = "SIN_TURNO_ASIGNADO"),    // No hay turno configurado
    		 @RowStyle(style = "estilo-rojo-intenso",   property = "evaluacion", value = "SIN_DATOS")             // Sin información básica
     },
     properties = "empleado.sucursal.nombre, empleado.nombreCompleto, fecha, horario, evaluacion",
     defaultOrder = "${fecha} desc, ${empleado.sucursal.nombre} asc, ${empleado.nombreCompleto} asc"
   )

public class AuditoriaRegistros extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY)
    @ReferenceView("simple")
    @NoFrame @ReadOnly
    private Personal empleado;

    @EditOnly
    @NoDefaultActions
    @RowStyle(style = "estilo-verde-claro",      property = "evaluacion", value = "ENTRADA EN HORARIO")
    @RowStyle(style = "estilo-verde-claro",	     property = "evaluacion", value = "SALIDA EN HORARIO")
    @RowStyle(style = "estilo-verde-intenso",    property = "evaluacion", value = "ENTRADA ANTICIPADA")
    @RowStyle(style = "estilo-amarillo-intenso", property = "evaluacion", value = "SALIDA ANTICIPADA")
    @RowStyle(style = "estilo-amarillo-intenso", property = "evaluacion", value = "ENTRADA TARDE")
    @RowStyle(style = "estilo-verde-intenso",    property = "evaluacion", value = "SALIDA TARDIA")
    @RowStyle(style = "estilo-rojo-claro",       property = "evaluacion", value = "SIN HORARIO DE ENTRADA")
    @RowStyle(style = "estilo-rojo-claro",       property = "evaluacion", value = "SIN HORARIO DE SALIDA")
    @RowStyle(style = "estilo-rojo-claro",       property = "evaluacion", value = "DIA NO LABORAL")
    @RowStyle(style = "estilo-rojo-claro",       property = "evaluacion", value = "SIN TURNO ASIGNADO")
    @RowStyle(style = "estilo-azul-claro",       property = "evaluacion", value = "INICIO PAUSA")
    @RowStyle(style = "estilo-azul-claro",       property = "evaluacion", value = "FIN PAUSA")
    @RowStyle(style = "estilo-azul-claro",       property = "evaluacion", value = "UBICACION")
    @RowStyle(style = "estilo-azul-intenso",     property = "evaluacion", value = "REGISTRO MANUAL")
    @RowStyle(style = "estilo-rojo-intenso",     property = "evaluacion", value = "ERROR DE REGISTRO - SIN ASISTENCIA DIARIA")
    @RowStyle(style = "estilo-rojo-intenso",     property = "evaluacion", value = "ERROR DE REGISTRO - SIN DATOS")
    @RowStyle(style = "estilo-rojo-intenso",     property = "evaluacion", value = "ERROR DE REGISTRO - SIN EMPLEADO")
    @RowStyle(style = "estilo-rojo-intenso",     property = "evaluacion", value = "REGISTRO NO VALIDADO - TIPO DE MOVIMIENTO INCORRECTO")
    @OneToMany(mappedBy = "asistenciaDiaria", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("diaSemana, fecha, hora, evaluacion")
    private List<ColeccionRegistros> registros = new ArrayList<>();

    @Stereotype("FECHA")
    private LocalDate fecha;
    
 
    @Stereotype("HORA")
    private LocalTime horaEsperadaEntrada;

    @Stereotype("HORA")
    private LocalTime horaEsperadaSalida;

    private int minutosEsperados;
    private int minutosTrabajados;
    private int minutosExtras;

    
    @Stereotype("BOLD_LABEL")
    @LabelFormat(LabelFormatType.NO_LABEL)
    private EvaluacionJornada evaluacion;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    @DefaultValueCalculator(TrueCalculator.class)
    private boolean justificado;

    private boolean feriado;
    
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
    
    private boolean licencia;

    @TextArea
    private String nota;

    /**
     * Agrega un registro a la lista deduplicando por fecha y hora.
     * Si ya existe un registro con la misma combinacion, se reemplaza.
     *
     * @param registro Nuevo registro a incorporar
     */
    public void agregarRegistro(ColeccionRegistros registro) {
        if (registro == null) return;

        registros.removeIf(r ->
            Objects.equals(r.getFecha(), registro.getFecha()) &&
            Objects.equals(r.getHora(), registro.getHora())
        );

        registro.setAsistenciaDiaria(this);
        registros.add(registro);
    }

// ===================== ME‰TODOS PRINCIPALES =====================

    public void consolidarDesdeRegistros() {
        if (empleado == null || fecha == null) return;

        inicializarTurnoYCondiciones();

        if (registros == null || registros.isEmpty()) {
            evaluarSinRegistros();
        } else {
            calcularDuraciones();
            evaluarConRegistros();
        }
    }

    private void inicializarTurnoYCondiciones() {
        TurnosHorarios turno = empleado.getTurnoParaFecha(fecha);
        DayOfWeek dia = fecha.getDayOfWeek();

        if (turno != null) {
            horaEsperadaEntrada = turno.getEntradaParaDia(dia);
            horaEsperadaSalida = turno.getSalidaParaDia(dia);
            minutosEsperados = TiempoUtils.calcularMinutosLocalTime(horaEsperadaEntrada, horaEsperadaSalida);
        } else {
            horaEsperadaEntrada = null;
            horaEsperadaSalida = null;
            minutosEsperados = 0;
        }
    }

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

        if (getConLicencia()) {
            evaluacion = EvaluacionJornada.LICENCIA;
        } else if (getEsFeriado()) {
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

        if (getConLicencia()) {
            evaluacion = EvaluacionJornada.LICENCIA;
        } else if (getEsFeriado()) {
            evaluacion = EvaluacionJornada.FERIADO_TRABAJADO;
        } else if (!esLaboral) {
            evaluacion = EvaluacionJornada.DIA_NO_LABORAL;
        } else if (minutosTrabajados >= minutosEsperados) {
            evaluacion = EvaluacionJornada.COMPLETA;
        } else {
            evaluacion = EvaluacionJornada.INCOMPLETA;
        }
    }

// ===================== PROPIEDADES CALCULADAS =====================

    @Transient
    @ReadOnly
    public boolean getEsFeriado() {
        return Feriados.existeParaFecha(fecha);
    }

    @Transient
    @ReadOnly
    public boolean getConLicencia() {
        return Licencia.tieneLicenciaEnFecha(empleado, fecha);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm")
    public String getHorasTrabajadasTurno() {
        if (esJornadaEspecial()) return "00:00";
        int minutosNormales = Math.min(minutosTrabajados, minutosEsperados);
        return TiempoUtils.formatearMinutosComoHHMM(minutosNormales);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm-plus")
    public String getHorasExtras() {
        if (esJornadaEspecial()) return "00:00";
        return TiempoUtils.formatearMinutosComoHHMM(minutosExtras);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "alarm-multiple")
    public String getHorasEspeciales() {
        return esJornadaEspecial() ? TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados) : "00:00";
    }
    
    
    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHora,horasTrabajadasTurno")
    public BigDecimal getTotalHorasTurno() {
        return calcularTotalMonetario(getHorasTrabajadasTurno(), getEmpleado() != null ? getEmpleado().getValorHora() : null);
    }

    @Transient
    @ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHoraExtras,horasExtras")
    public BigDecimal getTotalHorasExtras() {
        return calcularTotalMonetario(getHorasExtras(), getEmpleado() != null ? getEmpleado().getValorHoraExtra() : null);
    }

    @Transient@ReadOnly
    @DisplaySize(10)
    @LabelFormat(LabelFormatType.SMALL)
    @Money
    @Depends("empleado.valorHoraEspeciales,horasEspeciales")
    public BigDecimal getTotalHorasEspeciales() {
        return calcularTotalMonetario(getHorasEspeciales(), getEmpleado() != null ? getEmpleado().getValorHoraEspecial() : null);
    }

    private BigDecimal calcularTotalMonetario(String horasEnFormatoHHmm, BigDecimal valorPorHora) {
        if (horasEnFormatoHHmm == null || valorPorHora == null) return BigDecimal.ZERO;

        try {
            String[] partes = horasEnFormatoHHmm.split(":");
            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);

            BigDecimal horasDecimal = BigDecimal.valueOf(horas).add(
                BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
            );

            return valorPorHora.multiply(horasDecimal).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    

    @Transient
    private boolean esJornadaEspecial() {
        return evaluacion == EvaluacionJornada.FERIADO_TRABAJADO ||
               evaluacion == EvaluacionJornada.DIA_NO_LABORAL;
    }

    @Transient
    @ReadOnly
    public String getHorario() {
        if (registros == null || registros.isEmpty()) return "Sin Registros";

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
    @MiLabel(medida = "chica", negrita = true, recuadro = true, icon = "calendar-check")
    public String getTurnoPlanificado() {
        return (empleado != null && fecha != null)
            ? TiempoUtils.formatearFecha(fecha) + " - " + empleado.getTurnoDescripcionParaFecha(fecha)
            : "Sin información";
    }
}
