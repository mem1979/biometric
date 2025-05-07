package com.sta.biometric.modelo;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
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
    "DetalleTurno {turnoPlanificado, evaluacion;" +
    " Registros {registros};" +
    " CalculosYAjustes {horasTrabajadasNormalesStr, horasExtrasStr, horasEspecialesStr;};" +
    " Notas {nota};" +
    "}"
)
@Tab(editors = "List",
     rowStyles = {
         @RowStyle(style="asistenciaCorrecta", property="evaluacion", value="TURNO COMPLETO"),
         @RowStyle(style="llegadaTarde", property="evaluacion", value="TURNO INCOMPLETO"),
         @RowStyle(style="ausencia", property="evaluacion", value="AUSENTE"),
         @RowStyle(style="licencia", property="evaluacion", value="LICENCIA"),
         @RowStyle(style="feriado", property="evaluacion", value="FERIADO")
     },
     properties="empleado.sucursal.nombre, empleado.nombreCompleto, fecha, horario, evaluacion",
     defaultOrder="${empleado.nombreCompleto} asc, ${fecha} desc"
)
public class AuditoriaRegistros extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY)
    @ReferenceView("simple")
    @NoFrame @ReadOnly
    private Personal empleado;

    @EditOnly
    @NoDefaultActions 
    @RowStyle(style = "asistenciaCorrecta", property = "evaluacion", value = "ENTRADA EN HORARIO")
    @RowStyle(style = "asistenciaCorrecta", property = "evaluacion", value = "SALIDA EN HORARIO")
    @RowStyle(style = "llegadaTarde", property = "evaluacion", value = "ENTRADA TARDE")
    @RowStyle(style = "llegadaTarde", property = "evaluacion", value = "SALIDA ANTICIPADA")
    @RowStyle(style = "salidaAnticipada", property = "evaluacion", value = "ENTRADA ANTICIPADA")
    @RowStyle(style = "salidaAnticipada", property = "evaluacion", value = "SALIDA TARDIA")
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

    @Enumerated(EnumType.STRING)
    @DisplaySize(20)
    @MiLabel(medida = "mediana", negrita = true, recuadro = true, icon = "calendar-search")
    private EvaluacionJornada evaluacion;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    @DefaultValueCalculator(TrueCalculator.class)
    private boolean justificado;

    private boolean feriado;
    
    private boolean licencia;

    @TextArea
    private String nota;

/*    // ===================== ME‰TODOS PRINCIPALES =====================

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
            evaluacion = EvaluacionJornada.LICENCIA_JUSTIFICADA;
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
            evaluacion = EvaluacionJornada.LICENCIA_JUSTIFICADA;
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
*/
    // ===================== PROPIEDADES CALCULADAS =====================

    @Transient @ReadOnly
    public boolean getEsFeriado() {
        return Feriados.existeParaFecha(fecha);
    }

    @Transient @ReadOnly
    public boolean getConLicencia() {
        return Licencia.tieneLicenciaEnFecha(empleado, fecha);
    }

    @Transient @ReadOnly @LargeDisplay(icon = "alarm")
    public String getHorasTrabajadasNormalesStr() {
        if (esJornadaEspecial()) return "00:00";
        int minutosNormales = Math.min(minutosTrabajados, minutosEsperados);
        return TiempoUtils.formatearMinutosComoHHMM(minutosNormales);
    }

    @Transient @ReadOnly @LargeDisplay(icon = "alarm-plus")
    public String getHorasExtrasStr() {
        if (esJornadaEspecial()) return "00:00";
        return TiempoUtils.formatearMinutosComoHHMM(minutosExtras);
    }

    @Transient @ReadOnly @LargeDisplay(icon = "alarm-multiple")
    public String getHorasEspecialesStr() {
        return esJornadaEspecial() ? TiempoUtils.formatearMinutosComoHHMM(minutosTrabajados) : "00:00";
    }

    @Transient
    private boolean esJornadaEspecial() {
        return evaluacion == EvaluacionJornada.FERIADO_TRABAJADO ||
               evaluacion == EvaluacionJornada.DIA_NO_LABORAL;
    }

    @Transient @ReadOnly
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

 /*   @PrePersist
    private void antesDeCrear() {
        setLicencia(getConLicencia());
        setFeriado(getEsFeriado());
    } */

    @Transient @ReadOnly
    @MiLabel(medida = "chica", negrita = true, recuadro = true, icon = "calendar-check")
    public String getTurnoPlanificado() {
        return (empleado != null && fecha != null)
            ? TiempoUtils.formatearFecha(fecha) + " - " + empleado.getTurnoDescripcionParaFecha(fecha)
            : "Sin informaciÃ³n";
    }
}
