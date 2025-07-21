package com.sta.biometric.auxiliares;

import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.acciones.*;
import com.sta.biometric.anotaciones.*;
import com.sta.biometric.calculadores.*;
import com.sta.biometric.enums.*;
import com.sta.biometric.formateadores.*;

import lombok.*;

@View(members =
    "Jornada [" +
    "turnoNombre, tolerancia;"+
    "codigo, calculaTotalHoras;" +
    "];" +

    "DIAS [#" +
    "lunes, horaEntradaLunes, horaSalidaLunes, horasLunes;" +
    "martes, horaEntradaMartes, horaSalidaMartes, horasMartes;" +
    "miercoles, horaEntradaMiercoles, horaSalidaMiercoles, horasMiercoles;" +
    "jueves, horaEntradaJueves, horaSalidaJueves, horasJueves;" +
    "viernes, horaEntradaViernes, horaSalidaViernes, horasViernes;" +
    "sabado, horaEntradaSabado, horaSalidaSabado, horasSabado;" +
    "domingo, horaEntradaDomingo, horaSalidaDomingo, horasDomingo;" +
    "];" +
    "detalleJornadaHoras"
)

@Tab(editors = "List", properties = "codigo, turnoNombre.nombre, detalleJornadaHoras, totalHoras")
@Entity
@Getter @Setter
public class TurnosHorarios extends Identifiable {

    @ReadOnly
    @Required
    @SearchKey
    @MiLabel(medida = "grande", negrita = true, recuadro = true, icon = "timetable")
    @Column(length = 6, unique = true)
    private String codigo;

    
    @LabelFormat(LabelFormatType.SMALL)
    @Enumerated(EnumType.STRING)
    private Turnos turnoNombre;

    @DefaultValueCalculator(
        value = CalculadorDefaultFromProperties.class,
        properties = {
            @PropertyValue(name = "propiedad", value = "tolerancia.minutos"),
            @PropertyValue(name = "valorPorDefecto", value = "5"),
            @PropertyValue(name = "tipo", value = "int")
        }
    )
    private Integer tolerancia;

    // D�as activos (checkbox)
    @Column
    @OnChange(TurnosHorariosOnChangeDiaAction.class)
    @DefaultValueCalculator(FalseCalculator.class)
    private boolean lunes, martes, miercoles, jueves, viernes, sabado, domingo;

    // Horarios por día
    private LocalTime horaEntradaLunes, horaSalidaLunes;
    private LocalTime horaEntradaMartes, horaSalidaMartes;
    private LocalTime horaEntradaMiercoles, horaSalidaMiercoles;
    private LocalTime horaEntradaJueves, horaSalidaJueves;
    private LocalTime horaEntradaViernes, horaSalidaViernes;
    private LocalTime horaEntradaSabado, horaSalidaSabado;
    private LocalTime horaEntradaDomingo, horaSalidaDomingo;

    @Column
    private BigDecimal totalHoras;

    // ========== C�lculo de duraci�n por dia ==========

    @DisplaySize(20)
    @Depends("lunes, horaEntradaLunes, horaSalidaLunes")
    public String getHorasLunes() {
        return calcularDuracionFormateada(lunes, horaEntradaLunes, horaSalidaLunes);
    }

    @DisplaySize(20)
    @Depends("martes, horaEntradaMartes, horaSalidaMartes")
    public String getHorasMartes() {
        return calcularDuracionFormateada(martes, horaEntradaMartes, horaSalidaMartes);
    }

    @DisplaySize(20)
    @Depends("miercoles, horaEntradaMiercoles, horaSalidaMiercoles")
    public String getHorasMiercoles() {
        return calcularDuracionFormateada(miercoles, horaEntradaMiercoles, horaSalidaMiercoles);
    }

    @DisplaySize(20)
    @Depends("jueves, horaEntradaJueves, horaSalidaJueves")
    public String getHorasJueves() {
        return calcularDuracionFormateada(jueves, horaEntradaJueves, horaSalidaJueves);
    }

    @DisplaySize(20)
    @Depends("viernes, horaEntradaViernes, horaSalidaViernes")
    public String getHorasViernes() {
        return calcularDuracionFormateada(viernes, horaEntradaViernes, horaSalidaViernes);
    }

    @DisplaySize(20)
    @Depends("sabado, horaEntradaSabado, horaSalidaSabado")
    public String getHorasSabado() {
        return calcularDuracionFormateada(sabado, horaEntradaSabado, horaSalidaSabado);
    }

    @DisplaySize(20)
    @Depends("domingo, horaEntradaDomingo, horaSalidaDomingo")
    public String getHorasDomingo() {
        return calcularDuracionFormateada(domingo, horaEntradaDomingo, horaSalidaDomingo);
    }

    private String calcularDuracionFormateada(boolean diaActivo, LocalTime entrada, LocalTime salida) {
        if (!diaActivo || entrada == null || salida == null) return "0 Hs. 0 Min.";
        int minutos = TiempoUtils.calcularMinutosLocalTime(entrada, salida);
        return (minutos / 60) + " Hs. " + (minutos % 60) + " Min.";
    }

    // ========== Total semanal ==========

    @Depends("horasLunes, horasMartes, horasMiercoles, horasJueves, horasViernes, horasSabado, horasDomingo")
   // @LargeDisplay(icon = "timetable")
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "clock")
    @LabelFormat(LabelFormatType.NO_LABEL)
    public String getCalculaTotalHoras() {
        int totalMinutos = obtenerTotalMinutosSemanales();
        return (totalMinutos / 60) + " Hs. " + (totalMinutos % 60) + " Min.";
    }

    @Label
    @LabelFormat(LabelFormatType.NO_LABEL)
    public BigDecimal getTotalHorasDecimal() {
        int totalMinutos = obtenerTotalMinutosSemanales();
        return BigDecimal.valueOf(totalMinutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int obtenerTotalMinutosSemanales() {
        return Arrays.asList(
            getHorasLunes(), getHorasMartes(), getHorasMiercoles(),
            getHorasJueves(), getHorasViernes(), getHorasSabado(), getHorasDomingo()
        ).stream()
         .mapToInt(this::extraerMinutosDesdeTexto)
         .sum();
    }

    private int extraerMinutosDesdeTexto(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        String[] partes = texto.split(" ");
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[2]);
    }

    // ========== Resumen visual de jornada ==========

    @Depends("lunes, horaEntradaLunes, horaSalidaLunes, martes, horaEntradaMartes, horaSalidaMartes, " +
             "miercoles, horaEntradaMiercoles, horaSalidaMiercoles, jueves, horaEntradaJueves, horaSalidaJueves, " +
             "viernes, horaEntradaViernes, horaSalidaViernes, sabado, horaEntradaSabado, horaSalidaSabado, " +
             "domingo, horaEntradaDomingo, horaSalidaDomingo")
    @TextArea
    @LabelFormat(LabelFormatType.SMALL)
    public String getDetalleJornadaHoras() {
        Map<String, String> horariosDias = new LinkedHashMap<>();
        agregarDia(horariosDias, "Lu.", lunes, horaEntradaLunes, horaSalidaLunes);
        agregarDia(horariosDias, "Ma.", martes, horaEntradaMartes, horaSalidaMartes);
        agregarDia(horariosDias, "Mi.", miercoles, horaEntradaMiercoles, horaSalidaMiercoles);
        agregarDia(horariosDias, "Ju.", jueves, horaEntradaJueves, horaSalidaJueves);
        agregarDia(horariosDias, "Vi.", viernes, horaEntradaViernes, horaSalidaViernes);
        agregarDia(horariosDias, "Sa.", sabado, horaEntradaSabado, horaSalidaSabado);
        agregarDia(horariosDias, "Do.", domingo, horaEntradaDomingo, horaSalidaDomingo);

        StringBuilder resultado = new StringBuilder();
        for (Map.Entry<String, String> entry : horariosDias.entrySet()) {
            if (resultado.length() > 0) resultado.append(" / ");
            resultado.append(entry.getValue()).append(" de ").append(entry.getKey()).append(" Hs");
        }
        return resultado.toString();
    }

    private void agregarDia(Map<String, String> mapa, String dia, boolean activo, LocalTime entrada, LocalTime salida) {
        if (activo && entrada != null && salida != null) {
            String horario = String.format("%02d:%02d a %02d:%02d",
                entrada.getHour(), entrada.getMinute(), salida.getHour(), salida.getMinute());
            mapa.merge(horario, dia, (dias, nuevoDia) -> dias + nuevoDia);
        }
    }

    // ========== Persistencia ==========

    @PrePersist
    private void preGuardar() {
        setTotalHoras(getTotalHorasDecimal());
        generarCodigo();
    }

    @PreUpdate
    private void preActualizar() {
        setTotalHoras(getTotalHorasDecimal());
    }

    private void generarCodigo() {
        if (turnoNombre == null ) {
            throw new IllegalStateException("El nombre del turno no puede estar vac�o.");
        }

        String inicial = turnoNombre.name().substring(0, 1).toUpperCase();
        String prefijo = "T" + inicial;

        Query q = XPersistence.getManager().createQuery(
            "select max(e.codigo) from " + getClass().getSimpleName() +
            " e where e.codigo like :codigo");
        q.setParameter("codigo", prefijo + ".%");

        String ultimo = (String) q.getSingleResult();
        int nro;

        if (ultimo == null) {
            nro = 1;
        } else {
            nro = Integer.parseInt(ultimo.substring(3)) + 1;
            if (nro > 10) {
                throw new IllegalStateException("Se ha alcanzado el m�ximo de 10 c�digos para el turno: " + turnoNombre);
            }
        }

        this.codigo = prefijo + "." + String.format("%02d", nro);
    }

    // ========== Utilidades externas ==========
    
    /**
     * Devuelve el día especificado.
     * Si el día no está activo o no hay horario definido, devuelve Null.
     */

    public boolean esLaboral(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return lunes;
            case TUESDAY: return martes;
            case WEDNESDAY: return miercoles;
            case THURSDAY: return jueves;
            case FRIDAY: return viernes;
            case SATURDAY: return sabado;
            case SUNDAY: return domingo;
            default: return false;
        }
    }

    /**
     * Devuelve la Hora de Entrada para el día especificado.
     * Si el día no está activo o no hay horario definido, devuelve Null.
     */
    public LocalTime getEntradaParaDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return horaEntradaLunes;
            case TUESDAY: return horaEntradaMartes;
            case WEDNESDAY: return horaEntradaMiercoles;
            case THURSDAY: return horaEntradaJueves;
            case FRIDAY: return horaEntradaViernes;
            case SATURDAY: return horaEntradaSabado;
            case SUNDAY: return horaEntradaDomingo;
            default: return null;
        }
    }

    
    /**
     * Devuelve la Hora de salida para el día especificado.
     * Si el día no está activo o no hay horario definido, devuelve Null.
     */
    public LocalTime getSalidaParaDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return horaSalidaLunes;
            case TUESDAY: return horaSalidaMartes;
            case WEDNESDAY: return horaSalidaMiercoles;
            case THURSDAY: return horaSalidaJueves;
            case FRIDAY: return horaSalidaViernes;
            case SATURDAY: return horaSalidaSabado;
            case SUNDAY: return horaSalidaDomingo;
            default: return null;
        }
    }
    
    /**
     * Devuelve la cantidad de minutos trabajados esperados para el día especificado.
     * Si el día no está activo o no hay horario definido, devuelve 0.
     */
    public int getHorasParaDia(DayOfWeek dia) {
        boolean activo;
        LocalTime entrada;
        LocalTime salida;

        switch (dia) {
            case MONDAY:
                activo = lunes; entrada = horaEntradaLunes; salida = horaSalidaLunes; break;
            case TUESDAY:
                activo = martes; entrada = horaEntradaMartes; salida = horaSalidaMartes; break;
            case WEDNESDAY:
                activo = miercoles; entrada = horaEntradaMiercoles; salida = horaSalidaMiercoles; break;
            case THURSDAY:
                activo = jueves; entrada = horaEntradaJueves; salida = horaSalidaJueves; break;
            case FRIDAY:
                activo = viernes; entrada = horaEntradaViernes; salida = horaSalidaViernes; break;
            case SATURDAY:
                activo = sabado; entrada = horaEntradaSabado; salida = horaSalidaSabado; break;
            case SUNDAY:
                activo = domingo; entrada = horaEntradaDomingo; salida = horaSalidaDomingo; break;
            default:
                return 0;
        }

        if (!activo || entrada == null || salida == null) return 0;
        return TiempoUtils.calcularMinutosLocalTime(entrada, salida);
    }


    @AssertTrue(message = "Debe seleccionar al menos un d�a de la Semana.")
    public boolean isAlMenosUnDiaSeleccionado() {
        return lunes || martes || miercoles || jueves || viernes || sabado || domingo;
    }
}
