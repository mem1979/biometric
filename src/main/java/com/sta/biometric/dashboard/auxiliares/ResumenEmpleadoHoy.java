package com.sta.biometric.dashboard.auxiliares;

import java.time.*;
import java.time.temporal.*;

import javax.persistence.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ResumenEmpleadoHoy {
    private Personal empleado;
    private boolean debeTrabajar;
    private boolean conLicencia;
    private boolean ingresoRealizado;
    private boolean llegadaTarde;
    private boolean salidaAnticipada;
    private EvaluacionJornada evaluacion;

    // Nuevos campos para datos dinámicos
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private LocalTime entradaEsperada;
    private LocalTime salidaEsperada;
    private String nombreTurno;
    private boolean salidaRealizada;

    // Constructor original para compatibilidad
    public ResumenEmpleadoHoy(Personal empleado, boolean debeTrabajar, boolean conLicencia,
            boolean ingresoRealizado, boolean llegadaTarde, boolean salidaAnticipada,
            EvaluacionJornada evaluacion) {
        this.empleado = empleado;
        this.debeTrabajar = debeTrabajar;
        this.conLicencia = conLicencia;
        this.ingresoRealizado = ingresoRealizado;
        this.llegadaTarde = llegadaTarde;
        this.salidaAnticipada = salidaAnticipada;
        this.evaluacion = evaluacion;
    }

    // Constructor extendido con todos los campos
    public ResumenEmpleadoHoy(Personal empleado, boolean debeTrabajar, boolean conLicencia,
            boolean ingresoRealizado, boolean llegadaTarde, boolean salidaAnticipada,
            EvaluacionJornada evaluacion, LocalTime horaEntrada, LocalTime horaSalida,
            LocalTime entradaEsperada, LocalTime salidaEsperada, String nombreTurno,
            boolean salidaRealizada) {
        this.empleado = empleado;
        this.debeTrabajar = debeTrabajar;
        this.conLicencia = conLicencia;
        this.ingresoRealizado = ingresoRealizado;
        this.llegadaTarde = llegadaTarde;
        this.salidaAnticipada = salidaAnticipada;
        this.evaluacion = evaluacion;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.entradaEsperada = entradaEsperada;
        this.salidaEsperada = salidaEsperada;
        this.nombreTurno = nombreTurno;
        this.salidaRealizada = salidaRealizada;
    }

    @Transient
    public String getEmpleadoNombre() {
        return empleado != null ? empleado.getNombreCompleto() : "";
    }

    @Transient
    public String getSucursalNombre() {
        return (empleado != null && empleado.getSucursal() != null) ? empleado.getSucursal().getNombre() : "";
    }

    @Transient
    public String getTurnoStr() {
        return nombreTurno != null && !nombreTurno.isEmpty() ? nombreTurno : "Sin turno";
    }

    @Transient
    public String getHoraEntradaStr() {
        if (!debeTrabajar)
            return "N/A";
        if (conLicencia)
            return "Licencia";
        if (horaEntrada != null) {
            String hora = String.format("%02d:%02d", horaEntrada.getHour(), horaEntrada.getMinute());
            return llegadaTarde ? hora + " ⚠️" : hora;
        }
        return "Pendiente";
    }

    @Transient
    public String getHoraSalidaStr() {
        if (!debeTrabajar)
            return "N/A";
        if (conLicencia)
            return "Licencia";
        if (!ingresoRealizado)
            return "-";
        if (horaSalida != null) {
            String hora = String.format("%02d:%02d", horaSalida.getHour(), horaSalida.getMinute());
            return salidaAnticipada ? hora + " ⚠️" : hora;
        }
        return "En curso 🔵";
    }

    @Transient
    public String getEstadoIcono() {
        if (evaluacion == null)
            return "❓ Sin datos";
        switch (evaluacion) {
            case COMPLETA:
                return "🟢 Completa";
            case EN_CURSO:
                return "🔵 En curso";
            case INCOMPLETA:
                return "⚠️ Incompleta";
            case PENDIENTE:
                return "⏳ Pendiente";
            case AUSENTE:
                return "🔴 Ausente";
            case LICENCIA:
                return "📋 Licencia";
            case FERIADO:
                return "🎉 Feriado";
            case FERIADO_TRABAJADO:
                return "🎉⚡ Feriado trabajado";
            case DIA_NO_LABORAL:
                return "🏖️ No laboral";
            case DIA_NO_LABORAL_TRABAJADO:
                return "🏖️⚡ No laboral trabajado";
            case SIN_TURNO_ASIGNADO:
                return "📭 Sin turno";
            default:
                return "❓ " + evaluacion.toString();
        }
    }

    @Transient
    public String getTiempoTranscurrido() {
        if (!ingresoRealizado || horaEntrada == null)
            return "-";

        LocalTime hasta = salidaRealizada && horaSalida != null ? horaSalida : LocalTime.now();
        long minutos = ChronoUnit.MINUTES.between(horaEntrada, hasta);

        if (minutos < 0)
            minutos = 0;
        long horas = minutos / 60;
        long mins = minutos % 60;

        return String.format("%dh %02dm", horas, mins);
    }

    // Métodos legacy para compatibilidad
    @Transient
    public String getIngresoRealizadoStr() {
        return debeTrabajar ? (ingresoRealizado ? "Sí" : "No") : "N/A";
    }

    @Transient
    public String getLlegadaTardeStr() {
        return debeTrabajar && ingresoRealizado ? (llegadaTarde ? "Sí" : "No") : "N/A";
    }

    @Transient
    public String getSalidaAnticipadaStr() {
        return debeTrabajar && ingresoRealizado ? (salidaAnticipada ? "Sí" : "No") : "N/A";
    }
}
