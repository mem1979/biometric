package com.sta.biometric.auxiliares;

import java.math.*;     // BigDecimal, RoundingMode
import java.time.*;     // LocalTime, DayOfWeek
import java.util.*;     // Map, LinkedHashMap

import javax.persistence.*;            // JPA: @Entity, @Column, @PrePersist, etc.
import javax.validation.constraints.*; // Bean Validation: @AssertTrue

import org.openxava.annotations.*;     // Anotaciones de OpenXava para UI (@View, @Tab, etc.)
import org.openxava.calculators.*;     // Calculators por defecto (@DefaultValueCalculator)
import org.openxava.jpa.*;             // XPersistence para obtener EntityManager
import org.openxava.model.*;           // Identifiable (id persistente OX)

import com.sta.biometric.acciones.*;       // Acción @OnChange para UI (tu implementación)
import com.sta.biometric.anotaciones.*;    // @MiLabel (decoración de etiquetas en UI)
import com.sta.biometric.calculadores.*;   // CalculadorDefaultFromProperties (valor por defecto configurable)
import com.sta.biometric.enums.*;          // Enum Turnos

import lombok.*;   // Lombok para @Getter/@Setter

/**
 * Entidad que modela un esquema de horarios semanales de un turno.
 *
 * Responsabilidades:
 * - Marcar qué días están activos y sus horas de entrada/salida.
 * - Calcular minutos por día y total semanal:
 *      * Representación amigable "X Hs. Y Min." para UI.
 *      * Representación decimal (BigDecimal) para persistir en 'totalHoras'.
 * - Generar un 'codigo' único por tipo de turno (prefijo por inicial del enum).
 *
 * Mejores prácticas aplicadas:
 * 1) No dependemos de cadenas para calcular totales (evita errores de formato).
 * 2) Soportamos turnos nocturnos (ej. 22:00–06:00 => 8h).
 * 3) Validaciones de consistencia: días activos deben tener horas; tolerancia no negativa.
 */
@View(members =
    // Vista compuesta en secciones: "Jornada", "DIAS" y el resumen "detalleJornadaHoras"
    "Jornada [" +
      // Fila 1: tipo de turno y tolerancia
      "turnoNombre, tolerancia;" +
      // Fila 2: código generado (read-only) y total semanal formateado (derivado)
      "codigo, calculaTotalHoras;" +
    "];" +

    "DIAS [#" + // Sección tipo grid: por cada día, checkbox + entrada + salida + calculado
      "lunes, horaEntradaLunes, horaSalidaLunes, horasLunes;" +
      "martes, horaEntradaMartes, horaSalidaMartes, horasMartes;" +
      "miercoles, horaEntradaMiercoles, horaSalidaMiercoles, horasMiercoles;" +
      "jueves, horaEntradaJueves, horaSalidaJueves, horasJueves;" +
      "viernes, horaEntradaViernes, horaSalidaViernes, horasViernes;" +
      "sabado, horaEntradaSabado, horaSalidaSabado, horasSabado;" +
      "domingo, horaEntradaDomingo, horaSalidaDomingo, horasDomingo;" +
    "];" +
    // Resumen compacto que agrupa días con el mismo horario
    "detalleJornadaHoras"
)
@Tab(
    // Columnas visibles en la lista del módulo
    editors = "List",
    properties = "codigo, turnoNombre.nombre, detalleJornadaHoras, totalHoras"
)
@Entity
@Getter @Setter
public class TurnosHorarios extends Identifiable { // Identifiable provee 'id' y equals/hashCode estándar OX

    // ===========================
    // Identidad y clasificación
    // ===========================

    @ReadOnly                          // No editable desde UI: se genera en @PrePersist
    @SearchKey                         // Clave de búsqueda rápida en OpenXava
    @MiLabel(medida = "grande", negrita = true, recuadro = true) // Estilo visual de etiqueta
    @Column(length = 6, unique = true) // Ej.: "TM.01" (6 es ajustado; amplía si cambias formato)
    private String codigo;


    @LabelFormat(LabelFormatType.SMALL) // Etiqueta compacta en UI
    @Enumerated(EnumType.STRING)        // Persistir enum como texto legible (no ordinal)
    private Turnos turnoNombre;         // Ej.: MANANA, TARDE, NOCHE (según tu enum)

    // ===========================
    // Parámetros del turno
    // ===========================

    @DefaultValueCalculator( // Toma valor de properties; por defecto 5 si no hay propiedad
        value = CalculadorDefaultFromProperties.class,
        properties = {
            @PropertyValue(name = "propiedad", value = "tolerancia.minutos"),
            @PropertyValue(name = "valorPorDefecto", value = "5"),
            @PropertyValue(name = "tipo", value = "int")
        }
    )
    private Integer tolerancia; // Minutos de tolerancia (se valida no-negativo; uso fuera de esta clase)

    // ===========================
    // Días de la semana activos
    // ===========================
    @Column
    @OnChange(TurnosHorariosOnChangeDiaAction.class) // Tu acción UI para reaccionar al toggle de un día
    @DefaultValueCalculator(FalseCalculator.class)   // Por defecto: desactivado
    private boolean lunes, martes, miercoles, jueves, viernes, sabado, domingo;

    // ===========================
    // Horarios por día (LocalTime)
    // ===========================
    // Si un día está activo, ambos deben ser no-nulos (validado más abajo)

    private LocalTime horaEntradaLunes, horaSalidaLunes;
    private LocalTime horaEntradaMartes, horaSalidaMartes;
    private LocalTime horaEntradaMiercoles, horaSalidaMiercoles;
    private LocalTime horaEntradaJueves, horaSalidaJueves;
    private LocalTime horaEntradaViernes, horaSalidaViernes;
    private LocalTime horaEntradaSabado, horaSalidaSabado;
    private LocalTime horaEntradaDomingo, horaSalidaDomingo;

    // ===========================
    // Totales persistidos
    // ===========================

    @Column
    private BigDecimal totalHoras; // Horas semanales en decimal (ej. 16.00); se setea en prePersist/preUpdate

    // ===========================================================
    // Helpers de cálculo (evitan duplicar lógica y facilitan test)
    // ===========================================================

    /**
     * Diferencia en minutos entre 'entrada' y 'salida'.
     * - Si 'salida' es antes que 'entrada' asumimos cruce de medianoche (turno nocturno) => +24h.
     * - Si alguna es null retornamos 0 (consistencia con getters de día).
     */
    private int minutosEntre(LocalTime entrada, LocalTime salida) {
        if (entrada == null || salida == null) return 0;
        int start = entrada.getHour() * 60 + entrada.getMinute();
        int end   = salida.getHour()  * 60 + salida.getMinute();
        int diff  = end - start;
        if (diff < 0) diff += 24 * 60; // soporte a nocturnos: ej. 22:00 -> 06:00
        return diff;
    }

    /** Devuelve "X Hs. Y Min." dado un total de minutos. */
    private String formatearMinutos(int minutos) {
        return (minutos / 60) + " Hs. " + (minutos % 60) + " Min.";
    }

    // ===========================================================
    // Cálculo por día (propiedades derivadas para mostrar en UI)
    // ===========================================================

    @DisplaySize(20)
    @Depends("horaEntradaLunes, horaSalidaLunes") // Recalcula si cambia cualquiera
    public String getHorasLunes() {
        return formatearDuracion( horaEntradaLunes, horaSalidaLunes);
    }

    @DisplaySize(20)
    @Depends("horaEntradaMartes, horaSalidaMartes")
    public String getHorasMartes() {
        return formatearDuracion( horaEntradaMartes, horaSalidaMartes);
    }

    @DisplaySize(20)
    @Depends("horaEntradaMiercoles, horaSalidaMiercoles")
    public String getHorasMiercoles() {
        return formatearDuracion(horaEntradaMiercoles, horaSalidaMiercoles);
    }

    @DisplaySize(20)
    @Depends("horaEntradaJueves, horaSalidaJueves")
    public String getHorasJueves() {
        return formatearDuracion(horaEntradaJueves, horaSalidaJueves);
    }

    @DisplaySize(20)
    @Depends("horaEntradaViernes, horaSalidaViernes")
    public String getHorasViernes() {
        return formatearDuracion(horaEntradaViernes, horaSalidaViernes);
    }

    @DisplaySize(20)
    @Depends("horaEntradaSabado, horaSalidaSabado")
    public String getHorasSabado() {
        return formatearDuracion(horaEntradaSabado, horaSalidaSabado);
    }

    @DisplaySize(20)
    @Depends("horaEntradaDomingo, horaSalidaDomingo")
    public String getHorasDomingo() {
        return formatearDuracion( horaEntradaDomingo, horaSalidaDomingo);
    }

 // Si hay horas definidas calcula; si no, 0. El on-change al desactivar borra las horas.
        private String formatearDuracion(LocalTime entrada, LocalTime salida) {
            if (entrada == null || salida == null) return "0 Hs. 0 Min.";
            return formatearMinutos(minutosEntre(entrada, salida));
        }

    // ===========================================================
    // Total semanal (derivados, sin parsear cadenas intermedias)
    // ===========================================================

    @Depends(
        "lunes, horaEntradaLunes, horaSalidaLunes, horasLunes  " +
        "martes, horaEntradaMartes, horaSalidaMartes, horasMartes" +
        "miercoles, horaEntradaMiercoles, horaSalidaMiercoles, horasMiercoles " +
        "jueves, horaEntradaJueves, horaSalidaJueves, horasJueves " +
        "viernes, horaEntradaViernes, horaSalidaViernes, horasViernes " +
        "sabado, horaEntradaSabado, horaSalidaSabado, horasSabado " +
        "domingo, horaEntradaDomingo, horaSalidaDomingo, horasDomingo"
    )
    @MiLabel(medida = "extra", negrita = true, recuadro = true, icon = "clock") // Estilo destacado
    @LabelFormat(LabelFormatType.NO_LABEL) // Mostrar el valor sin etiqueta a la izquierda
    public String getCalculaTotalHoras() {
        return formatearMinutos(obtenerTotalMinutosSemanales());
    }

    @Label
    @LabelFormat(LabelFormatType.NO_LABEL)
    public BigDecimal getTotalHorasDecimal() {
        // Conversión de minutos a horas con 2 decimales (HALF_UP)
        return BigDecimal.valueOf(obtenerTotalMinutosSemanales())
                         .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    /**
     * Suma en minutos las duraciones de cada día usando la API de negocio (getHorasParaDia).
     * Ventaja: evita acoplarse al formato de strings de los getters de UI.
     */
    private int obtenerTotalMinutosSemanales() {
        int total = 0;
        total += getHorasParaDia(DayOfWeek.MONDAY);
        total += getHorasParaDia(DayOfWeek.TUESDAY);
        total += getHorasParaDia(DayOfWeek.WEDNESDAY);
        total += getHorasParaDia(DayOfWeek.THURSDAY);
        total += getHorasParaDia(DayOfWeek.FRIDAY);
        total += getHorasParaDia(DayOfWeek.SATURDAY);
        total += getHorasParaDia(DayOfWeek.SUNDAY);
        return total;
    }

    // ===========================================================
    // Resumen visual de jornada (agrupa días con mismo horario)
    // ===========================================================

    @Depends(
            "lunes, horaEntradaLunes, horaSalidaLunes, horasLunes  " +
            "martes, horaEntradaMartes, horaSalidaMartes, horasMartes" +
            "miercoles, horaEntradaMiercoles, horaSalidaMiercoles, horasMiercoles " +
            "jueves, horaEntradaJueves, horaSalidaJueves, horasJueves " +
            "viernes, horaEntradaViernes, horaSalidaViernes, horasViernes " +
            "sabado, horaEntradaSabado, horaSalidaSabado, horasSabado " +
            "domingo, horaEntradaDomingo, horaSalidaDomingo, horasDomingo"
        )
    @TextArea
    @LabelFormat(LabelFormatType.SMALL) // Texto compacto
    public String getDetalleJornadaHoras() {
        // Clave: "HH:mm a HH:mm" ; Valor: concatenación de abreviaturas "Lu.Ma." en orden
        Map<String, String> horariosDias = new LinkedHashMap<>();
        agregarDia(horariosDias, "Lu.", lunes,    horaEntradaLunes,     horaSalidaLunes);
        agregarDia(horariosDias, "Ma.", martes,   horaEntradaMartes,    horaSalidaMartes);
        agregarDia(horariosDias, "Mi.", miercoles,horaEntradaMiercoles, horaSalidaMiercoles);
        agregarDia(horariosDias, "Ju.", jueves,   horaEntradaJueves,    horaSalidaJueves);
        agregarDia(horariosDias, "Vi.", viernes,  horaEntradaViernes,   horaSalidaViernes);
        agregarDia(horariosDias, "Sa.", sabado,   horaEntradaSabado,    horaSalidaSabado);
        agregarDia(horariosDias, "Do.", domingo,  horaEntradaDomingo,   horaSalidaDomingo);

        StringBuilder resultado = new StringBuilder();
        for (Map.Entry<String, String> entry : horariosDias.entrySet()) {
            if (resultado.length() > 0) resultado.append(" / ");
            // Ej.: "Lu.Ma." + " de " + "08:00 a 16:00" + " Hs"
            resultado.append(entry.getValue()).append(" de ").append(entry.getKey()).append(" Hs");
        }
        return resultado.toString();
    }

    /**
     * Si 'activo' y ambos horarios definidos, agrega el día a la agrupación por rango horario.
     * - Usa merge() para concatenar abreviaturas si ya existe la misma clave "HH:mm a HH:mm".
     */
    private void agregarDia(Map<String, String> mapa, String dia, boolean activo, LocalTime entrada, LocalTime salida) {
        if (activo && entrada != null && salida != null) {
            String horario = String.format("%02d:%02d a %02d:%02d",
                entrada.getHour(), entrada.getMinute(), salida.getHour(), salida.getMinute());
            mapa.merge(horario, dia, (dias, nuevoDia) -> dias + nuevoDia);
        }
    }

    // ===========================================================
    // Ciclo de persistencia (JPA)
    // ===========================================================

    @PrePersist
    private void preGuardar() {
        // Mantener 'totalHoras' sincronizado al insertar
        setTotalHoras(getTotalHorasDecimal());
        // Generación del código único por prefijo de turno
        generarCodigo();
    }

    @PreUpdate
    private void preActualizar() {
        // Mantener 'totalHoras' sincronizado al actualizar
        setTotalHoras(getTotalHorasDecimal());
    }

    /**
     * Genera 'codigo' con patrón: "T{InicialTurno}.{NN}" (ej.: MANANA -> "TM.01").
     * - Busca el máximo existente con ese prefijo y lo incrementa.
     * - Limita a 10 códigos por prefijo (01..10); si se excede lanza excepción.
     * Nota de concurrencia:
     *   Hay ventana de carrera entre el SELECT y el INSERT. La restricción 'unique = true'
     *   protege la integridad; ante colisión deberías capturar la excepción y reintentar.
     */
    private void generarCodigo() {
        if (turnoNombre == null) {
            throw new IllegalStateException("El nombre del turno no puede estar vacío.");
        }
        String inicial = turnoNombre.name().substring(0, 1).toUpperCase(); // Inicial del enum (ej. 'M')
        String prefijo = "T" + inicial;                                    // Ej. "TM"

        // Consulta JPA para obtener el mayor 'codigo' con el prefijo (orden lexicográfico)
        Query q = XPersistence.getManager().createQuery(
            "select max(e.codigo) from " + getClass().getSimpleName() +
            " e where e.codigo like :codigo"
        );
        q.setParameter("codigo", prefijo + ".%");

        String ultimo = (String) q.getSingleResult(); // Ej.: "TM.03" o null si no existe ninguno
        int nro = (ultimo == null) ? 1 : Integer.parseInt(ultimo.substring(3)) + 1; // "TM." ocupa 3 chars

        if (nro > 99) {
            throw new IllegalStateException("Se ha alcanzado el máximo de 99 códigos para el turno: " + turnoNombre);
        }
        this.codigo = prefijo + "." + String.format("%02d", nro); // Formato 2 dígitos: 01..99
    }

    // ===========================================================
    // API de negocio por día (para reglas externas y para el total)
    // ===========================================================

    /** ¿El día está marcado como laboral? */
    public boolean esLaboral(DayOfWeek dia) {
        switch (dia) {
            case MONDAY:    return lunes;
            case TUESDAY:   return martes;
            case WEDNESDAY: return miercoles;
            case THURSDAY:  return jueves;
            case FRIDAY:    return viernes;
            case SATURDAY:  return sabado;
            case SUNDAY:    return domingo;
            default:        return false;
        }
    }

    /** Hora de entrada (o null si el día no aplica/no configurado). */
    public LocalTime getEntradaParaDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY:    return horaEntradaLunes;
            case TUESDAY:   return horaEntradaMartes;
            case WEDNESDAY: return horaEntradaMiercoles;
            case THURSDAY:  return horaEntradaJueves;
            case FRIDAY:    return horaEntradaViernes;
            case SATURDAY:  return horaEntradaSabado;
            case SUNDAY:    return horaEntradaDomingo;
            default:        return null;
        }
    }

    /** Hora de salida (o null si el día no aplica/no configurado). */
    public LocalTime getSalidaParaDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY:    return horaSalidaLunes;
            case TUESDAY:   return horaSalidaMartes;
            case WEDNESDAY: return horaSalidaMiercoles;
            case THURSDAY:  return horaSalidaJueves;
            case FRIDAY:    return horaSalidaViernes;
            case SATURDAY:  return horaSalidaSabado;
            case SUNDAY:    return horaSalidaDomingo;
            default:        return null;
        }
    }

    /**
     * Minutos esperados de trabajo para 'dia'.
     * - 0 si el día no está activo o faltan horarios.
     * - Soporta nocturnos vía 'minutosEntre'.
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
        return (activo && entrada != null && salida != null) ? minutosEntre(entrada, salida) : 0;
    }

    // ===========================================================
    // Validaciones Bean Validation (se muestran en UI y bloquean persistencia)
    // ===========================================================

    /** Debe existir al menos un día activo. */
    @AssertTrue(message = "Debe seleccionar al menos un día de la Semana.")
    public boolean isAlMenosUnDiaSeleccionado() {
        return lunes || martes || miercoles || jueves || viernes || sabado || domingo;
    }

    /** Para cada día activo deben definirse ambas horas (entrada y salida). */
    @AssertTrue(message = "Para cada día activo debe definir hora de entrada y salida.")
    public boolean isHorasDefinidasParaDiasActivos() {
        return (!lunes    || (horaEntradaLunes    != null && horaSalidaLunes    != null)) &&
               (!martes   || (horaEntradaMartes   != null && horaSalidaMartes   != null)) &&
               (!miercoles|| (horaEntradaMiercoles!= null && horaSalidaMiercoles!= null)) &&
               (!jueves   || (horaEntradaJueves   != null && horaSalidaJueves   != null)) &&
               (!viernes  || (horaEntradaViernes  != null && horaSalidaViernes  != null)) &&
               (!sabado   || (horaEntradaSabado   != null && horaSalidaSabado   != null)) &&
               (!domingo  || (horaEntradaDomingo  != null && horaSalidaDomingo  != null));
    }

    /** 'tolerancia' no puede ser negativa (null se admite como "sin configurar"). */
    @AssertTrue(message = "La tolerancia no puede ser negativa.")
    public boolean isToleranciaValida() {
        return tolerancia == null || tolerancia >= 0;
    }
}
